package com.jinjiaxin.yixiapan.controller;

import com.jinjiaxin.yixiapan.annotation.GlobalInterceptor;
import com.jinjiaxin.yixiapan.annotation.VerifyParam;
import com.jinjiaxin.yixiapan.entity.dto.SessionWebUserDto;
import com.jinjiaxin.yixiapan.entity.pojo.FileShare;
import com.jinjiaxin.yixiapan.entity.query.FileShareQuery;
import com.jinjiaxin.yixiapan.entity.vo.PaginationResultVO;
import com.jinjiaxin.yixiapan.entity.vo.ResponseVO;
import com.jinjiaxin.yixiapan.service.FileShareService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("fileShareController")
@RequestMapping("/share")
@Slf4j
public class FileShareController extends ABaseController {

    @Resource
    private FileShareService fileShareService;

    @PostMapping("/loadShareList")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public ResponseVO loadShareList(HttpSession session, FileShareQuery query){
        query.setUserId(getUserInfoFromSession(session).getUserId());
        query.setOrderBy("share_time desc");
        query.setQueryFileName(true);
        PaginationResultVO<FileShare> list = fileShareService.findListByPage(query);

        return getSuccessResponseVO(list);
    }

    @PostMapping("/shareFile")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public ResponseVO shareFile(HttpSession session, @VerifyParam(required = true) String fileId, @VerifyParam(required = true) Integer validType, String code){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        FileShare share = new FileShare();
        share.setFileId(fileId);
        share.setValidType(validType);
        share.setCode(code);
        share.setUserId(webUserDto.getUserId());
        fileShareService.saveShare(share);

        return getSuccessResponseVO(share);
    }

    @PostMapping("/cancelShare")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public ResponseVO cancelShare(HttpSession session, @VerifyParam(required = true) String shareIds){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        fileShareService.deleteShareBatch(webUserDto.getUserId(),shareIds);

        return getSuccessResponseVO(null);
    }

}
