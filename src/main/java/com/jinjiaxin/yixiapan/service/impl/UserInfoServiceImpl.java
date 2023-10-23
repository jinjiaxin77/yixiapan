package com.jinjiaxin.yixiapan.service.impl;

import com.jinjiaxin.yixiapan.component.RedisComponent;
import com.jinjiaxin.yixiapan.entity.config.AppConfig;
import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.dto.QQInfoDto;
import com.jinjiaxin.yixiapan.entity.dto.SessionWebUserDto;
import com.jinjiaxin.yixiapan.entity.dto.UserSpaceDto;
import com.jinjiaxin.yixiapan.entity.enums.UserStatusEnum;
import com.jinjiaxin.yixiapan.entity.pojo.User;
import com.jinjiaxin.yixiapan.exception.BusinessException;
import com.jinjiaxin.yixiapan.mappers.FileInfoMapper;
import com.jinjiaxin.yixiapan.mappers.UserInfoMapper;
import com.jinjiaxin.yixiapan.service.EmailCodeService;
import com.jinjiaxin.yixiapan.service.UserInfoService;
import com.jinjiaxin.yixiapan.utils.JsonUtils;
import com.jinjiaxin.yixiapan.utils.OKHttpUtils;
import com.jinjiaxin.yixiapan.utils.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.ArrayUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

/**
 * @author jjx
 * @Description
 * @create 2023/9/18 16:12
 */

