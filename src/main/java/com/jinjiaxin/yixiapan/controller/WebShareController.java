package com.jinjiaxin.yixiapan.controller;

import com.jinjiaxin.yixiapan.annotation.GlobalInterceptor;
import com.jinjiaxin.yixiapan.annotation.VerifyParam;
import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.dto.SessionShareDto;
import com.jinjiaxin.yixiapan.entity.dto.SessionWebUserDto;
import com.jinjiaxin.yixiapan.entity.enums.FileCategoryEnums;
import com.jinjiaxin.yixiapan.entity.enums.FileDelFlagEnums;
import com.jinjiaxin.yixiapan.entity.enums.ResponseCodeEnum;
import com.jinjiaxin.yixiapan.entity.pojo.FileInfo;
import com.jinjiaxin.yixiapan.entity.pojo.FileShare;
import com.jinjiaxin.yixiapan.entity.pojo.User;
import com.jinjiaxin.yixiapan.entity.query.FileInfoQuery;
import com.jinjiaxin.yixiapan.entity.vo.FileInfoVO;
import com.jinjiaxin.yixiapan.entity.vo.PaginationResultVO;
import com.jinjiaxin.yixiapan.entity.vo.ResponseVO;
import com.jinjiaxin.yixiapan.entity.vo.ShareInfoVO;
import com.jinjiaxin.yixiapan.exception.BusinessException;
import com.jinjiaxin.yixiapan.service.FileInfoService;
import com.jinjiaxin.yixiapan.service.FileShareService;
import com.jinjiaxin.yixiapan.service.UserInfoService;
import com.jinjiaxin.yixiapan.utils.CopyTools;
import com.jinjiaxin.yixiapan.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import javax.print.attribute.standard.OrientationRequested;
import java.io.UnsupportedEncodingException;
import java.util.Date;

@RestController("webShareController")
@RequestMapping("/showShare")
public class WebShareController extends CommonFileController {

    @Resource
    private FileShareService fileShareService;

    @Resource
    private FileInfoService fileInfoService;

    @Resource
    private UserInfoService userInfoService;

    @PostMapping("/getShareLoginInfo")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getShareLoginInfo(HttpSession session, @VerifyParam(required = true) String shareId){
        SessionShareDto sessionShareDto = getShareInfoFromSession(session,shareId);
        if(sessionShareDto == null){
            return getSuccessResponseVO(null);
        }
        ShareInfoVO shareInfoVO = getShareInfoCommon(shareId);
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        if(sessionShareDto != null && sessionWebUserDto.getUserId().equals(sessionShareDto.getShareUserId())){
            shareInfoVO.setCurrentUser(true);
        }else{
            shareInfoVO.setCurrentUser(false);
        }
        return getSuccessResponseVO(shareInfoVO);
    }

    @PostMapping("/getShareInfo")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getShareInfo( @VerifyParam(required = true) String shareId){
        ShareInfoVO shareInfoCommon = getShareInfoCommon(shareId);
        return getSuccessResponseVO(shareInfoCommon);
    }

