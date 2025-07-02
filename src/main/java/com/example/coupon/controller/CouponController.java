package com.example.coupon.controller;

import com.example.coupon.model.Coupon;
import com.example.coupon.model.IssuedCoupon;
import com.example.coupon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {
    
    private final CouponService couponService;
    
    @Autowired
    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }
    
    /**
     * 쿠폰 발급 API
     */
    @PostMapping("/{couponId}/issue")
    public ResponseEntity<Map<String, Object>> issueCoupon(
            @PathVariable Long couponId,
            @RequestParam Long userId) {
        
        CouponService.CouponIssueResult result = couponService.issueCoupon(couponId, userId);
        
        return ResponseEntity.ok(Map.of(
            "success", result.isSuccess(),
            "message", result.getMessage(),
            "timestamp", LocalDateTime.now()
        ));
    }
    
    /**
     * 모든 쿠폰 조회 API
     */
    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        List<Coupon> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(coupons);
    }
    
    /**
     * 활성화된 쿠폰 조회 API
     */
    @GetMapping("/active")
    public ResponseEntity<List<Coupon>> getActiveCoupons() {
        List<Coupon> coupons = couponService.getActiveCoupons();
        return ResponseEntity.ok(coupons);
    }
    
    /**
     * 쿠폰 생성 API
     */
    @PostMapping
    public ResponseEntity<Coupon> createCoupon(@RequestBody CouponCreateRequest request) {
        Coupon coupon = couponService.createCoupon(
            request.name(), 
            request.totalCount(), 
            request.startAt(), 
            request.endAt(),
            request.maxPerUser() != null ? request.maxPerUser() : 1
        );
        return ResponseEntity.ok(coupon);
    }
    
    /**
     * 특정 쿠폰의 발급 내역 조회 API
     */
    @GetMapping("/{couponId}/issued")
    public ResponseEntity<List<IssuedCoupon>> getIssuedCoupons(@PathVariable Long couponId) {
        List<IssuedCoupon> issuedCoupons = couponService.getIssuedCoupons(couponId);
        return ResponseEntity.ok(issuedCoupons);
    }
    
    /**
     * 사용자별 발급 내역 조회 API
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<IssuedCoupon>> getUserIssuedCoupons(@PathVariable Long userId) {
        List<IssuedCoupon> issuedCoupons = couponService.getUserIssuedCoupons(userId);
        return ResponseEntity.ok(issuedCoupons);
    }
    
    /**
     * 특정 쿠폰 정보 조회 API (실시간 업데이트용)
     */
    @GetMapping("/{couponId}")
    public ResponseEntity<Coupon> getCoupon(@PathVariable Long couponId) {
        Coupon coupon = couponService.getAllCoupons().stream()
                .filter(c -> c.getId().equals(couponId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));
        return ResponseEntity.ok(coupon);
    }
    
    /**
     * 사용자별 쿠폰 발급 현황 조회 API
     */
    @GetMapping("/{couponId}/user/{userId}/status")
    public ResponseEntity<CouponService.UserCouponStatus> getUserCouponStatus(
            @PathVariable Long couponId, 
            @PathVariable Long userId) {
        
        CouponService.UserCouponStatus status = couponService.getUserCouponStatus(couponId, userId);
        return ResponseEntity.ok(status);
    }
    
    /**
     * 쿠폰 생성 요청 DTO
     */
    public record CouponCreateRequest(
        String name,
        Integer totalCount,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer maxPerUser  // 1인당 최대 발급 가능 수량
    ) {}
} 