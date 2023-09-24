package com.jinjiaxin.yixiapan.aspect;

import com.jinjiaxin.yixiapan.annotation.GlobalInterceptor;
import com.jinjiaxin.yixiapan.annotation.VerifyParam;
import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.enums.ResponseCodeEnum;
import com.jinjiaxin.yixiapan.entity.enums.VerifyRegexEnum;
import com.jinjiaxin.yixiapan.exception.BusinessException;
import com.jinjiaxin.yixiapan.utils.StringTools;
import com.jinjiaxin.yixiapan.utils.VerifyUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author jjx
 * @Description
 * @create 2023/9/17 13:54
 */

@Aspect
@Component
@Slf4j
public class GlobalOperationAspect {

    @Pointcut("@annotation(com.jinjiaxin.yixiapan.annotation.GlobalInterceptor)")
    private void requestInterceptor(){ }

    @Before("requestInterceptor()")
    public Object InterceptorDo (JoinPoint point) throws BusinessException{
        try{
            Object target = point.getTarget();
            Object[] arguments = point.getArgs();
            String methodName = point.getSignature().getName();
            Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();
            Method method = target.getClass().getMethod(methodName,parameterTypes);
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);

            if(interceptor == null) return null;

            if(interceptor.checkParams()){
                validateParams(method,arguments);
            }

            return null;
        }catch (BusinessException e){
            log.error("全局拦截器异常",e);
            throw e;
        } catch (Exception e){
            log.error("全局拦截器异常",e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }catch (Throwable e){
            log.error("全局拦截器异常",e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }

    }

    private void validateParams(Method method, Object[] arguments){
        Parameter[] parameters = method.getParameters();
        for(int i = 0; i < parameters.length; i++){
            Parameter parameter = parameters[i];
            Object value = arguments[i];
            VerifyParam annotation = parameter.getAnnotation(VerifyParam.class);
            if(annotation == null){
                continue;
            }

            if(Constants.TYPE_STRING.equals(parameter.getParameterizedType().getTypeName()) || Constants.TYPE_LONG.equals(parameter.getParameterizedType().getTypeName()) || Constants.TYPE_INTEGER.equals(parameter.getParameterizedType().getTypeName())){
                checkValue(value,annotation);
            }else{
                checkObjValue(parameter,value);
            }
        }
    }

    private void checkObjValue(Parameter parameter, Object value){
        try{
            String typeName = parameter.getParameterizedType().getTypeName();
            Class clazz = Class.forName(typeName);
            Field[] fields = clazz.getDeclaredFields();
            for(Field field : fields){
                VerifyParam fieldVerifyParam = field.getAnnotation(VerifyParam.class);
                if(fieldVerifyParam == null) continue;
                field.setAccessible(true);
                Object resultValue = field.get(value);
                checkValue(resultValue,fieldVerifyParam);
            }
        }catch (BusinessException e){
            log.error("参数校验失败",e);
            throw e;
        } catch (Exception e){
            log.error("参数校验失败",e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }catch (Throwable e){
            log.error("参数校验失败",e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    private void checkValue(Object value, VerifyParam annotation){
        Boolean isEmpty = value==null;
        Integer length = value==null?0:value.toString().length();
        log.error(annotation.max() + "," + annotation.min());

        if(isEmpty && annotation.required()){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }else if( !isEmpty && ((annotation.max()!=-1 && annotation.max() < length) || (annotation.min() != -1 && annotation.min() > length))){
            throw new RuntimeException(String.valueOf(ResponseCodeEnum.CODE_600));
        }else if( !isEmpty && !StringTools.isEmpty(annotation.regex().getRegex()) && !VerifyUtils.verify(annotation.regex(),String.valueOf(value))){
            log.error("here");
            throw new RuntimeException(String.valueOf(ResponseCodeEnum.CODE_600));
        }
    }

}
