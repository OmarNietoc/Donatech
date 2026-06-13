package com.donatech.catalog.event;

import com.donatech.catalog.model.CampaignKit;
import com.donatech.catalog.model.Kit;
import com.donatech.catalog.model.Product;
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
public class DonationConfirmedConsumer {

    private final ProductService productService;
    private final KitRepository kitRepository;
    private final CampaignKitRepository campaignKitRepository;
    private final StockLowPublisher stockLowPublisher;

    // @Transactional: Kit.items es LAZY y se accede fuera de petición HTTP
    @Transactional
    @RabbitListener(queues = "catalog.stock.deduct")
    public void handleDonationConfirmed(DonationConfirmedEvent event) {
        log.info("Descontando stock para donación id={}", event.donationId());
        event.items().forEach(item -> {
            Kit kit = kitRepository.findById(item.kitId()).orElse(null);
            if (kit == null) {
                log.warn("Kit id={} no encontrado, omitiendo descuento de stock", item.kitId());
                return;
            }
            kit.getItems().forEach(kitItem -> {
                int totalUnits = kitItem.getCantidadRequerida() * item.quantity();
                Product product = productService.deductStock(kitItem.getProduct().getId(), totalUnits);
                if (product.getStock() <= product.getStockMinimo()) {
                    stockLowPublisher.publishStockLow(new StockLowEvent(
                            product.getId(),
                            product.getNombre(),
                            product.getStock(),
                            product.getStockMinimo()
                    ));
                }
            });

            // Contador "recibidos" por kit en la campaña de la donación
            incrementReceived(event.campaignId(), item.kitId(), item.quantity());
        });
    }

    private void incrementReceived(Long campaignId, Long kitId, Integer quantity) {
        if (campaignId == null) return;
        campaignKitRepository.findByCampaignIdAndKitId(campaignId, kitId).ifPresentOrElse(ck -> {
            ck.setCantidadFulfilled(ck.getCantidadFulfilled() + quantity);
            campaignKitRepository.save(ck);
        }, () -> log.warn("CampaignKit no encontrado para campaña={} kit={}; contador recibidos no actualizado", campaignId, kitId));
    }
}