@Service
@Slf4j
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    EmailCodeService emailCodeService;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private FileInfoMapper fileMapper;

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
            User user = new User(userId, nickName, email, null, null, StringTools.encodeByMd5(password), new Date(), new Date(), UserStatusEnum.ENABLE.getStatus(), 0l, redisComponent.getSysSettingDto().getUserInitUserSpace()*Constants.MB);
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
            System.out.println(emailUser==null);
            throw new BusinessException("账号或者密码错误");
        }

        if(UserStatusEnum.DISABLE.getStatus().equals(emailUser.getStatus())){
            throw new BusinessException("账号已禁用");
        }

        emailUser.setLastLoginTime(new Date());
        userInfoMapper.update(emailUser);

        Boolean isAdmin = ArrayUtils.contains(appConfig.getAdminEmails().split(","),email);

        Long useSpace = fileMapper.selectUseSpace(emailUser.getUserId());
        UserSpaceDto userSpace = new UserSpaceDto(useSpace, emailUser.getTotalSpace());
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
            user.setPassword(StringTools.encodeByMd5(password));
            userInfoMapper.update(user);
        }
    }

    @Override
    public void updateUserAvatarById(String userId, User user) {
        userInfoMapper.updateAvatarById(userId,user);
    }

    @Override
    public void updateUserPasswordById(String userId, User user) {
        userInfoMapper.updatePasswordById(userId,user);
    }

    @Override
    public SessionWebUserDto qqLogin(String code) {
        //1.通过回调code，获取accessToken
        String accessToken = getQQAccessToken(code);

        //2.获取qqOpenId
        String qqOpenId = getQQOpenId(accessToken);
        User user = this.userInfoMapper.selectByQqOpenId(qqOpenId);
        if(user == null){
            QQInfoDto qqInfoDto = getQQUserInfo(accessToken,qqOpenId);
            user = new User();

            String nickName = qqInfoDto.getNickName();
            nickName = nickName.length() > Constants.LENGTH_20 ? nickName.substring(0,Constants.LENGTH_20) : nickName;

            String avatar =  StringTools.isEmpty(qqInfoDto.getFigureUrl_qq_2()) ? qqInfoDto.getFigureUrl_qq_1() : qqInfoDto.getFigureUrl_qq_2();

            Date curDate = new Date();

            user.setQqOpenId(qqOpenId);
            user.setNickName(nickName);
            user.setQqAvatar(avatar);
            user.setRegisTime(curDate);
            user.setLastLoginTime(curDate);
            user.setUserId(StringTools.getRandomNumber(Constants.LENGTH_15));
            user.setStatus(UserStatusEnum.ENABLE.getStatus());
            user.setUseSpace(0L);
            user.setTotalSpace(redisComponent.getSysSettingDto().getUserInitUserSpace()*Constants.MB);
            this.userInfoMapper.add(user);

            user = userInfoMapper.selectByQqOpenId(qqOpenId);
        }else{
            user.setLastLoginTime(new Date());
            this.userInfoMapper.update(user);
        }

        Boolean isAdmin = user.getEmail() != null && ArrayUtils.contains(appConfig.getAdminEmails().split(","), user.getEmail());
        SessionWebUserDto userDto = new SessionWebUserDto(user.getNickName(), user.getUserId(), user.getQqAvatar(),isAdmin);

        UserSpaceDto userSpaceDto = new UserSpaceDto();
        Long useSpace = fileMapper.selectUseSpace(user.getUserId());
        userSpaceDto.setUseSpace(useSpace);
        userSpaceDto.setTotalSpace(user.getTotalSpace());
        redisComponent.saveUserSpace(user.getUserId(), userSpaceDto);

        return userDto;
    }

    private String getQQAccessToken(String code){
        String accessToken = null;
        String url = null;
        try{
            url = String.format(appConfig.getQqUrlAccessToken(),appConfig.getQqAppId(),appConfig.getQqAppKey(),code, URLEncoder.encode(appConfig.getQqUrlRedirect(),"utf-8"));
        }catch (UnsupportedEncodingException e){
            log.error("encode失败",e);
        }
        String tokenResult = OKHttpUtils.getRequest(url);
        if(tokenResult == null || tokenResult.indexOf(Constants.VIEW_OBJ_RESULT_KEY) != -1){
            log.error("获取token失败:{}",tokenResult);
            throw new BusinessException("获取token失败");
        }
        String[] params = tokenResult.split("&");
        if(params != null && params.length > 0){
            for(String p : params){
                if(p.indexOf("access_token") != -1){
                    accessToken = p.split("=")[1];
                    break;
                }
            }
        }

        return accessToken;
    }

    private String getQQOpenId(String accessToken){
        String url = String.format(appConfig.getQqUrlOpenId(),accessToken);
        String openIdResult = OKHttpUtils.getRequest(url);
        String tmpJson = this.getQQResp(openIdResult);
        if(tmpJson == null){
            log.error("调qq接口获取openId失败:tmpJson{}",tmpJson);
            throw new BusinessException("调qq接口获取openId失败");
        }
        Map jsonData = JsonUtils.jsonToObj(tmpJson, Map.class);
        if(jsonData == null || jsonData.containsKey(Constants.VIEW_OBJ_RESULT_KEY)){
            log.error("调qq接口获取openId失败：{}",jsonData);
            throw new BusinessException("调qq接口获取openId失败");
        }
        return String.valueOf(jsonData.get("openid"));
    }

    private String getQQResp(String result){
        if(StringUtils.isNotBlank(result)){
            int pos = result.indexOf("callback");
            if(pos != -1){
                int start = result.indexOf("(");
                int end = result.indexOf(")");
                String jsonStr = result.substring(start+1,end-1);
                return jsonStr;
            }
        }

        return null;
    }

    private QQInfoDto getQQUserInfo(String accessToken, String qqOpenId){
        String url = String.format(appConfig.getQqUrlUserInfo(),accessToken,appConfig.getQqAppId(),qqOpenId);
        String response = OKHttpUtils.getRequest(url);
        if(StringUtils.isNotBlank(response)){
            QQInfoDto qqInfoDto = JsonUtils.jsonToObj(response, QQInfoDto.class);
            if(qqInfoDto.getRet() != 0){
                log.error("qqInfo:{}",response);
                throw new BusinessException("调qq接口获取用户信息异常");
            }
            return qqInfoDto;
        }
        throw new BusinessException("调qq接口获取用户信息异常");
    }

}
