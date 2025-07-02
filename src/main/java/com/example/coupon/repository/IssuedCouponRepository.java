package com.example.coupon.repository;

import com.example.coupon.model.IssuedCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssuedCouponRepository extends JpaRepository<IssuedCoupon, Long> {
    
    // 특정 사용자가 특정 쿠폰을 이미 발급받았는지 확인
    boolean existsByCouponIdAndUserId(Long couponId, Long userId);
    
    // 특정 쿠폰의 발급 내역 조회
    List<IssuedCoupon> findByCouponId(Long couponId);
    
    // 특정 사용자의 발급 내역 조회
    List<IssuedCoupon> findByUserId(Long userId);
    
    // 특정 쿠폰의 발급 수량 조회
    @Query("SELECT COUNT(ic) FROM IssuedCoupon ic WHERE ic.coupon.id = :couponId")
    Long countByCouponId(@Param("couponId") Long couponId);
} 