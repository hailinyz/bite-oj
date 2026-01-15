package com.bite.judge.config;

import cn.hutool.core.io.FileUtil;
import com.bite.common.core.constants.JudgeConstants;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DockerSandBoxPoll {

    private DockerClient dockerClient;

    private String sandboxImage;

    private String volumeDir;

    private Long memoryLimit;

    private Long memorySwapLimit;

    private Long cpuLimit;

    private int poolSize;

    private String containerNamePrefix;

    private BlockingQueue<String> containerQueue;

    private Map<String, String> containerNameMap;


    public DockerSandBoxPoll(DockerClient dockerClient,
                             String sandboxImage, String volumeDir, Long memoryLimit,
                             Long memorySwapLimit, Long cpuLimit,
                             int poolSize, String containerNamePrefix) {
        this.dockerClient = dockerClient;
        this.sandboxImage = sandboxImage;
        this.volumeDir = volumeDir;
        this.memoryLimit = memoryLimit;
        this.memorySwapLimit = memorySwapLimit;
        this.cpuLimit = cpuLimit;
        this.poolSize = poolSize;
        this.containerNamePrefix = containerNamePrefix;
        this.containerQueue = new ArrayBlockingQueue<>(poolSize);
        this.containerNameMap = new HashMap<>();
    }


    public void initDockerPool() { //初始化docker容器池
        for (int i = 0; i < poolSize; i++) {
            createContainer(containerNamePrefix + "-" + i);
        }
    }

    public String getContainer() {
        try {
            return containerQueue.take(); //获取一个空闲的docker容器
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void returnContainer(String containerId) {
        containerQueue.add(containerId);
    }

    private void createContainer(String containerName) {

        //如果已经有容器了，就不用再创建了
        List<Container> containerList = dockerClient.listContainersCmd().withShowAll(true).exec();
        if (!CollectionUtils.isEmpty(containerList)) {
            String names = JudgeConstants.JAVA_CONTAINER_PREFIX + containerName;
            for (Container container : containerList) {
                String[] containerNames = container.getNames();
                if (containerNames != null && containerNames.length > 0 && names.equals(containerNames[0])) {
                    if ("created".equals(container.getState()) || "exited".equals(container.getState())) {
                        //启动容器
                        dockerClient.startContainerCmd(container.getId()).exec();
                    }
                    containerQueue.add(container.getId());
                    containerNameMap.put(container.getId(), containerName);
                    return;
                }
            }
        }


        // 拉取镜像
        pullJavaEnvImage();

        // 创建容器 限制资源 控制权限
        HostConfig hostConfig = getHostConfig(containerName);
        CreateContainerCmd containerCmd = dockerClient
                .createContainerCmd(JudgeConstants.JAVA_ENV_IMAGE)
                .withName(containerName);

        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();

        // 记录容器id
        String containerId = createContainerResponse.getId();
        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();
        containerQueue.add(containerId);
        containerNameMap.put(containerId, containerName);
    }



    // 拉取java执行环境镜像 需要控制只拉取一次
    private void pullJavaEnvImage() {
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        List<Image> imageList = listImagesCmd.exec();

        // 检查本地是否已存在目标镜像
        for (Image image : imageList) {
            String[] repoTags = image.getRepoTags();
            if (repoTags != null && repoTags.length > 0 && sandboxImage.equals(repoTags[0])) {
                // 镜像已存在，直接返回，避免重复拉取
                return;
            }
        }

        // 镜像不存在，执行拉取
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(sandboxImage);
        try {
            pullImageCmd.exec(new PullImageResultCallback()).awaitCompletion();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    // 限制资源 控制权限
    private HostConfig getHostConfig(String ContainerName) {
        HostConfig hostConfig = new HostConfig();

        // 设置挂载目录，指定用户代码路径
        String userCodeDir = createContainerDir(ContainerName);
        hostConfig.setBinds(new Bind(userCodeDir, new Volume(volumeDir)));

        // 限制docker容器使用资源
        hostConfig.withMemory(memoryLimit);
        hostConfig.withMemorySwap(memorySwapLimit);
        hostConfig.withCpuCount(cpuLimit);
        hostConfig.withNetworkMode("none"); // 禁用网络
        hostConfig.withReadonlyRootfs(true); // 禁止在root目录写文件

        return hostConfig;
    }


    public String getCodeDir(String containerId) {
        String containerName = containerNameMap.get(containerId);
        return System.getProperty("user.dir") + File.separator + JudgeConstants.CODE_DIR_POOL + File.separator + containerName;
    }


    //为每个容器，创建的指定挂载文件
    private String createContainerDir(String containerName) {
        //一级目录 存放所有容器的挂载目录
        String codeDir = System.getProperty("user.dir") + File.separator + JudgeConstants.CODE_DIR_POOL;
        if (!FileUtil.exist(codeDir)) {
            FileUtil.mkdir(codeDir);
        }
        return codeDir + File.separator + containerName;
    }


}
