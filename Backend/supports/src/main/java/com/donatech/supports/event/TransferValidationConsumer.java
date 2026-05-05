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
        log.info("Auto-creando ticket VALIDACION_TRANSFERENCIA para orden id={}", event.orderId());

        SoporteRequestDTO dto = new SoporteRequestDTO();
        dto.setTitulo("Validación transferencia — orden #" + event.orderId());
        dto.setDescripcion("Donante " + event.userEmail() +
                " adjuntó comprobante de transferencia para orden #" + event.orderId() +
                ". Fecha: " + event.submittedAt());
        dto.setUsuarioId(0L);
        dto.setPrioridad(PrioridadSoporte.ALTO);
        dto.setTipo(TipoSoporte.VALIDACION_TRANSFERENCIA);
        dto.setDonationId(event.orderId());
        dto.setRecipientEmail(event.userEmail());

        soporteService.crear(dto);
    }
}
