package com.zkrypto.zk_mpc_core.infrastucture.amqp;

import com.zkrypto.zk_mpc_core.application.message.MessageBroker;
import com.zkrypto.zk_mpc_core.application.message.dto.InitProtocolEndEvent;
import com.zkrypto.zk_mpc_core.application.message.dto.MessageProcessEndEvent;
import com.zkrypto.zk_mpc_core.common.config.RabbitMqConfig;
import com.zkrypto.zk_mpc_core.infrastucture.amqp.dto.ProceedRoundMessage;
import com.zkrypto.zk_mpc_core.infrastucture.amqp.dto.StartProtocolMessage;
import com.zkrypto.zk_mpc_core.infrastucture.amqp.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMqTssBroker implements MessageBroker {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(MessageProcessEndEvent event) {
        String routingKey = RabbitMqConfig.TSS_DELIVER_ROUTING_KEY_PREFIX + event.recipient();
        ProceedRoundMessage message = MessageMapper.from(event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }

    @Override
    public void publish(InitProtocolEndEvent event) {
        String routingKey = RabbitMqConfig.TSS_DELIVER_ROUTING_KEY_PREFIX + event.sid();
        StartProtocolMessage message = MessageMapper.from(event);
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, message);
    }
}
