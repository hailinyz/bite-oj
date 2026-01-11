package com.bite.judge.service.impl;

import com.bite.judge.domain.SandBoxExecuteResult;
import com.bite.judge.service.ISandboxService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SandboxServiceImpl implements ISandboxService {

    /*
     代码沙箱运行java代码
     */
    @Override
    public SandBoxExecuteResult exeJavaCode(String userCode, List<String> inputList) {
        return null;
    }
}
