package com.example.coupon.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
public class Coupon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "total_count", nullable = false)
    private Integer totalCount;
    
    @Column(name = "issued_count", nullable = false)
    private Integer issuedCount = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "start_at")
    private LocalDateTime startAt;
    
    @Column(name = "end_at")
    private LocalDateTime endAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    // 기본 생성자
    public Coupon() {}
    
    // 생성자
    public Coupon(String name, Integer totalCount, LocalDateTime startAt, LocalDateTime endAt) {
        this.name = name;
        this.totalCount = totalCount;
        this.startAt = startAt;
        this.endAt = endAt;
        this.issuedCount = 0;
    }
    
    // 쿠폰 발급 가능 여부 확인
    public boolean canIssue() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startAt) && now.isBefore(endAt) && issuedCount < totalCount;
    }
    
    // 쿠폰 발급 수량 증가
    public void increaseIssuedCount() {
        if (canIssue()) {
            this.issuedCount++;
        } else {
            throw new IllegalStateException("쿠폰을 발급할 수 없습니다.");
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    
    public Integer getIssuedCount() { return issuedCount; }
    public void setIssuedCount(Integer issuedCount) { this.issuedCount = issuedCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
} 