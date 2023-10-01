package com.jinjiaxin.yixiapan.component;

import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.dto.SysSettingsDto;
import com.jinjiaxin.yixiapan.entity.dto.UserSpaceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RedisComponent {

    @Autowired
    private RedisUtils redisUtils;

    public SysSettingsDto getSysSettingDto(){
        SysSettingsDto sysSettingsDto = (SysSettingsDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if(null == sysSettingsDto){
            sysSettingsDto = new SysSettingsDto();
            redisUtils.set(Constants.REDIS_KEY_SYS_SETTING,sysSettingsDto);
        }
        return sysSettingsDto;
    }

    public UserSpaceDto getUserSpaceDto(String userId){
        UserSpaceDto userSpaceDto = (UserSpaceDto) redisUtils.get(Constants.REDIS_KEY_USER_SPACE_USED + userId);
        if(userSpaceDto == null){
            //查询文件表
            userSpaceDto = new UserSpaceDto(0L, getSysSettingDto().getUserInitUserSpace()*Constants.MB);
            saveUserSpace(userId,userSpaceDto);
        }
        return userSpaceDto;
    }

    public void saveUserSpace(String userId,UserSpaceDto userSpaceDto){
        redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USED + userId, userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
    }

}
