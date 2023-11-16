package com.jinjiaxin.yixiapan.component;

import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.dto.DownloadFileDto;
import com.jinjiaxin.yixiapan.entity.dto.SysSettingsDto;
import com.jinjiaxin.yixiapan.entity.dto.UserSpaceDto;
import com.jinjiaxin.yixiapan.entity.pojo.User;
import com.jinjiaxin.yixiapan.mappers.FileInfoMapper;
import com.jinjiaxin.yixiapan.mappers.UserInfoMapper;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RedisComponent {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private FileInfoMapper fileMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    public SysSettingsDto getSysSettingDto(){
        SysSettingsDto sysSettingsDto = (SysSettingsDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if(null == sysSettingsDto){
            sysSettingsDto = new SysSettingsDto();
            redisUtils.set(Constants.REDIS_KEY_SYS_SETTING,sysSettingsDto);
        }
        return sysSettingsDto;
    }

    public void saveFileTempSize(String userId, String fileId, Long fileSize){
        Long fileTempSize = getFileTempSize(userId,fileId);
        redisUtils.setex(Constants.REDIS_KEY_FILE_TEMP_SIZE + userId + fileId,fileSize + fileTempSize,Constants.REDIS_KEY_EXPIRES_DAY);
    }

    public UserSpaceDto getUserSpaceDto(String userId){
        UserSpaceDto userSpaceDto = (UserSpaceDto) redisUtils.get(Constants.REDIS_KEY_USER_SPACE_USED + userId);
        if(userSpaceDto == null){
            Long useSpace = fileMapper.selectUseSpace(userId);
            userSpaceDto = new UserSpaceDto(useSpace, getSysSettingDto().getUserInitUseSpace()*Constants.MB);
            saveUserSpace(userId,userSpaceDto);
        }
        return userSpaceDto;
    }

    public void saveUserSpace(String userId,UserSpaceDto userSpaceDto){
        redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USED + userId, userSpaceDto, Constants.REDIS_KEY_EXPIRES_DAY);
    }

    public UserSpaceDto resetUserSpaceUse(String userId){
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        Long useSpace = this.fileMapper.selectUseSpace(userId);
        userSpaceDto.setUseSpace(useSpace);
        User user = this.userInfoMapper.selectByUserId(userId);
        userSpaceDto.setTotalSpace(user.getTotalSpace());
        this.saveUserSpace(userId,userSpaceDto);
        return userSpaceDto;
    }

    public Long getFileTempSize(String userId, String fileId) {
        String key = Constants.REDIS_KEY_FILE_TEMP_SIZE + userId + fileId;
        Object obj = redisUtils.get(key);
        Long tempSize = 0L;
        if(obj == null){
            return tempSize;
        }
        if(obj instanceof Integer){
            return ((Integer) obj).longValue();
        }else if(obj instanceof Long){
            return (Long)obj;
        }

        return 0L;
    }

    public void saveDownloadCode(String code, DownloadFileDto fileDto) {
        redisUtils.setex(Constants.REDIS_KEY_DOWNLOAD_CODE + code , fileDto, (long) Constants.REDIS_KEY_EXPIRES_MINUTE * Constants.LENGTH_5);
    }

    public DownloadFileDto getDownloadCode(String code) {
        return (DownloadFileDto) redisUtils.get(Constants.REDIS_KEY_DOWNLOAD_CODE + code);
    }

    public void saveSysSettingDto(SysSettingsDto sysSettingsDto) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING,sysSettingsDto);
    }
}
