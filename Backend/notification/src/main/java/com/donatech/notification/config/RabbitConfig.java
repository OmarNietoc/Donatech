package com.donatech.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
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
    public Queue campaignActivatedQueue() {
        return new Queue("notification.campaign.activated", true);
    }

    @Bean
    public Binding campaignActivatedBinding(Queue campaignActivatedQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(campaignActivatedQueue).to(donatechExchange).with("campaign.activated");
    }

    @Bean
    public Queue campaignRejectedQueue() {
        return new Queue("notification.campaign.rejected", true);
    }

    @Bean
    public Binding campaignRejectedBinding(Queue campaignRejectedQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(campaignRejectedQueue).to(donatechExchange).with("campaign.rejected");
    }

    @Bean
    public Queue transferRejectedQueue() {
        return new Queue("notification.transfer.rejected", true);
    }

    @Bean
    public Binding transferRejectedBinding(Queue transferRejectedQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(transferRejectedQueue).to(donatechExchange).with("transfer.rejected");
    }

    @Bean
    public Queue orderShippedQueue() {
        return new Queue("notification.order.shipped", true);
    }

    @Bean
    public Binding orderShippedBinding(Queue orderShippedQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(orderShippedQueue).to(donatechExchange).with("order.shipped");
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
