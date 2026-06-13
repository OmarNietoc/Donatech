package com.donatech.catalog.config;

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
    public Queue stockDeductQueue() {
        return new Queue("catalog.stock.deduct", true);
    }

    @Bean
    public Binding stockDeductBinding(Queue stockDeductQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(stockDeductQueue).to(donatechExchange).with("donation.confirmed");
    }

    @Bean
    public Queue stockRestoreQueue() {
        return new Queue("catalog.stock.restore", true);
    }

    @Bean
    public Binding stockRestoreBinding(Queue stockRestoreQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(stockRestoreQueue).to(donatechExchange).with("donation.cancelled");
    }

    @Bean
    public Queue orderDeliveredQueue() {
        return new Queue("catalog.order.delivered", true);
    }

    @Bean
    public Binding orderDeliveredBinding(Queue orderDeliveredQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(orderDeliveredQueue).to(donatechExchange).with("order.delivered");
    }

    @Bean
    public Queue campaignResultQueue() {
        return new Queue("catalog.campaign.result", true);
    }

    @Bean
    public Binding campaignActivatedBinding(Queue campaignResultQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(campaignResultQueue).to(donatechExchange).with("campaign.activated");
    }

    @Bean
    public Binding campaignRejectedBinding(Queue campaignResultQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(campaignResultQueue).to(donatechExchange).with("campaign.rejected");
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
