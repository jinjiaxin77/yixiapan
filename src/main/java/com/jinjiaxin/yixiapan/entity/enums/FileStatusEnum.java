package com.jinjiaxin.yixiapan.entity.enums;

import lombok.Getter;

import java.security.PrivilegedAction;

public enum FileStatusEnum {

    TRANSFER(0,"转码中"),
    TRANSFER_FALT(1, "转码失败"),
    USING(2,"使用中");

    @Getter
    private Integer status;

    @Getter
    private String desc;

    FileStatusEnum(Integer status, String desc){
        this.status = status;
        this.desc = desc;
    }

}
