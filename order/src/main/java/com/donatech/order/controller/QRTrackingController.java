package com.donatech.order.controller;

import com.donatech.order.controller.response.DashboardResponse;
import com.donatech.order.controller.response.MessageResponse;
import com.donatech.order.dto.QRScanDto;
import com.donatech.order.model.QRTracking;
import com.donatech.order.service.QRTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "QR & Dashboard", description = "Generación de QR, trazabilidad y resumen de donaciones")
public class QRTrackingController {

    private final QRTrackingService qrTrackingService;

    @Operation(summary = "Generar QR para una donación")
    @PostMapping("/api/donations/{id}/generate-qr")
    public ResponseEntity<Map<String, String>> generateQR(@PathVariable Long id) {
        return qrTrackingService.generateQR(id);
    }

    @Operation(summary = "Registrar escaneo de QR")
    @PostMapping("/api/qr-tracking/scan")
    public ResponseEntity<MessageResponse> recordScan(@Valid @RequestBody QRScanDto dto) {
        return qrTrackingService.recordScan(dto);
    }

    @Operation(summary = "Historial de escaneos por donación")
    @GetMapping("/api/qr-tracking/{donationId}")
    public ResponseEntity<List<QRTracking>> getHistory(@PathVariable Long donationId) {
        return qrTrackingService.getHistory(donationId);
    }

    @Operation(summary = "Dashboard — resumen de donaciones")
    @GetMapping("/api/dashboard/summary")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return qrTrackingService.getDashboard();
    }
}
