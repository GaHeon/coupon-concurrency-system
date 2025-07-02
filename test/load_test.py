#!/usr/bin/env python3
"""
쿠폰 발급 시스템 부하 테스트
동시성 제어와 중복 방지 기능을 검증하는 테스트 스크립트
"""

import asyncio
import aiohttp
import time
import json
from typing import List, Dict
import argparse

class CouponLoadTester:
    def __init__(self, base_url: str = "http://localhost:8080"):
        self.base_url = base_url
        self.results = []
        
    async def issue_coupon(self, session: aiohttp.ClientSession, coupon_id: int, user_id: int) -> Dict:
        """단일 쿠폰 발급 요청"""
        url = f"{self.base_url}/api/coupons/{coupon_id}/issue"
        params = {"userId": user_id}
        
        start_time = time.time()
        try:
            async with session.post(url, params=params) as response:
                response_time = time.time() - start_time
                result = await response.json()
                
                return {
                    "user_id": user_id,
                    "status_code": response.status,
                    "response_time": response_time,
                    "success": result.get("success", False),
                    "message": result.get("message", ""),
                    "timestamp": time.time()
                }
        except Exception as e:
            return {
                "user_id": user_id,
                "status_code": 0,
                "response_time": time.time() - start_time,
                "success": False,
                "message": f"Request failed: {str(e)}",
                "timestamp": time.time()
            }
    
    async def run_concurrent_test(self, coupon_id: int, user_count: int) -> List[Dict]:
        """동시 요청 테스트 실행"""
        print(f"🚀 {user_count}명의 사용자가 동시에 쿠폰 {coupon_id} 발급 요청을 시작합니다...")
        
        connector = aiohttp.TCPConnector(limit=user_count)
        timeout = aiohttp.ClientTimeout(total=30)
        
        async with aiohttp.ClientSession(connector=connector, timeout=timeout) as session:
            # 모든 요청을 동시에 실행
            tasks = [
                self.issue_coupon(session, coupon_id, user_id) 
                for user_id in range(1, user_count + 1)
            ]
            
            start_time = time.time()
            results = await asyncio.gather(*tasks)
            total_time = time.time() - start_time
            
            print(f"⏰ 총 소요 시간: {total_time:.2f}초")
            return results
    
    async def get_coupon_info(self) -> Dict:
        """쿠폰 정보 조회"""
        async with aiohttp.ClientSession() as session:
            async with session.get(f"{self.base_url}/api/coupons") as response:
                return await response.json()
    
    def analyze_results(self, results: List[Dict]) -> None:
        """결과 분석 및 출력"""
        total_requests = len(results)
        successful_issues = sum(1 for r in results if r["success"])
        failed_issues = total_requests - successful_issues
        
        # 응답 시간 통계
        response_times = [r["response_time"] for r in results]
        avg_response_time = sum(response_times) / len(response_times)
        max_response_time = max(response_times)
        min_response_time = min(response_times)
        
        # 에러 메시지 분류
        error_messages = {}
        for result in results:
            if not result["success"]:
                msg = result["message"]
                error_messages[msg] = error_messages.get(msg, 0) + 1
        
        print("\n📊 테스트 결과 분석")
        print("=" * 50)
        print(f"📈 총 요청 수: {total_requests}")
        print(f"✅ 성공한 발급: {successful_issues}")
        print(f"❌ 실패한 발급: {failed_issues}")
        print(f"📊 성공률: {(successful_issues/total_requests)*100:.1f}%")
        print()
        
        print("⏱️ 응답 시간 통계:")
        print(f"   평균: {avg_response_time:.3f}초")
        print(f"   최대: {max_response_time:.3f}초")
        print(f"   최소: {min_response_time:.3f}초")
        print()
        
        if error_messages:
            print("🔍 실패 사유 분석:")
            for message, count in error_messages.items():
                print(f"   - {message}: {count}건")
        print()
    
    async def check_server_status(self) -> bool:
        """서버 상태 확인"""
        try:
            async with aiohttp.ClientSession() as session:
                async with session.get(f"{self.base_url}/api/coupons") as response:
                    return response.status == 200
        except:
            return False

async def main():
    parser = argparse.ArgumentParser(description="쿠폰 발급 시스템 부하 테스트")
    parser.add_argument("--url", default="http://localhost:8080", help="서버 URL")
    parser.add_argument("--coupon-id", type=int, default=1, help="테스트할 쿠폰 ID")
    parser.add_argument("--users", type=int, default=100, help="동시 사용자 수")
    parser.add_argument("--verbose", action="store_true", help="상세 출력")
    
    args = parser.parse_args()
    
    tester = CouponLoadTester(args.url)
    
    print("🎟️ 쿠폰 발급 시스템 부하 테스트")
    print("=" * 50)
    print(f"🌐 서버: {args.url}")
    print(f"🎫 쿠폰 ID: {args.coupon_id}")
    print(f"👥 동시 사용자: {args.users}")
    print()
    
    # 서버 상태 확인
    print("🔍 서버 상태 확인 중...")
    if not await tester.check_server_status():
        print("❌ 서버에 연결할 수 없습니다. 서버를 먼저 실행해주세요.")
        return
    
    print("✅ 서버가 정상적으로 실행 중입니다.")
    print()
    
    # 테스트 전 쿠폰 상태 확인
    print("📋 테스트 전 쿠폰 상태:")
    try:
        coupon_info = await tester.get_coupon_info()
        if args.verbose:
            print(json.dumps(coupon_info, indent=2, ensure_ascii=False))
        else:
            for coupon in coupon_info:
                if coupon["id"] == args.coupon_id:
                    print(f"   쿠폰: {coupon['name']}")
                    print(f"   발급 수량: {coupon['issuedCount']}/{coupon['totalCount']}")
                    break
    except Exception as e:
        print(f"⚠️ 쿠폰 정보 조회 실패: {e}")
    
    print()
    
    # 부하 테스트 실행
    start_time = time.time()
    results = await tester.run_concurrent_test(args.coupon_id, args.users)
    
    # 결과 분석
    tester.analyze_results(results)
    
    # 테스트 후 쿠폰 상태 확인
    print("📋 테스트 후 쿠폰 상태:")
    try:
        coupon_info = await tester.get_coupon_info()
        for coupon in coupon_info:
            if coupon["id"] == args.coupon_id:
                print(f"   쿠폰: {coupon['name']}")
                print(f"   발급 수량: {coupon['issuedCount']}/{coupon['totalCount']}")
                break
    except Exception as e:
        print(f"⚠️ 쿠폰 정보 조회 실패: {e}")
    
    print("\n🎉 테스트 완료!")
    
    # 상세 결과 저장
    if args.verbose:
        with open("detailed_results.json", "w", encoding="utf-8") as f:
            json.dump(results, f, indent=2, ensure_ascii=False)
        print("📄 상세 결과가 detailed_results.json에 저장되었습니다.")

if __name__ == "__main__":
    asyncio.run(main()) 