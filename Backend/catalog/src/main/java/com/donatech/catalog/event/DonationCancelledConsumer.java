package com.donatech.catalog.event;

import com.donatech.catalog.model.Kit;
import com.donatech.catalog.repository.CampaignKitRepository;
import com.donatech.catalog.repository.KitRepository;
import com.donatech.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonationCancelledConsumer {

    private final ProductService productService;
    private final KitRepository kitRepository;
    private final CampaignKitRepository campaignKitRepository;

    // @Transactional: Kit.items es LAZY y se accede fuera de petición HTTP
    @Transactional
    @RabbitListener(queues = "catalog.stock.restore")
    public void handleDonationCancelled(DonationCancelledEvent event) {
        log.info("Restaurando stock para donación cancelada id={}", event.donationId());
        event.items().forEach(item -> {
            Kit kit = kitRepository.findById(item.kitId()).orElse(null);
            if (kit == null) {
                log.warn("Kit id={} no encontrado, omitiendo restauración de stock", item.kitId());
                return;
            }
            kit.getItems().forEach(kitItem -> {
                int totalUnits = kitItem.getCantidadRequerida() * item.quantity();
                productService.restoreStock(kitItem.getProduct().getId(), totalUnits);
            });

            // Revertir contador "recibidos": la donación cancelada ya había descontado stock
            decrementReceived(event.campaignId(), item.kitId(), item.quantity());
        });
    }

    private void decrementReceived(Long campaignId, Long kitId, Integer quantity) {
        if (campaignId == null) return;
        campaignKitRepository.findByCampaignIdAndKitId(campaignId, kitId).ifPresent(ck -> {
            ck.setCantidadFulfilled(Math.max(0, ck.getCantidadFulfilled() - quantity));
            campaignKitRepository.save(ck);
        });
    }
}
