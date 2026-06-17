package com.donatech.order.event;

import com.donatech.order.model.Donation;
import com.donatech.order.model.DonationStatus;
import com.donatech.order.model.Order;
import com.donatech.order.model.PaymentStatus;
import com.donatech.order.model.TrackingHistory;
import com.donatech.order.repository.DonationRepository;
import com.donatech.order.repository.TrackingHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferValidatedConsumer {

    private final DonationRepository donationRepository;
    private final TrackingHistoryRepository trackingHistoryRepository;
    private final DonationEventPublisher donationEventPublisher;

    // @Transactional mantiene la sesión Hibernate abierta para los LAZY (orders/items).
    @Transactional
    @RabbitListener(queues = "order.transfer.validated")
    public void handleTransferValidated(TransferValidatedEvent event) {
        log.info("Recibido transfer.validated para donación id={}, aprobado={}", event.donationId(), event.approved());

        Donation donation = donationRepository.findById(event.donationId()).orElse(null);
        if (donation == null) {
            log.warn("Donación id={} no encontrada al procesar transfer.validated", event.donationId());
            return;
        }

        donation.setEstadoPago(event.approved() ? PaymentStatus.APROBADA : PaymentStatus.RECHAZADA);
        if (!event.approved()) donation.setRejectionReason(event.motivo());
        donationRepository.save(donation);

        DonationStatus estadoNuevo = event.approved() ? DonationStatus.EN_PREPARACION : DonationStatus.RECHAZADA;

        // Aplicar a cada orden hija (cada una su campaña/beneficiario).
        for (Order order : donation.getOrders()) {
            DonationStatus estadoAnterior = order.getEstado();
            // Solo transicionar órdenes que aún esperan validación (no tocar canceladas).
            if (estadoAnterior == DonationStatus.CANCELADA) continue;

            if (!event.approved()) order.setRejectionReason(event.motivo());
            order.setEstado(estadoNuevo);

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
                        new DonationConfirmedEvent(order.getId(), order.getUserEmail(), order.getCampaignId(), items, LocalDateTime.now()));
                donationEventPublisher.publishOrderReadyForShipping(
                        new OrderReadyForShippingEvent(order.getId(), order.getUserEmail(),
                                order.getBeneficiaryId(), order.getZonaCatastrofeId()));
            }
        }
    }
}
