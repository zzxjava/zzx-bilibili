package com.imooc.bilibili.domain.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;


/**
 * 自己开发一个注解，进行权限控制
 *
 * @Retention和@Target等元注解指定注解的作用范围。 使用@interface关键字，然后可以定义各种属性
 */
@Retention(RetentionPolicy.RUNTIME)//属性：是在运行时启动
@Target({ElementType.METHOD})// 目标：是放在方法上面
@Documented//
@Component
public @interface ApiLimitedRole {
    //定义一个属性，limitedRoleCodeList，是String数组，默认是空的
    String[] limitedRoleCodeList() default {};
}
