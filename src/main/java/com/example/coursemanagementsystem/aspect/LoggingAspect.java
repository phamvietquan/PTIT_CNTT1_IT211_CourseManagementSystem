package com.example.coursemanagementsystem.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Pointcut bao phủ tất cả public methods trong service layer.
     */
    @Pointcut("execution(public * com.example.coursemanagementsystem.service.*.*(..))")
    public void serviceMethods() {}

    /**
     * Pointcut bao phủ tất cả public methods trong controller layer.
     */
    @Pointcut("execution(public * com.example.coursemanagementsystem.controller.*.*(..))")
    public void controllerMethods() {}

    /**
     * Around advice: đo và ghi log thời gian thực hiện của tất cả service methods.
     */
    @Around("serviceMethods()")
    public Object logServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecutionTime(joinPoint, "SERVICE");
    }

    /**
     * Around advice: đo và ghi log thời gian thực hiện của tất cả controller methods.
     */
    @Around("controllerMethods()")
    public Object logControllerExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecutionTime(joinPoint, "CONTROLLER");
    }

    /**
     * Phương thức dùng chung để đo thời gian và ghi log.
     *
     * @param joinPoint join point của method được gọi
     * @param layer     tên layer (SERVICE / CONTROLLER)
     * @return kết quả trả về của method gốc
     * @throws Throwable nếu method gốc ném exception
     */
    private Object logExecutionTime(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        long startTime = System.currentTimeMillis();
        Object result;

        try {
            result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[{}] [{}.{}] - executed successfully in {}ms",
                    layer, className, methodName, duration);
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("[{}] [{}.{}] - failed after {}ms | Exception: {}",
                    layer, className, methodName, duration, ex.getMessage());
            throw ex;
        }

        return result;
    }
}
