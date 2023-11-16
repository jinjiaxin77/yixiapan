package com.jinjiaxin.yixiapan.entity.dto;

import lombok.Data;
import org.springframework.data.redis.core.convert.SpelIndexResolver;

@Data
public class DownloadFileDto {
    private String downloadCode;

    private String fileName;

    private String filePath;

}
