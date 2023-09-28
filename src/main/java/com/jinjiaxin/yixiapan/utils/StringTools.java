package com.jinjiaxin.yixiapan.utils;
import ch.qos.logback.core.testUtil.RandomUtil;
import com.jinjiaxin.yixiapan.exception.BusinessException;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class StringTools {

    public static void checkParam(Object param) {
        try {
            Field[] fields = param.getClass().getDeclaredFields();
            boolean notEmpty = false;
            for (Field field : fields) {
                String methodName = "get" + StringTools.upperCaseFirstLetter(field.getName());
                Method method = param.getClass().getMethod(methodName);
                Object object = method.invoke(param);
                if (object != null && object instanceof String && !StringTools.isEmpty(object.toString())
                        || object != null && !(object instanceof String)) {
                    notEmpty = true;
                    break;
                }
            }
            if (!notEmpty) {
                throw new BusinessException("多参数更新，删除，必须有非空条件");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("校验参数是否为空失败");
        }
    }

    public static String upperCaseFirstLetter(String field) {
        if (isEmpty(field)) {
            return field;
        }
        //如果第二个字母是大写，第一个字母不大写
        if (field.length() > 1 && Character.isUpperCase(field.charAt(1))) {
            return field;
        }
        return field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    public static boolean isEmpty(String str) {
        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }

    public static final String getRandomNumber(Integer count){
        String str1 = "abcdefghijklmnopqrstuvwxyz1234567890";
        String str2 = "";
        int len = str1.length() - 1;
        double r;
        for( int i = 0; i < count; i++){
            r = (Math.random()) * len;
            str2 = str2 + str1.charAt((int) r);
        }

        return  str2;
    }

    public static String encodeByMd5(String password){
        return isEmpty(password)?null: DigestUtils.md5DigestAsHex(password.getBytes());
    }

    public static boolean pathIsOk(String path){
        if(StringTools.isEmpty(path)){
            return true;
        }
        if(path.contains("../") || path.contains("..\\")){
            return false;
        }
        return true;
    }
}
