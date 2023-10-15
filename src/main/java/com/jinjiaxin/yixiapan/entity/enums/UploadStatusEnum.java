package com.jinjiaxin.yixiapan.entity.enums;

import lombok.Getter;

public enum UploadStatusEnum {

    UPLOAD_SECONDS("upload_seconds","秒传"),
    UPLOADING("uploading","上传中"),
    UPLOAD_FINISH("upload_finish","上传完成");

    @Getter
    private String code;

    @Getter
    private String desc;

    UploadStatusEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }

}
