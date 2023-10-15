package com.jinjiaxin.yixiapan.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SysSettingsDto implements Serializable {

    private String registerMailTitle = "邮箱验证码";

    private String registerEmailContent = "你好，您的邮箱验证码是：%s，15分钟内有效";

    private Long userInitUserSpace = 100L;

}
