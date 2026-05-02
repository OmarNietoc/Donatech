package com.donatech.order.service;

import com.donatech.order.controller.response.MessageResponse;
import com.donatech.order.exception.ResourceNotFoundException;
import com.donatech.order.model.Coupon;
import com.donatech.order.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public ResponseEntity<List<Coupon>> getAllCoupons() {
        List<Coupon> coupons = couponRepository.findAll();
        if (coupons.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(coupons);
    }

    public Coupon getCouponById(Long id) {

        return couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Error al usar cupón." + id));
    }

    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Error al usar cupón."));
    }

    public ResponseEntity<Coupon> createCoupon(Integer discountAmount) {
        String generatedCode = UUID.randomUUID().toString().replace("-", "").substring(0, 9).toUpperCase();

        Coupon coupon = Coupon.builder()
                .code(generatedCode)
                .discountAmount(discountAmount)
                .active(true)
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);

        return ResponseEntity
                .status(201) // 201 Created
                .body(savedCoupon);
    }

    public ResponseEntity<MessageResponse> updateCouponById(Long id, Coupon updatedCoupon) {
        Coupon coupon = getCouponById(id);
        coupon.setCode(updatedCoupon.getCode());
        coupon.setDiscountAmount(updatedCoupon.getDiscountAmount());
        coupon.setActive(updatedCoupon.isActive());
        couponRepository.save(coupon);
        return ResponseEntity.ok(new MessageResponse("Cupón actualizado correctamente."));
    }

    public ResponseEntity<MessageResponse> deleteCoupon(Long id) {
        Coupon coupon = getCouponById(id);
        couponRepository.delete(coupon);
        return ResponseEntity.ok(new MessageResponse("Cupón eliminado correctamente."));
    }

    public ResponseEntity<?> updateCouponStatusByCode(String code, boolean active) {
        Coupon coupon = getCouponByCode(code);
        coupon.setActive(active);
        couponRepository.save(coupon);
        return ResponseEntity.ok(coupon);
    }
}