package com.jinjiaxin.yixiapan.utils;

import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.enums.VerifyRegexEnum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jjx
 * @Description
 * @create 2023/9/17 15:49
 */

public class VerifyUtils {

    public static Boolean verify(String regs,String value){
        if(StringTools.isEmpty(value)){
            return false;
        }
        Pattern pattern = Pattern.compile(regs);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    public static Boolean verify(VerifyRegexEnum regs, String value){
        return verify(regs.getRegex(),value);
    }

}
