package com.donatech.supports.event;

import com.donatech.supports.dto.SoporteRequestDTO;
import com.donatech.supports.model.PrioridadSoporte;
import com.donatech.supports.model.TipoSoporte;
import com.donatech.supports.service.SoporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignValidationConsumer {

    private final SoporteService soporteService;

    @RabbitListener(queues = "supports.campaign.created")
    public void handleCampaignCreated(CampaignCreatedEvent event) {
        log.info("Auto-creando ticket VALIDACION_CAMPAÑA para campaña id={}", event.campaignId());

        SoporteRequestDTO dto = new SoporteRequestDTO();
        dto.setTitulo("Validación campaña: " + event.titulo());
        dto.setDescripcion("Campaña '" + event.titulo() + "' requiere validación. Motivo: " + event.motivo());
        dto.setUsuarioId(event.beneficiaryId());
        dto.setPrioridad(PrioridadSoporte.ALTO);
        dto.setTipo(TipoSoporte.VALIDACION_CAMPAÑA);
        dto.setCampaignId(event.campaignId());

        soporteService.crear(dto);
    }
}
