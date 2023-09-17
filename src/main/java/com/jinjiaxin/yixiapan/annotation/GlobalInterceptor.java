package com.jinjiaxin.yixiapan.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalInterceptor {

    /**
     * 校验参数
     *
     * @return
     */
    boolean checkParams() default false;

}
