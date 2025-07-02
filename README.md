# 🎟️ 쿠폰 발급 시스템

선착순 한정 수량 쿠폰 발급 시스템 - **동시성 제어**와 **중복 방지**에 중점을 둔 실무형 백엔드 프로젝트

## 📌 주요 특징

### 🔐 핵심 동시성 기능
- ✅ **Pessimistic Lock을 활용한 동시성 제어**
- ✅ **사용자별 발급 수량 제어** - 1인당 최대 발급 수량 유연 관리
- ✅ **트랜잭션 기반 안전한 데이터 처리**
- ✅ **Redis 없이도 안전한 동시성 처리**

### 🎨 사용자 경험 (UX)
- ✅ **Toast 알림 시스템** - 성공/실패 즉각적 피드백
- ✅ **스마트 수량 업데이트** - 수동 새로고침 + 발급 성공 시 자동 갱신
- ✅ **사용자별 발급 제한** - 1인당 최대 발급 수량 제어
- ✅ **발급 현황 실시간 표시** - 개인별 발급 가능/불가 상태 UI
- ✅ **현대적이고 직관적인 웹 UI**

### 🛠️ 관리자 기능 (최근 개선!)
- ✅ **발급 내역 조회 최적화** - DTO 패턴으로 순환 참조 문제 해결
- ✅ **쿠폰 생성 및 관리** - 1인당 발급 제한 설정 포함
- ✅ **실시간 통계 대시보드**
- ✅ **안정적인 데이터 로딩** - JOIN FETCH로 성능 최적화

## 🛠️ 기술 스택

| 영역 | 기술 |
|------|------|
| 백엔드 | Spring Boot 3.2, JPA, MySQL |
| 프론트엔드 | HTML5, CSS3, JavaScript |
| 데이터베이스 | MySQL 8.0 |
| 빌드도구 | Maven |

## 🏗️ 프로젝트 구조

```
coupon_system/
├── src/main/java/com/example/coupon/
│   ├── CouponSystemApplication.java
│   ├── model/
│   │   ├── Coupon.java           # 쿠폰 엔티티
│   │   └── IssuedCoupon.java     # 발급내역 엔티티
│   ├── repository/
│   │   ├── CouponRepository.java       # 쿠폰 저장소
│   │   └── IssuedCouponRepository.java # 발급내역 저장소
│   ├── service/
│   │   └── CouponService.java    # 핵심 비즈니스 로직
│   ├── controller/
│   │   ├── CouponController.java # REST API
│   │   └── WebController.java    # 웹 페이지 라우팅
│   └── config/
│       └── DataInitializer.java  # 초기 데이터 설정
├── src/main/resources/
│   ├── templates/
│   │   ├── index.html            # 쿠폰 발급 페이지
│   │   └── admin.html            # 관리자 페이지
│   └── application.yml           # 애플리케이션 설정
├── pom.xml                      # Maven 빌드 설정
└── README.md
```

## 🚀 실행 방법

### 1. 사전 준비

- Java 17 이상
- MySQL 8.0 이상
- Git

### 2. MySQL 데이터베이스 설정

```sql
-- MySQL에 접속하여 데이터베이스 생성
CREATE DATABASE coupon_system;

-- 또는 application.yml에서 자동 생성됨 (createDatabaseIfNotExist=true)
```

### 3. 애플리케이션 실행

```bash
# 1. 프로젝트 클론
git clone <repository-url>
cd coupon_system

# 2. 애플리케이션 실행
mvn spring-boot:run

# 또는 IDE에서 CouponSystemApplication.java 실행
```

### 4. 접속 확인

- **메인 페이지**: http://localhost:8080
- **관리자 페이지**: http://localhost:8080/admin
- **API 문서**: http://localhost:8080/api/coupons

## 🧪 테스트 시나리오

### 🎨 UX 기능 테스트 (NEW!)

1. **Toast 알림 시스템 테스트**
   - 사용자 ID 입력 → 쿠폰 선택 → 발급 버튼 클릭
   - 성공 시 우상단 초록색 Toast 알림 확인
   - 실패 시 빨간색 Toast 알림 확인

2. **실시간 수량 업데이트 테스트**
   - 사용자 ID 입력 후 15초마다 자동 갱신 확인
   - 다른 브라우저/탭에서 발급 후 자동 반영 테스트
   - 진행률 바 실시간 업데이트 확인

3. **사용자별 발급 제한 테스트**
   - 동일 사용자로 maxPerUser 초과 발급 시도
   - "1인당 최대 X개까지만 발급 가능합니다. (현재 Y개 발급됨)" 메시지 확인
   - 발급 현황 UI 표시: "👤 발급 현황: 1/3개 ✅ 발급 가능"

### 🔐 기본 동시성 테스트

### 동시성 테스트

**Postman/cURL을 이용한 동시 요청 테스트:**

