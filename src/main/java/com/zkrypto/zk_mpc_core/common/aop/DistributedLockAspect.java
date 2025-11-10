package com.zkrypto.zk_mpc_core.common.aop;

import com.zkrypto.zk_mpc_core.common.annotation.PreventDuplicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {
    private final RedissonClient redissonClient;
    private final ParameterNameDiscoverer parameterNameDiscoverer = new StandardReflectionParameterNameDiscoverer();
    private final String MESSAGE_DTO = "errorMessage";
    private final String SESSION_ID = "sessionId";

    @Around("@annotation(preventDuplicate)")
    public Object handleLock(ProceedingJoinPoint joinPoint, PreventDuplicate preventDuplicate) throws Throwable {
        String dynamicKey = findSessionIdValue(joinPoint);
        String lockKey = "lock:restart:mpc:" + dynamicKey;

        RLock lock = redissonClient.getLock(lockKey);

        long waitTime = preventDuplicate.waitTime();
        long leaseTime = preventDuplicate.leaseTime();
        TimeUnit timeUnit = preventDuplicate.timeUnit();

        try {
            // 락 획득 시도
            boolean lockAcquired = lock.tryLock(waitTime, leaseTime, timeUnit);

            if (lockAcquired) {
                // 락 획득 성공 -> 원본 메서드(비즈니스 로직) 실행
                log.info("[AOP] {} 락 획득 성공.", dynamicKey);
                return joinPoint.proceed();
            } else {
                // 락 획득 실패
                log.info("[AOP] {} 락 획득 실패.", dynamicKey);
                return null;
            }
        } catch (InterruptedException e) {
            log.error("[AOP] {} 락 획득 실패.", dynamicKey, e);
            Thread.currentThread().interrupt();
            throw e;
        } catch (IllegalStateException e) {
            log.error("[AOP] {} 오류.", dynamicKey, e);
            throw e;
        }
    }

    private String findSessionIdValue(ProceedingJoinPoint joinPoint) throws IllegalStateException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = parameterNameDiscoverer.getParameterNames(signature.getMethod());
        Object[] args = joinPoint.getArgs();

        if (paramNames == null) {
            throw new IllegalStateException("메서드의 파라미터가 없습니다." + signature.getName());
        }

        for (int i = 0; i < paramNames.length; i++) {
            if (MESSAGE_DTO.equals(paramNames[i])) {
                Object dto = args[i];
                if (dto == null) {
                    throw new IllegalStateException(MESSAGE_DTO + "파라미터의 값이 없습니다.");
                }

                try {
                    Method method = dto.getClass().getMethod(SESSION_ID);
                    Object value = method.invoke(dto);
                    return value.toString();
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException(MESSAGE_DTO + "파라미터의 메서드 실행 오류");
                }
            }
        }

        throw new IllegalStateException(MESSAGE_DTO + "파라미터가 없습니다.");
    }

}
