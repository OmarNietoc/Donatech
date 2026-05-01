package com.donatech.catalog.event;

import com.donatech.catalog.model.Product;
import com.donatech.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonationConfirmedConsumer {

    private final ProductService productService;
    private final StockLowPublisher stockLowPublisher;

    @RabbitListener(queues = "catalog.stock.deduct")
    public void handleDonationConfirmed(DonationConfirmedEvent event) {
        log.info("Descontando stock para donación id={}", event.donationId());
        event.items().forEach(item -> {
            Product product = productService.deductStock(item.productId(), item.quantity());
            if (product.getStock() <= product.getStockMinimo()) {
                stockLowPublisher.publishStockLow(new StockLowEvent(
                        product.getId(),
                        product.getNombre(),
                        product.getStock(),
                        product.getStockMinimo()
                ));
            }
        });
    }
}