```bash
# 100명이 동시에 같은 쿠폰 발급 요청
for i in {1..100}; do
  curl -X POST "http://localhost:8080/api/coupons/1/issue?userId=$i" &
done
wait
```

## 🎯 핵심 구현 포인트

### 1. 동시성 제어 (Pessimistic Lock)

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM Coupon c WHERE c.id = :id")
Optional<Coupon> findByIdWithLock(@Param("id") Long id);
```

### 2. 중복 발급 방지 (개선됨!)

```java
// 기존: 완전 중복 방지 (1인 1쿠폰)
@Table(name = "issued_coupon", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"coupon_id", "user_id"}))

// 현재: 수량 기반 유연한 제한 (1인당 N개까지)
@Table(name = "issued_coupon")  // UniqueConstraint 제거
```

**개선 사유**: 1인당 여러 개 쿠폰 발급 가능 (예: 대박 이벤트 쿠폰 3개까지)

### 3. 사용자별 발급 제한 (NEW!)

```java
@Transactional
public CouponIssueResult issueCoupon(Long couponId, Long userId) {
    // 1. Pessimistic Lock으로 쿠폰 조회
    Coupon coupon = couponRepository.findByIdWithLock(couponId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));
    
    // 2. 사용자별 발급 수량 확인 (NEW!)
    Long userIssuedCount = issuedCouponRepository.countByCouponIdAndUserId(couponId, userId);
    if (userIssuedCount >= coupon.getMaxPerUser()) {
        return new CouponIssueResult(false, 
            String.format("1인당 최대 %d개까지만 발급 가능합니다.", coupon.getMaxPerUser()));
    }
    
    // 3. 원자적 처리 보장
    coupon.increaseIssuedCount();
    // ... 발급 처리
}
```

### 4. 관리자 페이지 최적화 (NEW!)

```java
// DTO 패턴으로 순환 참조 방지
public record IssuedCouponDto(
    Long id,
    Long userId,
    CouponSummaryDto coupon,
    LocalDateTime issuedAt
) {}

// JOIN FETCH로 N+1 문제 해결
@Query("SELECT ic FROM IssuedCoupon ic JOIN FETCH ic.coupon WHERE ic.coupon.id = :couponId")
List<IssuedCoupon> findByCouponId(@Param("couponId") Long couponId);
```

### 5. 데이터베이스 설계

```sql
-- 쿠폰 테이블 (개선됨)
CREATE TABLE coupon (
  id BIGINT PRIMARY KEY,
  name VARCHAR(100),
  total_count INT,
  issued_count INT DEFAULT 0,
  max_per_user INT NOT NULL DEFAULT 1,  -- NEW: 1인당 최대 발급 수량
  start_at TIMESTAMP,
  end_at TIMESTAMP
);

-- 발급 내역 테이블 (수량 기반 제한)
CREATE TABLE issued_coupon (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  coupon_id BIGINT,
  user_id BIGINT,
  issued_at TIMESTAMP
  -- 수량 기반 제한으로 완전 중복 방지 제약조건은 제거
);
```

## 📊 API 명세

### 🎟️ 쿠폰 발급
```http
POST /api/coupons/{couponId}/issue?userId={userId}
Response: {"success": true, "message": "쿠폰이 성공적으로 발급되었습니다! (1/3개 발급됨)"}
```

### 📋 쿠폰 목록 조회
```http
GET /api/coupons              # 전체 쿠폰
GET /api/coupons/active       # 활성 쿠폰
GET /api/coupons/{couponId}   # 특정 쿠폰 조회 (실시간 업데이트용)
```

### 👤 사용자별 발급 현황 (NEW!)
```http
GET /api/coupons/{couponId}/user/{userId}/status
Response: {
  "issuedCount": 1,
  "maxPerUser": 3,
  "canIssue": true
}
```

### 📄 발급 내역 조회 (개선됨!)
```http
GET /api/coupons/{couponId}/issued    # 특정 쿠폰 발급 내역 (DTO 최적화)
GET /api/coupons/user/{userId}        # 사용자별 발급 내역

