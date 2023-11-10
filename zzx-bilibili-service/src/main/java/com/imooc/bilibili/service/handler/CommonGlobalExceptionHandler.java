package com.imooc.bilibili.service.handler;


import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.exception.ConditionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**全局异常处理器*/
@ControllerAdvice
//说明优先级是最高的
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonGlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    /**
     * 通用异常处理器
     */
    public JsonResponse<String> commonExceptionHandler(HttpServletRequest request, Exception e) {
        String errorMsg = e.getMessage();
        //如果这里截取到的异常是我们自定义的异常类ConditionException，需要把code值也输出
        if (e instanceof ConditionException) {
            /*
                instanceof是Java中的一个关键字，它用于判断一个对象是否是某个类的实例
                * 也就是判断e,是不是ConditionException类的实例，也就是说ConditionException能不能new除e这个对象来
            * */
            //获取到异常错误状态码
            String errorCode = ((ConditionException) e).getCode();
            return new JsonResponse<>(errorCode, errorMsg);
        } else {
            return new JsonResponse<>("500", errorMsg);
        }
    }
}
