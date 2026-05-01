package com.donatech.order.event;

import com.donatech.order.dto.QRScanDto;
import com.donatech.order.service.QRTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrScannedEventConsumer {

    private final QRTrackingService qrTrackingService;

    @RabbitListener(queues = "order.tracking.update")
    public void handleQrScanned(QrScannedEvent event) {
        log.info("QR escaneado para donación id={} por {}", event.donationId(), event.scannedByEmail());

        QRScanDto dto = QRScanDto.builder()
                .donationId(event.donationId())
                .scannedByEmail(event.scannedByEmail())
                .location(event.location())
                .notes(event.notes())
                .build();

        qrTrackingService.recordScan(dto);
    }
}
