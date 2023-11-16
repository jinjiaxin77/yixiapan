package com.jinjiaxin.yixiapan.controller;

import com.jinjiaxin.yixiapan.annotation.GlobalInterceptor;
import com.jinjiaxin.yixiapan.annotation.VerifyParam;
import com.jinjiaxin.yixiapan.entity.dto.SessionWebUserDto;
import com.jinjiaxin.yixiapan.entity.dto.UploadResultDto;
import com.jinjiaxin.yixiapan.entity.enums.FileCategoryEnums;
import com.jinjiaxin.yixiapan.entity.enums.FileDelFlagEnums;
import com.jinjiaxin.yixiapan.entity.enums.FileFolderTypeEnum;
import com.jinjiaxin.yixiapan.entity.pojo.FileInfo;
import com.jinjiaxin.yixiapan.entity.query.FileInfoQuery;
import com.jinjiaxin.yixiapan.entity.vo.FileInfoVO;
import com.jinjiaxin.yixiapan.entity.vo.PaginationResultVO;
import com.jinjiaxin.yixiapan.entity.vo.ResponseVO;
import com.jinjiaxin.yixiapan.service.FileInfoService;
import com.jinjiaxin.yixiapan.utils.CopyTools;
import com.jinjiaxin.yixiapan.utils.StringTools;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileInfoController extends CommonFileController {

    @Autowired
    private FileInfoService fileInfoService;

    @PostMapping("/loadDataList")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public ResponseVO loadDataList(HttpSession session, FileInfoQuery query, String category){
        FileCategoryEnums categoryEnum = FileCategoryEnums.getByCode(category);

        if(categoryEnum != null){
            query.setFileCategory(categoryEnum.getCategory());
        }
        query.setUserId(getUserInfoFromSession(session).getUserId());
        query.setOrderBy("last_update_time desc");
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());

        PaginationResultVO<FileInfo> resultVO = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(convert2PaginationVO(resultVO, FileInfoVO.class));
    }

    @PostMapping("/uploadFile")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    @Transactional(rollbackFor = Exception.class)
    public ResponseVO uploadFile(HttpSession session, String fileId, MultipartFile file,
                                 @VerifyParam(required = true) String fileName,
                                 @VerifyParam(required = true) String filePid,
                                 @VerifyParam(required = true) String fileMd5,
                                 @VerifyParam(required = true) Integer chunkIndex,
                                 @VerifyParam(required = true) Integer chunks){
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        UploadResultDto uploadResultDto = fileInfoService.uploadFile(userDto,fileId,file,fileName,filePid,fileMd5,chunkIndex,chunks);

        return getSuccessResponseVO(uploadResultDto);
    }

    @GetMapping("/getImage/{imageFolder}/{imageName}")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public void getImage(HttpServletResponse response, @PathVariable("imageFolder") String imageFolder, @PathVariable("imageName") String imageName){
        super.getImage(response,imageFolder,imageName);
    }

    @GetMapping("/ts/getVideoInfo/{fileId}")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public void getVideoInfo(HttpServletResponse response, HttpSession session, @PathVariable("fileId") String fileId){
        super.getFile(response,fileId,getUserInfoFromSession(session).getUserId());
    }

    @PostMapping("/getFile/{fileId}")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public void getFile(HttpServletResponse response, HttpSession session, @PathVariable("fileId") String fileId){
        super.getFile(response,fileId,getUserInfoFromSession(session).getUserId());
    }

    @PostMapping("/newFolder")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public ResponseVO newFolder(HttpSession session, @VerifyParam(required = true) String filePid, @VerifyParam(required = true) String fileName){
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        FileInfo fileInfo = fileInfoService.newFolder(filePid, userDto.getUserId(), fileName);

        return getSuccessResponseVO(CopyTools.copy(fileInfo,FileInfoVO.class));
    }

    @PostMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public ResponseVO getFolderInfo(HttpSession session, @VerifyParam(required = true) String path){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        return super.getFolderInfo(path,webUserDto.getUserId());
    }

    @PostMapping("/rename")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public ResponseVO rename(HttpSession session, @VerifyParam(required = true) String fileId, String fileName){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        FileInfo fileInfo = fileInfoService.rename(webUserDto.getUserId(),fileId,fileName);

        return getSuccessResponseVO(CopyTools.copy(fileInfo,FileInfoVO.class));
    }

    @PostMapping("/loadAllFolder")
    @GlobalInterceptor(checkLogin = true,checkParams = true)
    public ResponseVO loadAllFolder(HttpSession session, @VerifyParam(required = true) String filePid, @VerifyParam(required = true) String currentFileIds){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(webUserDto.getUserId());
        query.setFilePid(filePid);
        if(!StringTools.isEmpty(currentFileIds)){
            query.setExcludeFileIdArray(currentFileIds.split(","));
        }
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());
        query.setOrderBy("create_time desc");
        query.setFolderType(FileFolderTypeEnum.FOLDER.getType());
        List<FileInfo> list = fileInfoService.findListByParam(query);

        return getSuccessResponseVO(CopyTools.copyList(list,FileInfoVO.class));
    }

    @PostMapping("/changeFileFolder")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public ResponseVO changeFileFolder(HttpSession session, @VerifyParam(required = true) String fileIds, @VerifyParam(required = true) String filePid){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        fileInfoService.changeFileFolder(fileIds,filePid,webUserDto.getUserId());

        return getSuccessResponseVO(null);
    }

    @PostMapping("/createDownloadUrl/{fileId}")
    @GlobalInterceptor(checkParams = true, checkLogin = true)
    public ResponseVO createDownloadUrl(HttpSession session, @VerifyParam(required = true) @PathVariable("fileId") String fileId){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        return super.createDownloadUrl(fileId, webUserDto.getUserId());
    }

    @GetMapping("/download/{code}")
    @GlobalInterceptor(checkParams = true)
    public void download(HttpServletRequest request, HttpServletResponse response,@VerifyParam(required = true) @PathVariable("code") String code) throws UnsupportedEncodingException {
        super.download(request,response,code);
    }

    @PostMapping("/delFile")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public ResponseVO delFile(HttpSession session, @VerifyParam(required = true) String fileIds){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        this.fileInfoService.removeFile2RecycleBatch(webUserDto.getUserId(), fileIds);

        return getSuccessResponseVO(null);
    }

}
