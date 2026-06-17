package com.donatech.order.controller.response;

import com.donatech.order.model.DonationStatus;
import com.donatech.order.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private Long donationId;
    private String userEmail;
    private String donorName;
    private DonationStatus estado;
    private CouponResponse coupon;
    private Integer discountApplied;
    private Integer logisticsCost;
    private Integer finalPrice;
    private LocalDateTime orderDate;
    private Long campaignId;
    private String campaignTitulo;
    private Long beneficiaryId;
    private String beneficiaryName;
    private String beneficiaryApellido;
    private String beneficiaryDireccion;
    private String beneficiaryComuna;
    private String beneficiaryRegion;
    private String routeId;
    private String routeName;
    private Long zonaCatastrofeId;
    private String rejectionReason;
    private LocalDateTime deliveryConfirmedAt;
    private String transferProofUrl;
    private String transportistaNombre;
    private String transportistaContacto;
    private LocalDateTime courierAssignedAt;
    private String deliveryPhotoUrl;
    private String deliveryDocumentUrl;
    private String thankYouMessage;
    private LocalDateTime thankYouSentAt;
    private List<OrderItem> items;

}
