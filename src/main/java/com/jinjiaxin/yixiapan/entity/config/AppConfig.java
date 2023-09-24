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

    @Value("${admin.emails}")
    private String adminEmails;

}
