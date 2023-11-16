package com.jinjiaxin.yixiapan.controller;

import com.jinjiaxin.yixiapan.annotation.GlobalInterceptor;
import com.jinjiaxin.yixiapan.annotation.VerifyParam;
import com.jinjiaxin.yixiapan.component.RedisComponent;
import com.jinjiaxin.yixiapan.entity.dto.SysSettingsDto;
import com.jinjiaxin.yixiapan.entity.query.FileInfoQuery;
import com.jinjiaxin.yixiapan.entity.query.UserInfoQuery;
import com.jinjiaxin.yixiapan.entity.vo.PaginationResultVO;
import com.jinjiaxin.yixiapan.entity.vo.ResponseVO;
import com.jinjiaxin.yixiapan.entity.vo.UserInfoVO;
import com.jinjiaxin.yixiapan.service.FileInfoService;
import com.jinjiaxin.yixiapan.service.UserInfoService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController("adminController")
@RequestMapping("/admin")
public class AdminController extends CommonFileController {

    @Resource
    private FileInfoService fileInfoService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoService userInfoService;

    @PostMapping("/getSysSettings")
    @GlobalInterceptor(checkParams = true,checkLogin = true,checkAdmin = true)
    public ResponseVO getSysSettings(){
        SysSettingsDto sysSettingDto = redisComponent.getSysSettingDto();

        return getSuccessResponseVO(sysSettingDto);
    }

    @PostMapping("/saveSysSettings")
    @GlobalInterceptor(checkParams = true,checkLogin = true,checkAdmin = true)
    public ResponseVO saveSysSettings(@VerifyParam(required = true) String registerEmailTitle,
                                      @VerifyParam(required = true) String registerEmailContent,
                                      @VerifyParam(required = true) Integer userInitUseSpace){
        SysSettingsDto sysSettingsDto = new SysSettingsDto();
        sysSettingsDto.setRegisterEmailTitle(registerEmailTitle);
        sysSettingsDto.setRegisterEmailContent(registerEmailContent);
        sysSettingsDto.setUserInitUseSpace(Long.valueOf(userInitUseSpace));
        redisComponent.saveSysSettingDto(sysSettingsDto);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/loadUserList")
    @GlobalInterceptor(checkParams = true,checkLogin = true,checkAdmin = true)
    public ResponseVO loadUserList(UserInfoQuery userInfoQuery){
        userInfoQuery.setOrderBy("join_time desc");
        PaginationResultVO resultVO = this.userInfoService.findListByPage(userInfoQuery);

        return getSuccessResponseVO(convert2PaginationVO(resultVO, UserInfoVO.class));
    }

    @PostMapping("/updateUserStatus")
    @GlobalInterceptor(checkParams = true,checkLogin = true,checkAdmin = true)
    public ResponseVO updateUserStatus(@VerifyParam(required = true) String userId, @VerifyParam(required = true) Integer status){
        this.userInfoService.updateUserStatusById(userId,status);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/updateUserSpace")
    @GlobalInterceptor(checkParams = true,checkLogin = true,checkAdmin = true)
    public ResponseVO updateUserSpace(@VerifyParam(required = true) String userId, @VerifyParam(required = true) Long changeSpace){
        this.userInfoService.updateUserSpaceById(userId,changeSpace);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/loadFileList")
    @GlobalInterceptor(checkParams = true,checkLogin = true,checkAdmin = true)
    public ResponseVO loadFileList(FileInfoQuery query){
        query.setOrderBy("last_update_time desc");
        query.setQueryNickName(true);
        PaginationResultVO resultVO = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }

    @PostMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true,checkLogin = true,checkAdmin = true)
    public ResponseVO getFolderInfo(@VerifyParam(required = true) String path){
        return super.getFolderInfo(path, null);
    }

    @PostMapping("/getFile/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkLogin = true,checkAdmin = true)
    public void getFile(HttpServletResponse response, @VerifyParam(required = true) @PathVariable("userId") String userId, @VerifyParam(required = true) @PathVariable("fileId") String fileId){
        super.getFile(response,fileId,userId);
    }

    @GetMapping("/ts/getVideoInfo/{fileId}")
    @GlobalInterceptor(checkParams = true,checkLogin = true,checkAdmin = true)
    public void getVideoInfo(HttpServletResponse response, @VerifyParam(required = true) @PathVariable("fileId") String fileId, @VerifyParam(required = true) @PathVariable("userId") String userId){
        super.getFile(response,fileId,userId);
    }

    @PostMapping("/createDownloadUrl/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true, checkLogin = true)
    public ResponseVO createDownloadUrl(@VerifyParam(required = true) @PathVariable("userId") String userId, @VerifyParam(required = true) @PathVariable("fileId") String fileId){
        return super.createDownloadUrl(fileId, userId);
    }

    @GetMapping("/download/{code}")
    @GlobalInterceptor(checkParams = true)
    public void download(HttpServletRequest request, HttpServletResponse response, @VerifyParam(required = true) @PathVariable("code") String code) throws UnsupportedEncodingException {
        super.download(request,response,code);
    }

    @PostMapping("/delFile")
    @GlobalInterceptor(checkParams = true,checkLogin = true,checkAdmin = true)
    public ResponseVO delFile(@VerifyParam(required = true) String fileIdAndUserIds){
        String[] fileIdAndUserIdArray = fileIdAndUserIds.split(",");
        for(String fileIdAndUserId : fileIdAndUserIdArray){
            String[] itemArray = fileIdAndUserId.split("_");
            this.fileInfoService.delFileBatch(itemArray[0],itemArray[1],true);
        }

        return getSuccessResponseVO(null);
    }

}
