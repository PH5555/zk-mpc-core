package com.zkrypto.zk_mpc_core.infrastucture.amqp;

import com.zkrypto.zk_mpc_core.application.tss.TssService;
import com.zkrypto.zk_mpc_core.common.config.RabbitMqConfig;
import com.zkrypto.zk_mpc_core.infrastucture.amqp.dto.InitProtocolEndMessage;
import com.zkrypto.zk_mpc_core.infrastucture.amqp.dto.ProceedRoundMessage;
import com.zkrypto.zk_mpc_core.infrastucture.amqp.dto.ProtocolCompleteMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageConsumer {

    private final TssService tssService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "", durable = "true", exclusive = "true", autoDelete = "false"),
            exchange = @Exchange(value = RabbitMqConfig.TSS_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = RabbitMqConfig.TSS_ROUND_END_ROUTING_KEY_PREFIX
    ))
    public void handleRoundEndMessage(ProceedRoundMessage message) {
        log.info("라운드 메시지 수신");
        tssService.collectMessageAndCheckCompletion(message.type(), message.message(), message.sid());
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "", durable = "true", exclusive = "true", autoDelete = "false"),
            exchange = @Exchange(value = RabbitMqConfig.TSS_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = RabbitMqConfig.TSS_INIT_END_ROUTING_KEY_PREFIX
    ))
    public void handleInitProtocolEndMessage(InitProtocolEndMessage message) {
        log.info("프로토콜 시작 완료 메시지 수신");
        tssService.checkInitProtocolStatus(message.sid(), message.memberId(), message.type());
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "", durable = "true", exclusive = "true", autoDelete = "false"),
            exchange = @Exchange(value = RabbitMqConfig.TSS_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = RabbitMqConfig.TSS_PROTOCOL_COMPLETE_KEY_PREFIX
    ))
    public void handleProtocolCompleteMessage(ProtocolCompleteMessage message) {
        log.info("프로토콜 종료 메시지 수신");
        tssService.checkProtocolCompleteStatus(message.sid(), message.memberId(), message.type());
    }
}
