package com.donatech.order.service;

import com.donatech.order.controller.response.DashboardResponse;
import com.donatech.order.controller.response.MessageResponse;
import com.donatech.order.controller.response.OrderResponse;
import com.donatech.order.dto.QRScanDto;
import com.donatech.order.model.DonationStatus;
import com.donatech.order.model.Order;
import com.donatech.order.model.QRTracking;
import com.donatech.order.repository.OrderRepository;
import com.donatech.order.repository.QRTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QRTrackingService {

    private final QRTrackingRepository qrTrackingRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final QRGeneratorService qrGeneratorService;

    public ResponseEntity<Map<String, String>> generateQR(Long donationId) {
        Order order = orderService.getOrderById(donationId);

        String content = qrGeneratorService.buildQRContent(donationId, order.getUserEmail());
        String base64 = qrGeneratorService.generateQRBase64(content);

        order.setQrCode(content);
        order.setQrGeneratedAt(LocalDateTime.now());
        orderRepository.save(order);

        return ResponseEntity.ok(Map.of(
                "donationId", String.valueOf(donationId),
                "qrBase64", base64,
                "qrContent", content
        ));
    }

    public ResponseEntity<MessageResponse> recordScan(QRScanDto dto) {
        // Verify donation exists
        orderService.getOrderById(dto.getDonationId());

        QRTracking scan = QRTracking.builder()
                .donationId(dto.getDonationId())
                .scannedAt(LocalDateTime.now())
                .location(dto.getLocation())
                .scannedByEmail(dto.getScannedByEmail())
                .notes(dto.getNotes())
                .build();

        qrTrackingRepository.save(scan);
        return ResponseEntity.ok(new MessageResponse("Escaneo registrado correctamente."));
    }

    public ResponseEntity<List<QRTracking>> getHistory(Long donationId) {
        return ResponseEntity.ok(qrTrackingRepository.findByDonationIdOrderByScannedAtDesc(donationId));
    }

    public ResponseEntity<DashboardResponse> getDashboard() {
        List<Order> all = orderRepository.findAll();

        long totalDonations = all.size();

        long totalItems = all.stream()
                .mapToLong(o -> o.getItems().size())
                .sum();

        Map<String, Long> byStatus = Arrays.stream(DonationStatus.values())
                .collect(Collectors.toMap(
                        DonationStatus::name,
                        s -> all.stream().filter(o -> o.getEstado() == s).count()
                ));

        Map<Long, Long> byZone = all.stream()
                .filter(o -> o.getZonaCatastrofeId() != null)
                .collect(Collectors.groupingBy(Order::getZonaCatastrofeId, Collectors.counting()));

        return ResponseEntity.ok(DashboardResponse.builder()
                .totalDonations(totalDonations)
                .totalItems(totalItems)
                .donationsByStatus(byStatus)
                .donationsByZone(byZone)
                .build());
    }
}
