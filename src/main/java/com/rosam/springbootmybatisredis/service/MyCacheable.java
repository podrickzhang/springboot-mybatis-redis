package com.rosam.springbootmybatisredis.service;

import org.springframework.cache.annotation.Cacheable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
//@Cacheable(value = "user",keyGenerator = "myKeyGenerator")
@Cacheable(value = "users")
public @interface MyCacheable {
}
