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
    private String userEmail;
    private DonationStatus estado;
    private CouponResponse coupon;
    private Integer discountApplied;
    private Integer finalPrice;
    private LocalDateTime orderDate;
    private List<OrderItem> items;

}
