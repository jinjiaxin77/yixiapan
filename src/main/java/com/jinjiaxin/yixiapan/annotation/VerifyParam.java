package com.jinjiaxin.yixiapan.annotation;

import com.jinjiaxin.yixiapan.entity.enums.VerifyRegexEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.FIELD})
public @interface VerifyParam {

    boolean required() default false;

    int max() default -1;

    int min() default -1;

    VerifyRegexEnum regex() default VerifyRegexEnum.NO;

}
