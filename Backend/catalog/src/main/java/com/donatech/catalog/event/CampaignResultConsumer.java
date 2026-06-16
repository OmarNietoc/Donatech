package com.donatech.catalog.event;

import com.donatech.catalog.model.CampaignStatus;
import com.donatech.catalog.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignResultConsumer {

    private final CampaignRepository campaignRepository;

    @RabbitListener(queues = "catalog.campaign.result")
    public void handleCampaignResult(CampaignResultEvent event) {
        campaignRepository.findById(event.campaignId()).ifPresentOrElse(campaign -> {
            if (event.approved()) {
                campaign.setEstado(CampaignStatus.ACTIVA);
                campaign.setFechaActivacion(LocalDateTime.now());
                campaign.setCostoLogistica(event.logistica() != null ? event.logistica() : 0);
            } else {
                campaign.setEstado(CampaignStatus.INACTIVA);
                campaign.setMotivoRechazo(event.motivo());
            }
            campaignRepository.save(campaign);
            log.info("Campaign {} → {}", event.campaignId(), campaign.getEstado());
        }, () -> log.warn("Campaign {} not found for result event", event.campaignId()));
    }
}
