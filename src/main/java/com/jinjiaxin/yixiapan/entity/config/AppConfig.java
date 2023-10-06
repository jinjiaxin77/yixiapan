package com.jinjiaxin.yixiapan.entity.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * @author jjx
 * @Description
 * @create 2023/9/16 20:31
 */

@Component("appConfig")
@Data
public class AppConfig {

    @Value("${spring.mail.username}")
    private String sendUserName;

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${admin.emails}")
    private String adminEmails;

    @Value("${qq.app.id}")
    private String qqAppId;

    @Value("${qq.app.key}")
    private String qqAppKey;

    @Value("${qq.url.authorization}")
    private String qqUrlAuthorization;

    @Value("${qq.url.access.token}")
    private String qqUrlAccessToken;

    @Value("${qq.url.openid}")
    private String qqUrlOpenId;

    @Value("${qq.url.user.info}")
    private String qqUrlUserInfo;

    @Value("${qq.url.redirect}")
    private String qqUrlRedirect;

}
