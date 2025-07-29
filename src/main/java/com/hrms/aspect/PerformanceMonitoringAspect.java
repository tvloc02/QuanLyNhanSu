package com.hrms.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceMonitoringAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringAspect.class);

    @Around("execution(* com.hrms.service.EmployeeService.updateEmployeeSalary(..))")
    public Object monitorSalaryUpdateTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        logger.info("Bắt đầu cập nhật lương cho nhân viên...");

        try {
            // Thực hiện method gốc
            Object result = joinPoint.proceed();

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            logger.info("Cập nhật lương thành công! Thời gian xử lý: {} ms", executionTime);

            // Log chi tiết nếu thời gian xử lý quá lâu
            if (executionTime > 1000) {
                logger.warn("Cảnh báo: Thời gian cập nhật lương quá lâu: {} ms", executionTime);
            }

            return result;

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            logger.error("Lỗi khi cập nhật lương! Thời gian xử lý: {} ms, Lỗi: {}",
                    executionTime, e.getMessage());
            throw e;
        }
    }
}