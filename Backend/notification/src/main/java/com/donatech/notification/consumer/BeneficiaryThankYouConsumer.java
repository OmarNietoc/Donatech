package com.donatech.notification.consumer;

import com.donatech.notification.client.OrderImageClient;
import com.donatech.notification.event.BeneficiaryThankYouEvent;
import com.donatech.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeneficiaryThankYouConsumer {

    private final EmailService emailService;
    private final OrderImageClient orderImageClient;

    @RabbitListener(queues = "notification.beneficiary.thank-you")
    public void handleThankYou(BeneficiaryThankYouEvent event) {
        log.info("Enviando agradecimiento del beneficiario al donante {} (orden id={})",
                event.donorEmail(), event.orderId());

        List<byte[]> images = new ArrayList<>();
        for (int i = 0; i < event.imageCount(); i++) {
            try {
                byte[] bytes = orderImageClient.getThankYouImage(event.orderId(), i);
                if (bytes != null && bytes.length > 0) images.add(bytes);
            } catch (RuntimeException e) {
                log.warn("No se pudo obtener la imagen {} de la orden {}: {}", i, event.orderId(), e.getMessage());
            }
        }

        Context ctx = new Context();
        ctx.setVariable("message", event.message());
        ctx.setVariable("donorName", event.donorName() != null ? event.donorName() : "donante");
        ctx.setVariable("beneficiaryName", event.beneficiaryName() != null ? event.beneficiaryName() : "el beneficiario");
        ctx.setVariable("imageIndexes", buildIndexes(images.size()));

        emailService.sendHtmlEmailWithInlineImages(
                event.donorEmail(),
                "💙 " + event.beneficiaryName() + " te agradece tu donación — Donatech",
                "beneficiary-thank-you",
                ctx,
                images
        );
    }

    private List<Integer> buildIndexes(int size) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < size; i++) indexes.add(i);
        return indexes;
    }
}
