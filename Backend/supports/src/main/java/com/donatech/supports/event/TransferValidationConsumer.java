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
public class TransferValidationConsumer {

    private final SoporteService soporteService;

    @RabbitListener(queues = "supports.transfer.submitted")
    public void handleTransferSubmitted(TransferSubmittedEvent event) {
        log.info("Auto-creando ticket VALIDACION_TRANSFERENCIA para donación id={}", event.donationId());

        SoporteRequestDTO dto = new SoporteRequestDTO();
        dto.setTitulo("Validación transferencia — donación #" + event.donationId());
        dto.setDescripcion("Donante " + event.userEmail() +
                " adjuntó comprobante de transferencia para la donación #" + event.donationId() +
                ". Fecha: " + event.submittedAt());
        dto.setUsuarioId(0L);
        dto.setPrioridad(PrioridadSoporte.ALTO);
        dto.setTipo(TipoSoporte.VALIDACION_TRANSFERENCIA);
        dto.setDonationId(event.donationId());
        dto.setRecipientEmail(event.userEmail());

        soporteService.crear(dto);
    }
}
