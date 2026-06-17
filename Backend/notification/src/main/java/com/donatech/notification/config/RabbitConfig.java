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
    public Queue donationReceivedQueue() {
        return new Queue("notification.donation.received", true);
    }

    @Bean
    public Binding donationReceivedBinding(Queue donationReceivedQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(donationReceivedQueue).to(donatechExchange).with("transfer.submitted");
    }

    @Bean
    public Queue transferApprovedQueue() {
        return new Queue("notification.transfer.approved", true);
    }

    @Bean
    public Binding transferApprovedBinding(Queue transferApprovedQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(transferApprovedQueue).to(donatechExchange).with("transfer.validated");
    }

    @Bean
    public Queue deliverySubmittedQueue() {
        return new Queue("notification.delivery.submitted", true);
    }

    @Bean
    public Binding deliverySubmittedBinding(Queue deliverySubmittedQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(deliverySubmittedQueue).to(donatechExchange).with("delivery.submitted");
    }

    @Bean
    public Queue orderDeliveredQueue() {
        return new Queue("notification.order.delivered", true);
    }

    @Bean
    public Binding orderDeliveredBinding(Queue orderDeliveredQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(orderDeliveredQueue).to(donatechExchange).with("order.delivered");
    }

    @Bean
    public Queue beneficiaryThankYouQueue() {
        return new Queue("notification.beneficiary.thank-you", true);
    }

    @Bean
    public Binding beneficiaryThankYouBinding(Queue beneficiaryThankYouQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(beneficiaryThankYouQueue).to(donatechExchange).with("beneficiary.thank-you");
    }

    @Bean
    public Queue deliveryIncomingQueue() {
        return new Queue("notification.delivery.incoming", true);
    }

    @Bean
    public Binding deliveryIncomingBinding(Queue deliveryIncomingQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(deliveryIncomingQueue).to(donatechExchange).with("delivery.incoming");
    }

    @Bean
    public Queue deliveryConfirmRequestQueue() {
        return new Queue("notification.delivery.confirm-request", true);
    }

    @Bean
    public Binding deliveryConfirmRequestBinding(Queue deliveryConfirmRequestQueue, TopicExchange donatechExchange) {
        return BindingBuilder.bind(deliveryConfirmRequestQueue).to(donatechExchange).with("delivery.confirm-request");
    }

    @Bean
    public Queue routeAssignedQueue() {
        return new Queue("notification.route.assigned", true);
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
