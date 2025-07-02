package com.example.coupon.repository;

import com.example.coupon.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    // Pessimistic Lock을 사용한 쿠폰 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    Optional<Coupon> findByIdWithLock(@Param("id") Long id);
    
    // 활성화된 쿠폰 조회 (발급 가능한 쿠폰)
    @Query("SELECT c FROM Coupon c WHERE c.startAt <= CURRENT_TIMESTAMP AND c.endAt > CURRENT_TIMESTAMP")
    java.util.List<Coupon> findActiveCoupons();
} 