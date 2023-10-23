package com.jinjiaxin.yixiapan.entity.enums;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.web.service.annotation.GetExchange;

public enum FileTypeEnum {

    VIDEO(FileCategoryEnums.VIDEO,1,new String[]{".mp4",".avi","rmvb","mkv",".mov"},"视频"),
    MUSIC(FileCategoryEnums.MUSIC,2,new String[]{".mp3",".wav",".wma",".mp2",".flac",".midi",".ra",".qpe",".aac",".cda"},"音频"),
    IMAGE(FileCategoryEnums.IMAGE,3,new String[]{".ipeg",".jpg",".png",".gif",".bmp",".dds",".psd",".pdt",".webp",".xmp",".svg",".tiff"},"图片"),
    PDF(FileCategoryEnums.DOC,4,new String[]{".pdf"},"pdf"),
    WORD(FileCategoryEnums.DOC,5,new String[]{".doc",".docx"},"word"),
    EXCEL(FileCategoryEnums.DOC,6,new String[]{".xlsx"},"excel"),
    TXT(FileCategoryEnums.DOC,7,new String[]{".txt"},"txt文本"),
    PROGRAME(FileCategoryEnums.OTHERS,8,new String[]{".h",".c",".hpp",".hxx",".cpp",".cc",".c++",".cxx",".m",".o",".s",".dll",".cs",".java",".class",".js",".ts",".css",".scss",".vue",".jsx",".sql",".md",".json",".html",".xml"},"CODE"),
    ZIP(FileCategoryEnums.OTHERS,9,new String[]{".rar",".7z",".zip",".cab",".arj",".lzh",".tar",".gz",".ace",".uue",".bz",".jar",".iso",".mpg"},"压缩包"),
    OTHERS(FileCategoryEnums.OTHERS,10,new String[]{},"其他");

    @Getter
    private FileCategoryEnums category;

    @Getter
    private Integer type;

    @Getter
    private String[] suffix;

    @Getter
    private String desc;

    FileTypeEnum(FileCategoryEnums category, Integer type, String[] suffix, String desc){
        this.category = category;
        this.type = type;
        this.suffix = suffix;
        this.desc = desc;
    }

    public static FileTypeEnum getFileTypeBySuffix(String suffix){
        for(FileTypeEnum item : FileTypeEnum.values()){
            if(ArrayUtils.contains(item.getSuffix(),suffix)){
                return item;
            }
        }
        return FileTypeEnum.OTHERS;
    }

    public static FileTypeEnum getByType(Integer type){
        for(FileTypeEnum item : FileTypeEnum.values()){
            if(item.getType() == type){
                return item;
            }
        }
        return null;
    }

}
