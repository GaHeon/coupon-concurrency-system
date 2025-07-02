package com.example.coupon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    
    /**
     * 메인 페이지 - 쿠폰 발급 페이지
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    /**
     * 관리자 페이지 - 발급 내역 조회
     */
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
} 