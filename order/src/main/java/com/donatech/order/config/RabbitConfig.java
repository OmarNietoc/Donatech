package com.donatech.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "donatech.events";

    @Bean
    public TopicExchange donatechExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue beneficiaryReadyQueue() {
        return new Queue("order.beneficiary.ready", true);
    }

    @Bean
    public Binding beneficiaryReadyBinding(Queue beneficiaryReadyQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(beneficiaryReadyQueue).to(donatechExchange).with("beneficiary.verified");
    }

    @Bean
    public Queue trackingUpdateQueue() {
        return new Queue("order.tracking.update", true);
    }

    @Bean
    public Binding trackingUpdateBinding(Queue trackingUpdateQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(trackingUpdateQueue).to(donatechExchange).with("qr.scanned");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.donatech.*");
        converter.setJavaTypeMapper(typeMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
