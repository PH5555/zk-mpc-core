package com.zkrypto.zk_mpc_core.infrastucture.amqp;

import com.zkrypto.zk_mpc_core.application.message.MessageBroker;
import com.zkrypto.zk_mpc_core.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.InitProtocolEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.MessageProcessEndEvent;
import com.zkrypto.zk_mpc_core.common.config.RabbitMqConfig;
import com.zkrypto.zk_mpc_core.infrastucture.amqp.dto.InitProtocolMessage;
import com.zkrypto.zk_mpc_core.infrastucture.amqp.dto.ProceedRoundMessage;
import com.zkrypto.zk_mpc_core.infrastucture.amqp.dto.StartProtocolMessage;
import com.zkrypto.zk_mpc_core.infrastucture.amqp.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqBroker implements MessageBroker {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(MessageProcessEndEvent event) {
        String routingKey = RabbitMqConfig.TSS_ROUND_ROUTING_KEY_PREFIX + "." + event.recipient();
        ProceedRoundMessage message = MessageMapper.from(event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }

    @Override
    public void publish(InitProtocolEndEvent event) {
        String routingKey = RabbitMqConfig.TSS_START_ROUTING_KEY_PREFIX + "." + event.recipient();
        StartProtocolMessage message = MessageMapper.from(event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }

    @Override
    public void publish(InitProtocolEvent event) {
        String routingKey = RabbitMqConfig.TSS_INIT_ROUTING_KEY_PREFIX + "." + event.recipient();
        InitProtocolMessage message = MessageMapper.from(event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }
}
