#!/bin/bash

# 쿠폰 발급 시스템 동시성 테스트 스크립트

echo "🚀 쿠폰 발급 시스템 동시성 테스트 시작"
echo "=========================================="

# 설정
BASE_URL="http://localhost:8080"
COUPON_ID=1
CONCURRENT_USERS=100

echo "📊 테스트 설정:"
echo "- 서버 URL: $BASE_URL"
echo "- 쿠폰 ID: $COUPON_ID"
echo "- 동시 사용자 수: $CONCURRENT_USERS"
echo ""

# 서버 상태 확인
echo "🔍 서버 상태 확인 중..."
if curl -s "$BASE_URL/api/coupons" > /dev/null; then
    echo "✅ 서버가 정상적으로 실행 중입니다."
else
    echo "❌ 서버에 연결할 수 없습니다. 서버를 먼저 실행해주세요."
    exit 1
fi

echo ""

# 테스트 시작 시간 기록
START_TIME=$(date +%s)
echo "⏰ 테스트 시작 시간: $(date)"
echo ""

# 결과 파일 초기화
RESULT_FILE="test_results.txt"
> $RESULT_FILE

echo "🎯 $CONCURRENT_USERS 명의 사용자가 동시에 쿠폰 발급 요청을 보냅니다..."

# 동시 요청 실행
for ((i=1; i<=CONCURRENT_USERS; i++)); do
    {
        RESPONSE=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/api/coupons/$COUPON_ID/issue?userId=$i")
        HTTP_CODE=${RESPONSE: -3}
        BODY=${RESPONSE%???}
        echo "User $i: HTTP $HTTP_CODE - $BODY" >> $RESULT_FILE
    } &
done

# 모든 백그라운드 작업 완료 대기
wait

# 테스트 종료 시간 기록
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo ""
echo "⏰ 테스트 완료 시간: $(date)"
echo "🕐 총 소요 시간: ${DURATION}초"
echo ""

# 결과 분석
echo "📊 테스트 결과 분석:"
echo "===================="

SUCCESS_COUNT=$(grep -c '"success":true' $RESULT_FILE)
FAILURE_COUNT=$(grep -c '"success":false' $RESULT_FILE)
DUPLICATE_COUNT=$(grep -c "이미 발급받은 쿠폰" $RESULT_FILE)
EXHAUSTED_COUNT=$(grep -c "수량이 모두 소진" $RESULT_FILE)

echo "✅ 성공한 발급: $SUCCESS_COUNT 건"
echo "❌ 실패한 발급: $FAILURE_COUNT 건"
echo "🔄 중복 발급 시도: $DUPLICATE_COUNT 건"
echo "📦 수량 소진: $EXHAUSTED_COUNT 건"
echo ""

# 쿠폰 상태 확인
echo "🎟️ 최종 쿠폰 상태:"
COUPON_INFO=$(curl -s "$BASE_URL/api/coupons")
echo $COUPON_INFO | python3 -m json.tool 2>/dev/null || echo $COUPON_INFO

echo ""
echo "📄 상세 결과는 $RESULT_FILE 파일을 확인하세요."
echo ""
echo "🎉 테스트 완료!" 