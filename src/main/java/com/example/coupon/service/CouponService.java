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
            // 1. Pessimistic Lock으로 쿠폰 조회 (먼저 조회해서 maxPerUser 확인)
            Coupon coupon = couponRepository.findByIdWithLock(couponId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));
            
            // 2. 사용자별 발급 수량 확인
            Long userIssuedCount = issuedCouponRepository.countByCouponIdAndUserId(couponId, userId);
            if (userIssuedCount >= coupon.getMaxPerUser()) {
                return new CouponIssueResult(false, 
                    String.format("1인당 최대 %d개까지만 발급 가능합니다. (현재 %d개 발급됨)", 
                    coupon.getMaxPerUser(), userIssuedCount));
            }
            
            // 3. 쿠폰 발급 기간 및 수량 확인
            if (!coupon.canIssue()) {
                return new CouponIssueResult(false, "쿠폰 발급 기간이 아니거나 수량이 모두 소진되었습니다.");
            }
            
            // 4. 쿠폰 발급 처리
            coupon.increaseIssuedCount();
            couponRepository.save(coupon);
            
            // 5. 발급 내역 저장
            IssuedCoupon issuedCoupon = new IssuedCoupon(coupon, userId);
            issuedCouponRepository.save(issuedCoupon);
            
            return new CouponIssueResult(true, 
                String.format("쿠폰이 성공적으로 발급되었습니다! (%d/%d개 발급됨)", 
                userIssuedCount + 1, coupon.getMaxPerUser()));
            
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
     * 쿠폰 생성 (maxPerUser 포함)
     */
    @Transactional
    public Coupon createCoupon(String name, Integer totalCount, LocalDateTime startAt, LocalDateTime endAt, Integer maxPerUser) {
        Coupon coupon = new Coupon(name, totalCount, startAt, endAt, maxPerUser);
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
     * 사용자별 쿠폰 발급 현황 조회
     */
    public UserCouponStatus getUserCouponStatus(Long couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));
        
        Long userIssuedCount = issuedCouponRepository.countByCouponIdAndUserId(couponId, userId);
        
        return new UserCouponStatus(
            userIssuedCount.intValue(),
            coupon.getMaxPerUser(),
            userIssuedCount < coupon.getMaxPerUser()
        );
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
    
    /**
     * 사용자별 쿠폰 발급 현황 클래스
     */
    public static class UserCouponStatus {
        private final int issuedCount;
        private final int maxPerUser;
        private final boolean canIssue;
        
        public UserCouponStatus(int issuedCount, int maxPerUser, boolean canIssue) {
            this.issuedCount = issuedCount;
            this.maxPerUser = maxPerUser;
            this.canIssue = canIssue;
        }
        
        public int getIssuedCount() { return issuedCount; }
        public int getMaxPerUser() { return maxPerUser; }
        public boolean isCanIssue() { return canIssue; }
    }
} 