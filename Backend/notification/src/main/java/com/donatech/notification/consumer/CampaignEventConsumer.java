package com.donatech.notification.consumer;

import com.donatech.notification.event.CampaignResultEvent;
import com.donatech.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignEventConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "notification.campaign.activated")
    public void handleCampaignActivated(CampaignResultEvent event) {
        log.info("Notificando campaña activada id={}", event.campaignId());
        Context ctx = new Context();
        ctx.setVariable("campaignId", event.campaignId());
        emailService.sendHtmlEmail(
                event.recipientEmail(),
                "Tu campaña ha sido aprobada — Donatech",
                "campaign-activated",
                ctx
        );
    }

    @RabbitListener(queues = "notification.campaign.rejected")
    public void handleCampaignRejected(CampaignResultEvent event) {
        log.info("Notificando campaña rechazada id={}", event.campaignId());
        Context ctx = new Context();
        ctx.setVariable("campaignId", event.campaignId());
        ctx.setVariable("motivo", event.motivo());
        emailService.sendHtmlEmail(
                event.recipientEmail(),
                "Tu campaña no fue aprobada — Donatech",
                "campaign-rejected",
                ctx
        );
    }
}
