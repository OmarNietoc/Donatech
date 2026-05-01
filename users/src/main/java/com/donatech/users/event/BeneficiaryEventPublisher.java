package com.donatech.users.event;

import com.donatech.users.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeneficiaryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishBeneficiaryVerified(BeneficiaryVerifiedEvent event) {
        log.info("Publicando beneficiary.verified para beneficiario id={}", event.beneficiaryId());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "beneficiary.verified", event);
    }
}
