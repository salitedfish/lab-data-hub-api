//package com.labdatahub.framework.security;
//
//import com.labdatahub.common.core.domain.AjaxResult;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@Aspect
//@Component
//public class TimeBasedAspect {
//
//    private static final LocalDateTime END_TIME = LocalDateTime.of(2026,2,20,0,0);
//
//    // 定义切点：所有Controller方法
//    @Pointcut("@within(org.springframework.stereotype.Controller) || " +
//            "@within(org.springframework.web.bind.annotation.RestController)")
//    public void controllerPointcut() {}
//
//    @Around("controllerPointcut()")
//    public Object checkAccessTime(ProceedingJoinPoint joinPoint) throws Throwable {
//
//        LocalDateTime now = LocalDateTime.now();
//
//        // 如果在维护时间外
//        if (now.isAfter(END_TIME)) {
//            throw new RuntimeException("服务不可用");
//        }
//
//        // 正常执行
//        return joinPoint.proceed();
//    }
//}