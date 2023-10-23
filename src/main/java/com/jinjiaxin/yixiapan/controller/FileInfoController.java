package com.jinjiaxin.yixiapan.controller;

import com.jinjiaxin.yixiapan.annotation.GlobalInterceptor;
import com.jinjiaxin.yixiapan.annotation.VerifyParam;
import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.dto.SessionWebUserDto;
import com.jinjiaxin.yixiapan.entity.dto.UploadResultDto;
import com.jinjiaxin.yixiapan.entity.enums.FileCategoryEnums;
import com.jinjiaxin.yixiapan.entity.enums.FileDelFlagEnums;
import com.jinjiaxin.yixiapan.entity.query.FileInfoQuery;
import com.jinjiaxin.yixiapan.entity.vo.FileInfoVO;
import com.jinjiaxin.yixiapan.entity.vo.PaginationResultVO;
import com.jinjiaxin.yixiapan.entity.vo.ResponseVO;
import com.jinjiaxin.yixiapan.service.FileInfoService;
import jakarta.mail.Session;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileInfoController extends ABaseController {

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

        PaginationResultVO resultVO = fileInfoService.findListByPage(query);
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

}
