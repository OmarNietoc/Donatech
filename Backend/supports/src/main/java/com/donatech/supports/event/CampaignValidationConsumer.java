package com.donatech.supports.event;

import com.donatech.supports.client.UserClient;
import com.donatech.supports.dto.SoporteRequestDTO;
import com.donatech.supports.dto.UsuarioDTO;
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
    private final UserClient userClient;

    @RabbitListener(queues = "supports.campaign.created")
    public void handleCampaignCreated(CampaignCreatedEvent event) {
        log.info("Auto-creando ticket VALIDACION_CAMPAÑA para campaña id={}", event.campaignId());

        String recipientEmail = null;
        UsuarioDTO usuario = userClient.getUserById(event.beneficiaryId());
        if (usuario != null) {
            recipientEmail = usuario.getCorreo();
        } else {
            log.warn("No se pudo resolver email para beneficiaryId={}", event.beneficiaryId());
        }

        SoporteRequestDTO dto = new SoporteRequestDTO();
        dto.setTitulo("Validación campaña: " + event.titulo());
        dto.setDescripcion("Campaña '" + event.titulo() + "' requiere validación. Motivo: " + event.motivo());
        dto.setUsuarioId(event.beneficiaryId());
        dto.setPrioridad(PrioridadSoporte.ALTO);
        dto.setTipo(TipoSoporte.VALIDACION_CAMPAÑA);
        dto.setCampaignId(event.campaignId());
        dto.setRecipientEmail(recipientEmail);

        soporteService.crear(dto);
    }
}
