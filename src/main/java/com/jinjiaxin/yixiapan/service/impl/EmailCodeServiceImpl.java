package com.jinjiaxin.yixiapan.service.impl;

import com.jinjiaxin.yixiapan.annotation.GlobalInterceptor;
import com.jinjiaxin.yixiapan.annotation.VerifyParam;
import com.jinjiaxin.yixiapan.entity.config.AppConfig;
import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.dto.CreateImageCode;
import com.jinjiaxin.yixiapan.entity.enums.VerifyRegexEnum;
import com.jinjiaxin.yixiapan.entity.pojo.EmailCode;
import com.jinjiaxin.yixiapan.entity.pojo.User;
import com.jinjiaxin.yixiapan.exception.BusinessException;
import com.jinjiaxin.yixiapan.mappers.EmailCodeMapper;
import com.jinjiaxin.yixiapan.service.EmailCodeService;
import com.jinjiaxin.yixiapan.service.UserInfoService;
import com.jinjiaxin.yixiapan.utils.StringTools;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


/**
 * @author jjx
 * @Description
 * @create 2023/9/16 15:57
 */

@Slf4j
@Service
public class EmailCodeServiceImpl implements EmailCodeService {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private EmailCodeMapper emailCodeMapper;

    @Autowired
    private JavaMailSender sender;

    @Autowired
    private AppConfig appConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalInterceptor(checkParams = true)
    public void sendEmailCode(@VerifyParam(required = true,regex = VerifyRegexEnum.EMAIL) String email, Integer type) {
        if(type == Constants.ZERO){
            User user = userInfoService.getUserByEmail(email);
            if(user != null){
                throw new BusinessException("该邮箱已经注册");
            }
        }

        String code = StringTools.getRandomNumber(Constants.LENGTH_5);

        sendEmailCode(email,code);

        emailCodeMapper.disableEmailCode(email);
        EmailCode emailCode = new EmailCode(email,code,new Date(),Constants.ZERO);
        emailCodeMapper.add(emailCode);
    }

    private void sendEmailCode(String toEmail, String code) {
        try{
            MimeMessage message = sender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message,true);
            helper.setFrom(appConfig.getSendUserName());
            helper.setTo(toEmail);

            helper.setSubject("YixiaPan验证码");
            helper.setText("您的验证码为：" + code);
            helper.setSentDate(new Date());

            sender.send(message);
        }catch (Exception e){
            log.error("邮件发送失败",e);
            throw new BusinessException("邮件发送失败");
        }
    }
}
