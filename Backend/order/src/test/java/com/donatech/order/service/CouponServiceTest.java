package com.donatech.order.service;

import com.donatech.order.exception.ResourceNotFoundException;
import com.donatech.order.model.Coupon;
import com.donatech.order.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock CouponRepository couponRepository;

    @InjectMocks CouponService couponService;

    @Test
    void getCouponByCode_exists_returnsCoupon() {
        Coupon coupon = Coupon.builder().code("SAVE10").discountAmount(1000).active(true).build();
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));

        Coupon result = couponService.getCouponByCode("SAVE10");

        assertThat(result.getCode()).isEqualTo("SAVE10");
    }

    @Test
    void getCouponByCode_notFound_throwsException() {
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.getCouponByCode("INVALID"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createCoupon_validAmount_savesAndReturns201() {
        when(couponRepository.save(any())).thenAnswer(inv -> {
            Coupon c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        ResponseEntity<Coupon> response = couponService.createCoupon(2000);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getDiscountAmount()).isEqualTo(2000);
        assertThat(response.getBody().isActive()).isTrue();
    }

    @Test
    void updateCouponStatusByCode_setsInactive() {
        Coupon coupon = Coupon.builder().code("ABC123").active(true).build();
        when(couponRepository.findByCode("ABC123")).thenReturn(Optional.of(coupon));
        when(couponRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        couponService.updateCouponStatusByCode("ABC123", false);

        assertThat(coupon.isActive()).isFalse();
        verify(couponRepository).save(coupon);
    }

    @Test
    void deleteCoupon_existingId_deletesFromRepository() {
        Coupon coupon = Coupon.builder().id(1L).code("DEL01").build();
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        couponService.deleteCoupon(1L);

        verify(couponRepository).delete(coupon);
    }
}
