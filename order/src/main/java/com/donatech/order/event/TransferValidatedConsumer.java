package com.donatech.order.event;

import com.donatech.order.model.DonationStatus;
import com.donatech.order.model.Order;
import com.donatech.order.model.TrackingHistory;
import com.donatech.order.repository.OrderRepository;
import com.donatech.order.repository.TrackingHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferValidatedConsumer {

    private final OrderRepository orderRepository;
    private final TrackingHistoryRepository trackingHistoryRepository;
    private final DonationEventPublisher donationEventPublisher;

    @RabbitListener(queues = "order.transfer.validated")
    public void handleTransferValidated(TransferValidatedEvent event) {
        log.info("Recibido transfer.validated para orden id={}, aprobado={}", event.orderId(), event.approved());

        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null) {
            log.warn("Orden id={} no encontrada al procesar transfer.validated", event.orderId());
            return;
        }

        DonationStatus estadoAnterior = order.getEstado();
        DonationStatus estadoNuevo = event.approved() ? DonationStatus.EN_PREPARACION : DonationStatus.RECHAZADA;

        if (!event.approved()) {
            order.setRejectionReason(event.motivo());
        }
        order.setEstado(estadoNuevo);
        orderRepository.save(order);

        trackingHistoryRepository.save(TrackingHistory.builder()
                .order(order)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .comentario(event.motivo())
                .fechaCambio(LocalDateTime.now())
                .build());

        if (estadoNuevo == DonationStatus.EN_PREPARACION) {
            var items = order.getItems().stream()
                    .map(i -> new DonationItemEvent(i.getKitId(), i.getQuantity()))
                    .toList();
            donationEventPublisher.publishDonationConfirmed(
                    new DonationConfirmedEvent(order.getId(), order.getUserEmail(), items, LocalDateTime.now())
            );
            donationEventPublisher.publishOrderReadyForShipping(
                    new OrderReadyForShippingEvent(
                            order.getId(),
                            order.getUserEmail(),
                            order.getBeneficiaryId(),
                            order.getZonaCatastrofeId()
                    )
            );
        }
    }
}
