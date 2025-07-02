# 🎟️ 쿠폰 발급 시스템

선착순 한정 수량 쿠폰 발급 시스템 - **동시성 제어**와 **중복 방지**에 중점을 둔 실무형 백엔드 프로젝트

## 📌 주요 특징

- ✅ **Pessimistic Lock을 활용한 동시성 제어**
- ✅ **유니크 제약조건으로 중복 발급 방지**
- ✅ **트랜잭션 기반 안전한 데이터 처리**
- ✅ **Redis 없이도 안전한 동시성 처리**
- ✅ **실시간 쿠폰 수량 관리**
- ✅ **직관적인 웹 UI 제공**

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

### 기본 기능 테스트

1. **쿠폰 발급 테스트**
   - 사용자 ID 입력 후 쿠폰 선택
   - "쿠폰 발급받기" 버튼 클릭
   - 성공 메시지 확인

2. **중복 발급 방지 테스트**
   - 동일 사용자로 같은 쿠폰 재발급 시도
   - "이미 발급받은 쿠폰입니다" 메시지 확인

3. **수량 제한 테스트**
   - 10장 한정 VIP 쿠폰으로 테스트
   - 11번째 발급 시도 시 실패 확인

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

### 2. 중복 발급 방지 (Unique Constraint)

```java
@Table(name = "issued_coupon", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"coupon_id", "user_id"}))
```

### 3. 트랜잭션 안전성

```java
@Transactional
public CouponIssueResult issueCoupon(Long couponId, Long userId) {
    // 원자적 처리 보장
}
```

## 📊 API 명세

### 쿠폰 발급
- **POST** `/api/coupons/{couponId}/issue?userId={userId}`
- **Response**: `{"success": true, "message": "쿠폰이 성공적으로 발급되었습니다."}`

### 쿠폰 목록 조회
- **GET** `/api/coupons` - 전체 쿠폰
- **GET** `/api/coupons/active` - 활성 쿠폰

### 발급 내역 조회
- **GET** `/api/coupons/{couponId}/issued` - 특정 쿠폰 발급 내역
- **GET** `/api/coupons/user/{userId}` - 사용자별 발급 내역

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

## 🎨 UI 미리보기

### 메인 페이지
- 현대적이고 직관적인 쿠폰 발급 인터페이스
- 실시간 수량 표시 및 진행률 바
- 반응형 디자인

### 관리자 페이지
- 쿠폰 생성/관리 기능
- 발급 내역 및 통계 대시보드
- 실시간 데이터 조회

## 📈 확장 아이디어

- 📧 발급 성공 알림 (이메일/SMS)
- ⏰ 스케줄러 기반 만료 쿠폰 정리
- 📊 고급 통계 및 분석 기능
- 🔐 사용자 인증 시스템 연동
- 🎁 쿠폰 사용 처리 기능

---

**💡 이 프로젝트는 실무에서 자주 발생하는 동시성 문제를 안전하게 해결하는 방법을 보여주는 포트폴리오용 프로젝트입니다.** 