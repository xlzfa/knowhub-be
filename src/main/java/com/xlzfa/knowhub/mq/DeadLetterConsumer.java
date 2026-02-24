package com.xlzfa.knowhub.mq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterConsumer {

    @RabbitListener(queues = "answer.like.dlq")
    public void handleDeadMessage(Object msg) {

        System.err.println("死信消息: " + msg);

    }
}