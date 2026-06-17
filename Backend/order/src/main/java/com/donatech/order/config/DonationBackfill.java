package com.donatech.order.config;

import com.donatech.order.model.Donation;
import com.donatech.order.model.DonationStatus;
import com.donatech.order.model.Order;
import com.donatech.order.model.PaymentStatus;
import com.donatech.order.repository.DonationRepository;
import com.donatech.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Backfill idempotente: envuelve cada Order legacy (sin Donation padre) en una Donation de 1 hija.
 * Solo procesa órdenes con donation_id NULL; una vez vinculadas no se reprocesan. Ignora carritos (DRAFT).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DonationBackfill implements CommandLineRunner {

    private final OrderRepository orderRepository;
    private final DonationRepository donationRepository;

    @Override
    @Transactional
    public void run(String... args) {
        var orphans = orderRepository.findByDonationIsNull();
        int wrapped = 0;
        for (Order o : orphans) {
            if (o.getEstado() == DonationStatus.DRAFT) continue; // carrito, aún no es donación
            Donation d = Donation.builder()
                    .userEmail(o.getUserEmail())
                    .donorName(o.getDonorName())
                    .couponCode(o.getCoupon() != null ? o.getCoupon().getCode() : null)
                    .discountApplied(o.getDiscountApplied() != null ? o.getDiscountApplied() : 0)
                    .total(o.getFinalPrice() != null ? o.getFinalPrice() : 0)
                    .transferProofUrl(o.getTransferProofUrl())
                    .transferProofUploadedAt(o.getTransferProofUploadedAt())
                    .estadoPago(mapPaymentStatus(o.getEstado()))
                    .fechaCreacion(o.getOrderDate())
                    .build();
            donationRepository.save(d);
            o.setDonation(d);
            orderRepository.save(o);
            wrapped++;
        }
        if (wrapped > 0) log.info("DonationBackfill: {} orden(es) legacy envueltas en Donation", wrapped);
    }

    private PaymentStatus mapPaymentStatus(DonationStatus estado) {
        return switch (estado) {
            case INGRESADA -> PaymentStatus.INGRESADA;
            case EN_VALIDACION_TRANSFERENCIA -> PaymentStatus.EN_VALIDACION_TRANSFERENCIA;
            case RECHAZADA -> PaymentStatus.RECHAZADA;
            case CANCELADA -> PaymentStatus.CANCELADA;
            default -> PaymentStatus.APROBADA; // EN_PREPARACION..ENTREGADA
        };
    }
}
