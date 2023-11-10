package com.imooc.bilibili.api.support;

import com.imooc.bilibili.domain.exception.ConditionException;
import com.imooc.bilibili.service.UserService;
import com.imooc.bilibili.service.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 从请求头中获取Token信息，然后验证Token，获取用户ID，
 */
@Component
public class UserSupport {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户的ID
     *
     * @return
     */
    public Long getCurrentUserId() {
        /*抓取请求上下文的方法----获取前端请求封装好的信息*/
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //从请求头中获取Token,返回给HttpServletRequest实体类
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader("token");//从请求头中获取token信息
        Long userId = TokenUtil.verifyToken(token);//验证token
        if (userId < 0) {
            throw new ConditionException("非法用户");
        }
//        this.verifyRefreshToken(userId);
        return userId;
    }

    //验证刷新令牌
    private void verifyRefreshToken(Long userId) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String refreshToken = requestAttributes.getRequest().getHeader("refreshToken");
        String dbRefreshToken = userService.getRefreshTokenByUserId(userId);
        if (!dbRefreshToken.equals(refreshToken)) {
            throw new ConditionException("非法用户！");
        }
    }


}
