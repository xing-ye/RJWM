package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局的异常处理器，这要求对于报错的字段内容有清楚的认识
 * ControllerAdvice用于表示对controller的代理
 * 通过下面的设置，所有注解为RestController和Controller控制器都会被拦截
 * ResponseBody表示返回json
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获并处理 SQLIntegrityConstraintViolationException 异常
     * 这里只是要处理上述异常中的形式如下面的一个异常
     * Duplicate entry 'zhangsan' for key 'idx_username'
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());

        //Duplicate entry代表双重输入错误，即为了唯一约束
        if (ex.getMessage().contains("Duplicate entry")){
            String[] split=ex.getMessage().split(" ");
           String msg= split[2]+"已存在";//获取出重复字段
           return R.error(msg);
        }
        return R.error("未知错误");
    }

    /**
     * 捕获并处理自定义的CustomException异常
     * 该异常只是包含一个
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());
        // 这样做是为了可以将异常报到前端页面
        return R.error(ex.getMessage());
    }

}
