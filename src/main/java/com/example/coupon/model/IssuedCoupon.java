package com.example.coupon.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "issued_coupon", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"coupon_id", "user_id"}))
public class IssuedCoupon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "issued_at")
    private LocalDateTime issuedAt;
    
    @PrePersist
    protected void onCreate() {
        this.issuedAt = LocalDateTime.now();
    }
    
    // 기본 생성자
    public IssuedCoupon() {}
    
    // 생성자
    public IssuedCoupon(Coupon coupon, Long userId) {
        this.coupon = coupon;
        this.userId = userId;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
} 