    private ShareInfoVO getShareInfoCommon(String shareId){
        FileShare fileShare = fileShareService.getFileShareByShareId(shareId);
        if(fileShare == null || (fileShare.getExpireTime() != null && new Date().after(fileShare.getExpireTime()))){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        ShareInfoVO shareInfoVO = CopyTools.copy(fileShare,ShareInfoVO.class);
        FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(shareInfoVO.getFileId(),shareInfoVO.getUserId());
        if(fileInfo == null || !fileInfo.getDelFlag().equals(FileDelFlagEnums.USING.getFlag())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        shareInfoVO.setFileName(fileInfo.getFileName());
        User user = userInfoService.getUserByUserId(fileShare.getUserId());
        shareInfoVO.setNickName(user.getNickName());
        shareInfoVO.setAvatar(user.getQqAvatar());
        shareInfoVO.setUserId(user.getUserId());
        return shareInfoVO;
    }

    @PostMapping("/checkShareCode")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO checkShareCode(HttpSession session, @VerifyParam(required = true) String shareId, @VerifyParam(required = true) String code){
        SessionShareDto sessionShareDto = fileShareService.checkShareCode(shareId,code);
        session.setAttribute(Constants.SESSION_SHARE_KEY+shareId,sessionShareDto);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/loadFileList")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadFileList(HttpSession session, @VerifyParam(required = true) String shareId, String filePid){
        SessionShareDto sessionShareDto = checkShare(session,shareId);

        FileInfoQuery query = new FileInfoQuery();
        if(!StringTools.isEmpty(filePid) && !Constants.ZERO_STR.equals(filePid)){
            fileInfoService.checkRootFilePid(sessionShareDto.getFileId(),sessionShareDto.getShareUserId(),filePid);
            query.setFilePid(filePid);
        }else{
            query.setFileId(sessionShareDto.getFileId());
        }
        query.setUserId(sessionShareDto.getShareUserId());
        query.setOrderBy("last_update_time desc");
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());

        PaginationResultVO<FileInfo> resultVO = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(convert2PaginationVO(resultVO, FileInfoVO.class));
    }

    private SessionShareDto checkShare(HttpSession session, String shareId){
        SessionShareDto sessionShareDto = getShareInfoFromSession(session, shareId);
        if(sessionShareDto == null){
            throw new BusinessException(ResponseCodeEnum.CODE_903);
        }
        if(sessionShareDto.getExpireTime() != null && new Date().after(sessionShareDto.getExpireTime())){
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        return sessionShareDto;
    }

    @PostMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getFolderInfo(HttpSession session, @VerifyParam(required = true) String shareId,@VerifyParam(required = true) String path){
        SessionShareDto sessionShareDto = checkShare(session,shareId);
        return super.getFolderInfo(path,sessionShareDto.getShareUserId());
    }

    @PostMapping("/getFile/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public void getFile(HttpServletResponse response, HttpSession session, @VerifyParam(required = true) @PathVariable("shareId") String shareId, @VerifyParam(required = true) @PathVariable("fileId") String fileId){
        SessionShareDto sessionShareDto = checkShare(session,shareId);
        super.getFile(response,fileId, sessionShareDto.getShareUserId());
    }

    @GetMapping("/ts/getVideoInfo/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public void getVideoInfo(HttpServletResponse response,HttpSession session, @VerifyParam(required = true) @PathVariable("shareId") String shareId, @VerifyParam(required = true) @PathVariable("fileId") String fileId){
        SessionShareDto sessionShareDto = checkShare(session,shareId);
        super.getFile(response,fileId, sessionShareDto.getShareUserId());
    }

    @PostMapping("/createDownloadUrl/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO createDownloadUrl(HttpSession session, @VerifyParam(required = true) @PathVariable("shareId") String shareId, @VerifyParam(required = true) @PathVariable("fileId") String fileId){
        SessionShareDto sessionShareDto = checkShare(session,shareId);
        return super.createDownloadUrl(fileId, sessionShareDto.getShareUserId());
    }

    @GetMapping("/download/{code}")
    @GlobalInterceptor(checkParams = true)
    public void download(HttpServletRequest request, HttpServletResponse response, @VerifyParam(required = true) @PathVariable("code") String code) throws UnsupportedEncodingException {
        super.download(request,response,code);
    }

    @PostMapping("/saveShare")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public ResponseVO saveShare(HttpSession session, @VerifyParam(required = true) String shareId, @VerifyParam(required = true) String shareFileIds, @VerifyParam(required = true) String myFolderId){
        SessionShareDto sessionShareDto = getShareInfoFromSession(session,shareId);
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        if(sessionShareDto.getShareUserId().equals(sessionWebUserDto.getUserId())){
            throw new BusinessException("自己分享的文件无法保存到自己的网盘");
        }
        fileInfoService.saveShare(sessionShareDto.getFileId(),shareFileIds,myFolderId,sessionShareDto.getShareUserId(),sessionWebUserDto.getUserId());
        return getSuccessResponseVO(null);
    }

}
