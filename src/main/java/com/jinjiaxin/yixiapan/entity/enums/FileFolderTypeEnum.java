package com.jinjiaxin.yixiapan.entity.enums;

import lombok.Getter;

public enum FileFolderTypeEnum {
    FILE(0,"文档"),
    FOLDER(1,"目录");

    @Getter
    private Integer type;

    @Getter
    private String desc;

    FileFolderTypeEnum(Integer type, String desc){
        this.type = type;
        this.desc = desc;
    }
}
