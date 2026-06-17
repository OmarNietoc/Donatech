package com.donatech.shipping.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    public Queue orderReadyQueue() {
        return new Queue("shipping.order.ready", true);
    }

    @Bean
    public Binding orderReadyBinding(Queue orderReadyQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(orderReadyQueue).to(donatechExchange).with("order.ready_for_shipping");
    }

    // ── Ciclo de vida de la ruta: sincroniza estado de ruta/envío con la orden ──

    @Bean
    public Queue orderShippedQueue() {
        return new Queue("shipping.order.shipped", true);
    }

    @Bean
    public Binding orderShippedBinding(Queue orderShippedQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(orderShippedQueue).to(donatechExchange).with("order.shipped");
    }

    @Bean
    public Queue orderDeliveredQueue() {
        return new Queue("shipping.order.delivered", true);
    }

    @Bean
    public Binding orderDeliveredBinding(Queue orderDeliveredQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(orderDeliveredQueue).to(donatechExchange).with("order.delivered");
    }

    @Bean
    public Queue donationCancelledQueue() {
        return new Queue("shipping.donation.cancelled", true);
    }

    @Bean
    public Binding donationCancelledBinding(Queue donationCancelledQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(donationCancelledQueue).to(donatechExchange).with("donation.cancelled");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.donatech.*");
        converter.setJavaTypeMapper(typeMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
