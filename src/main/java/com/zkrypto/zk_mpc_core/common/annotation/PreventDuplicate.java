package com.zkrypto.zk_mpc_core.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreventDuplicate {
    long leaseTime() default 60; // 기본 60초
    long waitTime() default 0; // 기본 0초 (기다리지 않음)
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
