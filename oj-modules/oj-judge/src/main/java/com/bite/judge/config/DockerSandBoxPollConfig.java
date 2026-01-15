package com.bite.judge.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerSandBoxPollConfig {

    @Value("${sandbox.docker.host:tcp://localhost:2375}")
    private String dockerHost;

    @Value("${sandbox.docker.image:amazoncorretto:8}")
    private String sandboxImage;

    @Value("${sandbox.docker.volume:/usr/share/java}")
    private String volumeDir;

    @Value("${sandbox.limit.memory:10000000}")
    private Long memoryLimit;

    @Value("${sandbox.limit.memory-swap:10000000}")
    private Long memorySwapLimit;

    @Value("${sandbox.limit.cpu:1}")
    private Long cpuLimit;

    @Value("${sandbox.docker.pool.size:4}")
    private int poolSize;

    @Value("${sandbox.docker.name-prefix:oj-sandbox-jdk}")
    private String containerNamePrefix;


    @Bean
    public DockerClient createDockerClient() {
        DefaultDockerClientConfig clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();

        DockerClient dockerClient = DockerClientBuilder //都是要拿client操作第三方
                .getInstance(clientConfig)
                .withDockerCmdExecFactory(new NettyDockerCmdExecFactory())
                .build();
        return dockerClient;
    }

    @Bean
    public DockerSandBoxPoll createDockerSandBoxPoll(DockerClient dockerClient) {
        DockerSandBoxPoll dockerSandBoxPoll = new DockerSandBoxPoll(dockerClient, sandboxImage, volumeDir, memoryLimit,
                memorySwapLimit, cpuLimit, poolSize, containerNamePrefix);
        dockerSandBoxPoll.initDockerPool();
        return dockerSandBoxPoll;
    }


}
