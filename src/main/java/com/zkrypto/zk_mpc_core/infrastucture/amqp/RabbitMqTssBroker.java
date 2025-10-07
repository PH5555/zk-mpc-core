package com.zkrypto.zk_mpc_core.infrastucture.amqp;

import com.zkrypto.zk_mpc_core.application.message.MessageBroker;
import com.zkrypto.zk_mpc_core.common.config.RabbitMqConfig;
import com.zkrypto.zk_mpc_core.infrastucture.amqp.dto.ProceedRoundCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMqTssBroker implements MessageBroker {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(String recipient, String message, String type, String sid) {
        String routingKey = RabbitMqConfig.TSS_DELIVER_ROUTING_KEY_PREFIX + recipient;
        ProceedRoundCommand command = ProceedRoundCommand.builder()
                .message(message)
                .type(type)
                .sid(sid)
                .build();
        rabbitTemplate.convertAndSend(RabbitMqConfig.TSS_EXCHANGE, routingKey, command);
    }
}
