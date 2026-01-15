package com.bite.judge.rabbit;

import com.bite.api.domain.dto.JudgeSubmitDTO;
import com.bite.common.core.constants.RabbitMQConstants;
import com.bite.judge.service.IJudgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JudgeConsumer {

    @Autowired
    private IJudgeService judgeService;


    @RabbitListener(queues = RabbitMQConstants.OJ_WORK_QUEUE) // 监听队列
    public void consume(JudgeSubmitDTO judgeSubmitDTO) {
        log.info("收到消息为: {}", judgeSubmitDTO);
        judgeService.doJudgeJavaCode(judgeSubmitDTO);
    }



}