# 응답 예시 (순환 참조 방지)
{
  "id": 1,
  "userId": 123,
  "coupon": {
    "id": 1,
    "name": "신규회원 할인 쿠폰"
  },
  "issuedAt": "2024-01-15T10:30:00"
}
```

## 🔍 성능 및 안정성

### 예상 처리 성능
- **단일 요청**: ~50ms
- **동시 100건 요청**: 안전한 순차 처리
- **데이터 정합성**: 100% 보장

### 확장 가능성
- 캐시 레이어 추가 (Redis)
- Named Lock 적용
- 비동기 처리 (이벤트 기반)
- 마이크로서비스 분리

## 🎨 UI 특징

### 🎯 메인 페이지 (사용성 개선!)
- **🎉 Toast 알림 시스템**: 발급 성공/실패 즉각적 피드백
- **🔄 스마트 업데이트**: 수동 새로고침 + 발급 성공 시 자동 갱신
- **👤 사용자별 발급 현황**: "발급 현황: 1/3개 ✅ 발급 가능" 표시
- **📊 진행률 바**: 실시간 수량 변화 시각화
- **📱 반응형 디자인**: 모든 디바이스 대응

### 🛠️ 관리자 페이지 (최근 대폭 개선!)
- **📊 실시간 통계 대시보드**: 한눈에 보는 시스템 현황
- **➕ 쿠폰 생성/관리**: 1인당 발급 제한 설정 포함
- **📄 발급 내역 조회**: DTO 패턴으로 순환 참조 해결, 500 에러 완전 제거
- **⚡ 성능 최적화**: JOIN FETCH로 N+1 문제 해결
- **🎨 직관적인 UI**: 탭 기반 네비게이션

### 🚀 기술적 특징
- **Toastr.js**: 세련된 Toast 알림
- **AJAX 폴링**: 무중단 실시간 업데이트
- **jQuery**: 효율적 DOM 조작
- **CDN 라이브러리**: 빠른 로딩

## 📈 확장 아이디어

### ✅ 구현 완료된 기능
- ✅ **Toast 알림 시스템** - 즉각적 사용자 피드백
- ✅ **실시간 수량 업데이트** - AJAX 폴링 기반 자동 갱신
- ✅ **사용자별 발급 제한** - 1인당 최대 발급 수량 제어
- ✅ **발급 현황 UI 표시** - 개인별 발급 가능/불가 상태

### 🚀 향후 확장 계획
- 📧 발급 성공 알림 (이메일/SMS)
- ⏰ 스케줄러 기반 만료 쿠폰 정리
- 📊 고급 통계 및 분석 기능
- 🔐 사용자 인증 시스템 연동
- 🎁 쿠폰 사용 처리 기능
- 💬 WebSocket 실시간 통신 (현재 AJAX 폴링)
- 🎯 Redis 캐시 레이어 추가

## 🔧 최근 업데이트 (NEW!)

### 📈 2024년 최신 개선사항

#### 🐛 **관리자 페이지 발급 내역 문제 해결**
- **문제**: 500 에러 및 순환 참조로 발급 내역 조회 불가
- **해결**: DTO 패턴 도입 + JOIN FETCH 최적화
- **결과**: 안정적인 발급 내역 조회 및 성능 향상

#### 🔓 **사용자별 발급 제한 유연화**
- **문제**: UniqueConstraint로 1인당 1개만 발급 가능
- **해결**: 제약조건 제거 + Service 로직으로 수량 제어
- **결과**: 1인당 N개까지 유연한 발급 가능 (예: 대박 이벤트 쿠폰 3개)

#### 🎨 **사용성 대폭 개선**
- **변경**: 자동 새로고침(15초) → 수동 새로고침 + 스마트 업데이트
- **추가**: 발급 성공 시 자동 상태 갱신
- **추가**: 탭 전환 시 스마트 업데이트
- **결과**: 사용자 제어권 확보 + 불필요한 서버 요청 최소화

#### 🏗️ **아키텍처 최적화**
- **DTO 패턴**: 순환 참조 방지 및 JSON 직렬화 최적화
- **JOIN FETCH**: LAZY 로딩 문제 해결 및 N+1 쿼리 방지
- **트랜잭션 최적화**: 데이터 정합성 보장 강화

## 💡 포트폴리오 어필 포인트

### 🔥 핵심 강점
1. **실무 문제 해결**: 실제 서비스에서 발생하는 동시성 이슈를 안전하게 처리
2. **기술적 깊이**: Pessimistic Lock, 트랜잭션, 제약조건 등 백엔드 핵심 기술 활용
3. **사용자 경험**: Toast 알림, 실시간 업데이트 등 현대적 UX 구현
4. **완성도**: 백엔드부터 프론트엔드, 테스트까지 완전한 시스템
5. **확장성**: Redis 없이도 안전한 처리 + 다양한 개선 방향 제시

### 🎯 차별화 포인트
- **Redis 의존성 없음**: DB 기반만으로 안전한 동시성 처리 구현
- **유연한 발급 제한**: 쿠폰별 1인당 발급 수량 개별 설정 가능
- **실시간 UX**: 스마트 업데이트로 사용자 친화적 인터페이스 제공
- **비즈니스 로직**: 1인당 발급 제한 등 실제 비즈니스 요구사항 반영
- **테스트 가능**: 동시성 테스트 스크립트 및 다양한 시나리오 제공
- **아키텍처 완성도**: DTO 패턴, JOIN FETCH 등 실무 최적화 기법 적용

**🚀 이 프로젝트는 동시성 처리가 중요한 쿠폰/예약/좌석 시스템 등의 실무 경험을 보여주는 완성도 높은 포트폴리오입니다!** 