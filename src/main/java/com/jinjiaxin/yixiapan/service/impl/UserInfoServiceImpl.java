package com.jinjiaxin.yixiapan.service.impl;

import com.jinjiaxin.yixiapan.component.RedisComponent;
import com.jinjiaxin.yixiapan.component.RedisUtils;
import com.jinjiaxin.yixiapan.entity.config.AppConfig;
import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.dto.SessionWebUserDto;
import com.jinjiaxin.yixiapan.entity.dto.UserSpaceDto;
import com.jinjiaxin.yixiapan.entity.enums.UserStatusEnum;
import com.jinjiaxin.yixiapan.entity.pojo.User;
import com.jinjiaxin.yixiapan.exception.BusinessException;
import com.jinjiaxin.yixiapan.mappers.UserInfoMapper;
import com.jinjiaxin.yixiapan.service.EmailCodeService;
import com.jinjiaxin.yixiapan.service.UserInfoService;
import com.jinjiaxin.yixiapan.utils.StringTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.ArrayUtils;

import java.util.Date;

/**
 * @author jjx
 * @Description
 * @create 2023/9/18 16:12
 */

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    EmailCodeService emailCodeService;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private RedisComponent redisComponent;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String nickName, String password, String emailCode) {
        User emailUser = userInfoMapper.selectByEmail(email);
        if(emailUser != null){
            throw new BusinessException("邮箱账号已经存在");
        }
        User nickNameUser = userInfoMapper.selectByNickName(nickName);
        if(nickNameUser != null){
            throw new BusinessException("昵称已经存在");
        }

        if(!emailCodeService.checkCode(email,emailCode)){
            throw new BusinessException("邮箱验证码错误");
        }else{
            String userId = StringTools.getRandomNumber(Constants.LENGTH_15);
            User user = new User(userId, nickName, email, null, null, StringTools.encodeByMd5(password), new Date(), new Date(), UserStatusEnum.ENABLE.getStatus(), 0l, 100l);
            userInfoMapper.add(user);
        }

    }

    @Override
    public User getUserByEmail(String email) {
        return userInfoMapper.selectByEmail(email);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SessionWebUserDto login(String email, String password) {
        User emailUser = userInfoMapper.selectByEmail(email);
        if(emailUser == null || !emailUser.getPassword().equals(password)){
            throw new BusinessException("账号或者密码错误");
        }

        if(UserStatusEnum.DISABLE.getStatus().equals(emailUser.getStatus())){
            throw new BusinessException("账号已禁用");
        }

        emailUser.setLastLoginTime(new Date());
        userInfoMapper.update(emailUser);

        Boolean isAdmin = ArrayUtils.contains(appConfig.getAdminEmails().split(","),email);

        //查询文件表
        UserSpaceDto userSpace = new UserSpaceDto(emailUser.getUseSpace(), emailUser.getTotalSpace());
        redisComponent.saveUserSpace(emailUser.getUserId(),userSpace);

        return new SessionWebUserDto(emailUser.getNickName(),emailUser.getUserId(),emailUser.getQqAvatar(),isAdmin);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPwd(String email, String password, String emailCode) {
        User user = userInfoMapper.selectByEmail(email);
        if(user == null){
            throw new BusinessException("该用户不存在");
        }

        if(!emailCodeService.checkCode(email,emailCode)){
            throw new BusinessException("邮箱验证码错误");
        }else{
            user.setPassword(password);
            userInfoMapper.update(user);
        }
    }
}
