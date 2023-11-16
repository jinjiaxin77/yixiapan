package com.jinjiaxin.yixiapan.entity.enums;

import lombok.Getter;

@Getter
public enum ShareValidTypeEnum {
    DAY_1(0,1,"1天"),
    DAY_7(1,7,"7天"),
    DAY_30(2,30,"30天"),
    FOREVER(3,-1,"永久有效");

    private Integer type;
    private Integer days;
    private String desc;

    ShareValidTypeEnum(Integer type, Integer days, String desc){
        this.type = type;
        this.days = days;
        this.desc = desc;
    }

    public static ShareValidTypeEnum getByType(Integer type){
        for(ShareValidTypeEnum typeEnum : ShareValidTypeEnum.values()){
            if(typeEnum.getType().equals(type)){
                return typeEnum;
            }
        }
        return null;
    }
}
