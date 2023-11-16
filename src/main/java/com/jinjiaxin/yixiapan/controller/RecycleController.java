package com.jinjiaxin.yixiapan.controller;

import com.jinjiaxin.yixiapan.annotation.GlobalInterceptor;
import com.jinjiaxin.yixiapan.annotation.VerifyParam;
import com.jinjiaxin.yixiapan.entity.dto.SessionWebUserDto;
import com.jinjiaxin.yixiapan.entity.enums.FileDelFlagEnums;
import com.jinjiaxin.yixiapan.entity.pojo.FileInfo;
import com.jinjiaxin.yixiapan.entity.query.FileInfoQuery;
import com.jinjiaxin.yixiapan.entity.vo.FileInfoVO;
import com.jinjiaxin.yixiapan.entity.vo.PaginationResultVO;
import com.jinjiaxin.yixiapan.entity.vo.ResponseVO;
import com.jinjiaxin.yixiapan.service.FileInfoService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("recycleController")
@RequestMapping("/recycle")
public class RecycleController extends ABaseController {

    @Resource
    private FileInfoService fileInfoService;


    @PostMapping("loadRecycleList")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public ResponseVO loadRecycleList(HttpSession session, Integer pageNo, Integer pageSize){
        FileInfoQuery query = new FileInfoQuery();

        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        query.setUserId(getUserInfoFromSession(session).getUserId());
        query.setOrderBy("recovery_time desc");
        query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());

        PaginationResultVO<FileInfo> resultVO = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(convert2PaginationVO(resultVO, FileInfoVO.class));
    }

    @PostMapping("/recoverFile")
    @GlobalInterceptor(checkLogin = true, checkParams = true)
    public ResponseVO recoverFile(HttpSession session, @VerifyParam(required = true) String fileIds){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        this.fileInfoService.recoverFileBatch(webUserDto.getUserId(),fileIds);
        return  getSuccessResponseVO(null);
    }

    @PostMapping("/delFile")
    @GlobalInterceptor(checkParams = true,checkLogin = true)
    public ResponseVO delFile(HttpSession session, @VerifyParam(required = true) String fileIds){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        this.fileInfoService.delFileBatch(webUserDto.getUserId(),fileIds,false);
        return getSuccessResponseVO(null);
    }

}
