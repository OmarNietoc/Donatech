package com.donatech.order.event;

import com.donatech.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeneficiaryEventConsumer {

    private final OrderRepository orderRepository;

    @RabbitListener(queues = "order.beneficiary.ready")
    public void handleBeneficiaryVerified(BeneficiaryVerifiedEvent event) {
        log.info("Beneficiario verificado: id={} userId={} rut={} — habilitando asignación de kits",
                event.beneficiaryId(), event.userId(), event.rut());

        int ordenesExistentes = orderRepository.findByBeneficiaryId(event.beneficiaryId()).size();
        if (ordenesExistentes > 0) {
            log.info("Beneficiario id={} tiene {} órdenes registradas listas para asignación de kits",
                    event.beneficiaryId(), ordenesExistentes);
        } else {
            log.info("Beneficiario id={} verificado — elegible para recibir kits de donación",
                    event.beneficiaryId());
        }
    }
}
