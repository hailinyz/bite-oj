package com.bite.judge.service;

import com.bite.judge.domain.SandBoxExecuteResult;

import java.util.List;

public interface ISandboxService {
    SandBoxExecuteResult exeJavaCode(String userCode, List<String> inputList);
}
