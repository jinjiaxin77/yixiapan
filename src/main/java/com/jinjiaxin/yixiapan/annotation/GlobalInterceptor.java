package com.jinjiaxin.yixiapan.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Mapping
public @interface GlobalInterceptor {

    /**
     * 校验参数
     *
     * @return
     */
    boolean checkParams() default false;

    /**
     * 校验登录
     *
     * @return
     */
    boolean checkLogin() default false;

    /**
     * 校验管理员
     *
     * @return
     */
    boolean checkAdmin() default false;

}
