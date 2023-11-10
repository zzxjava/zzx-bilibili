package com.imooc.bilibili.api.aspect;

import com.imooc.bilibili.api.support.UserSupport;
import com.imooc.bilibili.domain.annotation.ApiLimitedRole;
import com.imooc.bilibili.domain.auth.UserRole;
import com.imooc.bilibili.domain.exception.ConditionException;
import com.imooc.bilibili.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 这是一个切面类，用于检查ApiLimitedRole注解标记的方法，以确保只有指定角色的用户才能访问该方法。
 */
@Order(1)//优先级的设计
@Component
@Aspect//切面类
public class ApiLimitedRoleAspect {

    /*获取用户id*/
    @Autowired
    private UserSupport userSupport;

    /*获取用户角色*/
    @Autowired
    private UserRoleService userRoleService;

    /**
     * 切点的切入位置：com.imooc.bilibili.domain.annotation.ApiLimitedRole，当自定义注解被识别的时候开始切入
     */
    @Pointcut("@annotation(com.imooc.bilibili.domain.annotation.ApiLimitedRole)")
    public void check() {
    }

    /**
     * 切入切点后的处理逻辑
     */
    @Before("check() && @annotation(apiLimitedRole)")
    public void doBefore(JoinPoint joinPoint, ApiLimitedRole apiLimitedRole) {
        //获取用户ID
        Long userId = userSupport.getCurrentUserId();
        //获取用户对应的角色列表
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        /*希望哪些用户角色被限制*/
        String[] limitedRoleCodeList = apiLimitedRole.limitedRoleCodeList();
        /*我们只需要比对 */
        //将限制的角色列表编码取出来
        Set<String> limitedRoleCodeSet = Arrays.stream(limitedRoleCodeList).collect(Collectors.toSet());
        /*从用户角色关联的列表中取出角色编码*/
        Set<String> roleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        roleCodeSet.retainAll(limitedRoleCodeSet);
        if (roleCodeSet.size() > 0) {
            throw new ConditionException("权限不足！");
        }
    }
}
