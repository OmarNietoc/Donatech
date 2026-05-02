package com.donatech.notification.consumer;

import com.donatech.notification.event.CampaignResultEvent;
import com.donatech.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignEventConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "notification.campaign.activated")
    public void handleCampaignActivated(CampaignResultEvent event) {
        log.info("Notificando campaña activada id={}", event.campaignId());
        // TODO: resolver email de la org desde el campaignId (via Feign a catalog/users)
        // Por ahora se loguea — el email se enviará cuando se integre el Feign client
        log.info("Campaña #{} activada. Motivo/observación: {}", event.campaignId(), event.motivo());
    }

    @RabbitListener(queues = "notification.campaign.rejected")
    public void handleCampaignRejected(CampaignResultEvent event) {
        log.info("Notificando campaña rechazada id={}", event.campaignId());
        log.info("Campaña #{} rechazada. Motivo: {}", event.campaignId(), event.motivo());
    }
}
