package com.zkrypto.zk_mpc_core.infrastucture.amqp;

import com.zkrypto.dto.ErrorMessage;
import com.zkrypto.zk_mpc_core.common.annotation.PreventDuplicate;
import com.zkrypto.zk_mpc_core.common.config.RabbitMqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class DeadLetterConsumer {

    @PreventDuplicate
    @RabbitListener(
            bindings = @QueueBinding(
                value = @Queue(value = RabbitMqConfig.TSS_DLQ_QUEUE, durable = "true", exclusive = "false", autoDelete = "false"),
                exchange = @Exchange(value = RabbitMqConfig.TSS_DLX_EXCHANGE, type = ExchangeTypes.DIRECT),
                key = RabbitMqConfig.TSS_DLQ_ROUTING_KEY
    ))
    public void handleDeadLetter(ErrorMessage errorMessage) {
        log.info("----------------------------------------");
        log.info("[DLQ LISTENER] Detected NACK message: " + errorMessage.sessionId());
        log.info("[DLQ THREAD] Executing on thread: " + Thread.currentThread().getName());
        log.info("----------------------------------------");
    }
}
