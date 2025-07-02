package com.example.coupon.service;

import com.example.coupon.model.Coupon;
import com.example.coupon.model.IssuedCoupon;
import com.example.coupon.repository.CouponRepository;
import com.example.coupon.repository.IssuedCouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CouponService {
    
    private final CouponRepository couponRepository;
    private final IssuedCouponRepository issuedCouponRepository;
    
    @Autowired
    public CouponService(CouponRepository couponRepository, IssuedCouponRepository issuedCouponRepository) {
        this.couponRepository = couponRepository;
        this.issuedCouponRepository = issuedCouponRepository;
    }
    
    /**
     * 쿠폰 발급 메서드 - 동시성 제어와 중복 발급 방지
     * Pessimistic Lock을 사용하여 동시성 문제 해결
     */
    @Transactional
    public CouponIssueResult issueCoupon(Long couponId, Long userId) {
        try {
            // 1. 중복 발급 확인
            if (issuedCouponRepository.existsByCouponIdAndUserId(couponId, userId)) {
                return new CouponIssueResult(false, "이미 발급받은 쿠폰입니다.");
            }
            
            // 2. Pessimistic Lock으로 쿠폰 조회
            Coupon coupon = couponRepository.findByIdWithLock(couponId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));
            
            // 3. 발급 가능 여부 확인
            if (!coupon.canIssue()) {
                return new CouponIssueResult(false, "쿠폰 발급 기간이 아니거나 수량이 모두 소진되었습니다.");
            }
            
            // 4. 쿠폰 발급 처리
            coupon.increaseIssuedCount();
            couponRepository.save(coupon);
            
            // 5. 발급 내역 저장
            IssuedCoupon issuedCoupon = new IssuedCoupon(coupon, userId);
            issuedCouponRepository.save(issuedCoupon);
            
            return new CouponIssueResult(true, "쿠폰이 성공적으로 발급되었습니다.");
            
        } catch (Exception e) {
            return new CouponIssueResult(false, "쿠폰 발급 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 모든 쿠폰 조회
     */
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }
    
    /**
     * 활성화된 쿠폰 조회
     */
    public List<Coupon> getActiveCoupons() {
        return couponRepository.findActiveCoupons();
    }
    
    /**
     * 쿠폰 생성
     */
    @Transactional
    public Coupon createCoupon(String name, Integer totalCount, LocalDateTime startAt, LocalDateTime endAt) {
        Coupon coupon = new Coupon(name, totalCount, startAt, endAt);
        return couponRepository.save(coupon);
    }
    
    /**
     * 특정 쿠폰의 발급 내역 조회
     */
    public List<IssuedCoupon> getIssuedCoupons(Long couponId) {
        return issuedCouponRepository.findByCouponId(couponId);
    }
    
    /**
     * 사용자별 발급 내역 조회
     */
    public List<IssuedCoupon> getUserIssuedCoupons(Long userId) {
        return issuedCouponRepository.findByUserId(userId);
    }
    
    /**
     * 쿠폰 발급 결과 클래스
     */
    public static class CouponIssueResult {
        private final boolean success;
        private final String message;
        
        public CouponIssueResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
} 