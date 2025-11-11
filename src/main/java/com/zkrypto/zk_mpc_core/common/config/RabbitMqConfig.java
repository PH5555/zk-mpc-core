package com.zkrypto.zk_mpc_core.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Value("${spring.rabbitmq.port}")
    private String port;
    @Value("${spring.rabbitmq.host}")
    private String host;
    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;

    public static final String TSS_EXCHANGE = "tss.exchange";

    public static final String ROUND_QUEUE = "tss.core.round";
    public static final String INIT_HANDLE_QUEUE = "tss.core.init.handle";
    public static final String PROTOCOL_COMPLETE_HANDLE_QUEUE = "tss.core.protocol.complete.handle";
    public static final String ROUND_COMPLETE_HANDLE_QUEUE = "tss.core.round.complete.handle";
    public static final String ERROR_HANDLE_QUEUE = "tss.core.error.handle";

    public static final String TSS_ROUND_END_ROUTING_KEY_PREFIX = "topic.round.end";
    public static final String TSS_ROUND_COMPLETE_KEY_PREFIX = "topic.round.complete";
    public static final String TSS_ERROR_HANDLE_KEY_PREFIX = "topic.error.handle";
    public static final String TSS_ROUND_ROUTING_KEY_PREFIX = "topic.round";
    public static final String TSS_INIT_ROUTING_KEY_PREFIX = "topic.init";
    public static final String TSS_INIT_END_ROUTING_KEY_PREFIX = "topic.init.end";
    public static final String TSS_START_ROUTING_KEY_PREFIX = "topic.start";
    public static final String TSS_PROTOCOL_COMPLETE_KEY_PREFIX = "topic.complete";

    public static final String TSS_DLX_EXCHANGE = "tss.dead.letter.exchange";
    public static final String TSS_DLQ_QUEUE = "tss.dead.letter.queue";
    public static final String TSS_DLQ_ROUTING_KEY = "tss.dead.letter.key";

    @Bean
    public TopicExchange tssExchange() {
        return new TopicExchange(TSS_EXCHANGE);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(TSS_DLX_EXCHANGE);
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(TSS_DLQ_QUEUE).build();
    }

    @Bean
    Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(TSS_DLQ_ROUTING_KEY);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setPort(Integer.parseInt(port));
        connectionFactory.setHost(host);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory());
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
