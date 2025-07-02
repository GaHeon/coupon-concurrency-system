package com.example.coupon.config;

import com.example.coupon.model.Coupon;
import com.example.coupon.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final CouponRepository couponRepository;
    
    @Autowired
    public DataInitializer(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // 기존 데이터가 있으면 초기화하지 않음
        if (couponRepository.count() > 0) {
            return;
        }
        
        // 테스트용 쿠폰 데이터 생성
        createTestCoupons();
    }
    
    private void createTestCoupons() {
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 현재 진행 중인 쿠폰 (100장 한정, 1인당 1개)
        Coupon coupon1 = new Coupon(
            "🎉 신규회원 할인 쿠폰",
            100,
            now.minusHours(1), // 1시간 전 시작
            now.plusDays(7),   // 7일 후 종료
            1                  // 1인당 최대 1개
        );
        
        // 2. 곧 시작되는 쿠폰 (50장 한정, 1인당 2개)
        Coupon coupon2 = new Coupon(
            "💰 특가 세일 쿠폰",
            50,
            now.plusHours(1),  // 1시간 후 시작
            now.plusDays(3),   // 3일 후 종료
            2                  // 1인당 최대 2개
        );
        
        // 3. 대용량 쿠폰 (1000장 한정, 1인당 3개) - 동시성 테스트용
        Coupon coupon3 = new Coupon(
            "🚀 대박 이벤트 쿠폰",
            1000,
            now.minusMinutes(30), // 30분 전 시작
            now.plusDays(1),      // 1일 후 종료
            3                     // 1인당 최대 3개
        );
        
        // 4. 소량 쿠폰 (10장 한정, 1인당 1개) - 빠른 소진 테스트용
        Coupon coupon4 = new Coupon(
            "⚡ 한정판 VIP 쿠폰",
            10,
            now.minusMinutes(10), // 10분 전 시작
            now.plusDays(2),      // 2일 후 종료
            1                     // 1인당 최대 1개 (VIP는 1개 제한)
        );
        
        couponRepository.save(coupon1);
        couponRepository.save(coupon2);
        couponRepository.save(coupon3);
        couponRepository.save(coupon4);
        
        System.out.println("✅ 테스트용 쿠폰 데이터가 초기화되었습니다.");
        System.out.println("   - 신규회원 할인 쿠폰: 100장 (1인당 1개)");
        System.out.println("   - 특가 세일 쿠폰: 50장 (1인당 2개, 1시간 후 시작)");
        System.out.println("   - 대박 이벤트 쿠폰: 1000장 (1인당 3개)");
        System.out.println("   - VIP 쿠폰: 10장 (1인당 1개)");
    }
} 