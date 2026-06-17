package com.donatech.order.controller.response;

import com.donatech.order.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationResponse {
    private Long id;
    private String userEmail;
    private String donorName;
    private PaymentStatus estadoPago;
    private Integer total;
    private Integer discountApplied;
    private String couponCode;
    private String transferProofUrl;
    private LocalDateTime transferProofUploadedAt;
    private String rejectionReason;
    private LocalDateTime fechaCreacion;
    // Activa = el pago no es terminal o queda ≥1 orden no entregada/cancelada/rechazada.
    private boolean activa;
    private List<OrderResponse> orders;
}
