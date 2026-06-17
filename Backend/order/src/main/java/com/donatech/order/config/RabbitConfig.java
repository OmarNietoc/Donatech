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
    public Queue transferValidatedQueue() {
        return new Queue("order.transfer.validated", true);
    }

    @Bean
    public Binding transferValidatedBinding(Queue transferValidatedQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(transferValidatedQueue).to(donatechExchange).with("transfer.validated");
    }

    // transfer.rejected también enruta al mismo queue: el consumer distingue por event.approved()
    // y mueve la orden a RECHAZADA. Sin este binding el rechazo se descarta y la orden
    // queda atascada en EN_VALIDACION_TRANSFERENCIA.
    @Bean
    public Binding transferRejectedBinding(Queue transferValidatedQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(transferValidatedQueue).to(donatechExchange).with("transfer.rejected");
    }

    // Ruta asignada por shipping → mover órdenes a ASIGNADA_ENVIO + guardar colaborador.
    @Bean
    public Queue routeAssignedQueue() {
        return new Queue("order.route.assigned", true);
    }

    @Bean
    public Binding routeAssignedBinding(Queue routeAssignedQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(routeAssignedQueue).to(donatechExchange).with("route.assigned");
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
