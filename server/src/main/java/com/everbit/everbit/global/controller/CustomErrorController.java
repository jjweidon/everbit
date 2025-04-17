package com.everbit.everbit.global.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 정의 오류 컨트롤러
 * Spring Boot의 기본 WhiteLabel 오류 페이지 대신 JSON 응답을 반환합니다.
 */
@Slf4j
@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    /**
     * 모든 오류 요청을 처리합니다.
     */
    @RequestMapping("/error")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Map<String, Object> errorInfo = new HashMap<>();
        
        // 오류 상태 코드 가져오기
        HttpStatus status = getStatus(request);
        errorInfo.put("status", status.value());
        errorInfo.put("error", status.getReasonPhrase());
        
        // 요청 URL 정보 추가
        errorInfo.put("path", request.getRequestURI());
        
        // 오류 메시지 추가
        String message = getErrorMessage(request);
        errorInfo.put("message", message);
        
        return ResponseEntity.status(status).body(errorInfo);
    }
    
    /**
     * 오류 상태 코드를 가져옵니다.
     */
    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            return HttpStatus.valueOf(statusCode);
        } catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
    
    /**
     * 오류 메시지를 가져옵니다.
     */
    private String getErrorMessage(HttpServletRequest request) {
        Throwable error = (Throwable) request.getAttribute("javax.servlet.error.exception");
        if (error != null) {
            while (error.getCause() != null) {
                error = error.getCause();
            }
            return error.getMessage();
        }
        
        String message = (String) request.getAttribute("javax.servlet.error.message");
        return message != null ? message : "알 수 없는 오류가 발생했습니다.";
    }
} 