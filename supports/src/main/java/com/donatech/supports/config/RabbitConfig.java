package com.donatech.supports.config;

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
    public Queue stockAlertQueue() {
        return new Queue("supports.stock.alert", true);
    }

    @Bean
    public Binding stockAlertBinding(Queue stockAlertQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(stockAlertQueue).to(donatechExchange).with("stock.low");
    }

    @Bean
    public Queue campaignCreatedQueue() {
        return new Queue("supports.campaign.created", true);
    }

    @Bean
    public Binding campaignCreatedBinding(Queue campaignCreatedQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(campaignCreatedQueue).to(donatechExchange).with("campaign.created");
    }

    @Bean
    public Queue transferSubmittedQueue() {
        return new Queue("supports.transfer.submitted", true);
    }

    @Bean
    public Binding transferSubmittedBinding(Queue transferSubmittedQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(transferSubmittedQueue).to(donatechExchange).with("transfer.submitted");
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
