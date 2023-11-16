package com.jinjiaxin.yixiapan.service.impl;

import com.jinjiaxin.yixiapan.annotation.GlobalInterceptor;
import com.jinjiaxin.yixiapan.annotation.VerifyParam;
import com.jinjiaxin.yixiapan.component.RedisComponent;
import com.jinjiaxin.yixiapan.component.RedisUtils;
import com.jinjiaxin.yixiapan.entity.config.AppConfig;
import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.dto.SysSettingsDto;
import com.jinjiaxin.yixiapan.entity.enums.VerifyRegexEnum;
import com.jinjiaxin.yixiapan.entity.pojo.EmailCode;
import com.jinjiaxin.yixiapan.entity.pojo.User;
import com.jinjiaxin.yixiapan.exception.BusinessException;
import com.jinjiaxin.yixiapan.mappers.EmailCodeMapper;
import com.jinjiaxin.yixiapan.mappers.UserInfoMapper;
import com.jinjiaxin.yixiapan.service.EmailCodeService;
import com.jinjiaxin.yixiapan.service.UserInfoService;
import com.jinjiaxin.yixiapan.utils.StringTools;
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
    private UserInfoMapper userInfoMapper;

    @Autowired
    private EmailCodeMapper emailCodeMapper;

    @Autowired
    private JavaMailSender sender;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private RedisComponent redisComponent;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalInterceptor(checkParams = true)
    public void sendEmailCode(@VerifyParam(required = true,regex = VerifyRegexEnum.EMAIL) String email, Integer type) {
        if(type == Constants.ZERO){
            User user = userInfoMapper.selectByEmail(email);
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

    @Override
    public boolean checkCode(String email, String code) {
        EmailCode emailCode = emailCodeMapper.selectEmailCode(email);
        if(emailCode == null){
            throw new BusinessException("邮箱验证码不正确");
        }

        if(System.currentTimeMillis() - emailCode.getCreateTime().getTime() > Constants.LENGTH_15*1000*60){
            emailCodeMapper.update(emailCode);
            throw new BusinessException("邮箱验证码失效");
        }


        emailCodeMapper.update(emailCode);
        return code.equalsIgnoreCase(emailCode.getCode());
    }

    private void sendEmailCode(String toEmail, String code) {
        try{
            MimeMessage message = sender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message,true);
            helper.setFrom(appConfig.getSendUserName());
            helper.setTo(toEmail);

            SysSettingsDto sysSettingDto = redisComponent.getSysSettingDto();

            helper.setSubject(sysSettingDto.getRegisterEmailTitle());
            helper.setText(String.format(sysSettingDto.getRegisterEmailContent(), code));
            helper.setSentDate(new Date());

            sender.send(message);
        }catch (Exception e){
            log.error("邮件发送失败",e);
            throw new BusinessException("邮件发送失败");
        }
    }
}
