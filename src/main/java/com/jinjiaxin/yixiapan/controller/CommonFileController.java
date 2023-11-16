package com.jinjiaxin.yixiapan.controller;

import com.jinjiaxin.yixiapan.component.RedisComponent;
import com.jinjiaxin.yixiapan.entity.config.AppConfig;
import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.dto.DownloadFileDto;
import com.jinjiaxin.yixiapan.entity.enums.FileCategoryEnums;
import com.jinjiaxin.yixiapan.entity.enums.FileFolderTypeEnum;
import com.jinjiaxin.yixiapan.entity.enums.ResponseCodeEnum;
import com.jinjiaxin.yixiapan.entity.pojo.FileInfo;
import com.jinjiaxin.yixiapan.entity.query.FileInfoQuery;
import com.jinjiaxin.yixiapan.entity.vo.FileInfoVO;
import com.jinjiaxin.yixiapan.entity.vo.ResponseVO;
import com.jinjiaxin.yixiapan.exception.BusinessException;
import com.jinjiaxin.yixiapan.service.FileInfoService;
import com.jinjiaxin.yixiapan.utils.CopyTools;
import com.jinjiaxin.yixiapan.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class CommonFileController extends ABaseController {

    @Resource
    private AppConfig appConfig;

    @Resource
    private FileInfoService fileInfoService;

    @Resource
    private RedisComponent redisComponent;

    protected void getImage(HttpServletResponse response, String imageFolder, String imageName){
        if(StringTools.isEmpty(imageFolder) || StringTools.isEmpty(imageName)){
            return;
        }
        if(!StringTools.pathIsOk(imageFolder) || !StringTools.pathIsOk(imageName)){
            return;
        }
        String imageSuffix = imageName.substring(imageName.lastIndexOf('.'));
        String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + imageFolder + "/" + imageName;
        imageSuffix = imageSuffix.replace(".","");
        String contentType = "image/" + imageSuffix;
        response.setContentType(contentType);
        response.setHeader("Cache-Control","max-age=2592000");
        readFile(response,filePath);
    }

    protected void getFile(HttpServletResponse response, String fileId, String userId){
        String filePath = null;
        if(!fileId.endsWith(".ts")){
            FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId,userId);
            if(fileInfo == null){
                return;
            }
            filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileInfo.getFilePath();
            if(FileCategoryEnums.VIDEO.getCategory().equals(fileInfo.getFileCategory())){
                filePath = filePath.substring(0,filePath.lastIndexOf('.')) + "/" + Constants.M3U8_NAME;
            }
            File file = new File(filePath);
            if(!file.exists()){
                return;
            }
        }else{
            String[] tsArray = fileId.split("_");
            String realFileId = tsArray[0];
            FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(realFileId,userId);
            String fileName = fileInfo.getFilePath().substring(0,fileInfo.getFilePath().lastIndexOf('.')) + "/" + fileId;
            filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileName;
        }

        readFile(response,filePath);
    }

    protected ResponseVO getFolderInfo(String path, String userId){
        String[] pathArray = path.split("/");
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setFolderType(FileFolderTypeEnum.FILE.getType());
        fileInfoQuery.setFileIdArray(pathArray);
        String orderBy = "field(file_id,\"" + StringUtils.join(pathArray,"\",\"") + "\")";
        fileInfoQuery.setOrderBy(orderBy);
        List<FileInfo> fileInfoList = fileInfoService.findListByParam(fileInfoQuery);
        return getSuccessResponseVO(CopyTools.copyList(fileInfoList, FileInfoVO.class));
    }

    protected ResponseVO createDownloadUrl(String fileId, String userId){
        FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId,userId);
        if(fileInfo == null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if(FileFolderTypeEnum.FOLDER.getType().equals(fileInfo.getFolderType())){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String code = StringTools.getRandomNumber(Constants.LENGTH_50);

        DownloadFileDto fileDto = new DownloadFileDto();
        fileDto.setFileName(fileInfo.getFileName());
        fileDto.setFilePath(fileInfo.getFilePath());
        fileDto.setDownloadCode(code);

        redisComponent.saveDownloadCode(code,fileDto);

        return getSuccessResponseVO(code);
    }

    protected void download(HttpServletRequest request, HttpServletResponse response, String code) throws UnsupportedEncodingException {
        DownloadFileDto downloadFileDto = redisComponent.getDownloadCode(code);
        if(downloadFileDto == null){
            return;
        }
        String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + downloadFileDto.getFilePath();
        String fileName = downloadFileDto.getFileName();
        response.setContentType("application/x-msdownload; charset=UTF-8");
        if(request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0){
            fileName = URLEncoder.encode(fileName,"UTF-8");
        }else{
            fileName = new String(fileName.getBytes("UTF-8"),"ISO8859-1");
        }
        response.setHeader("Content-Disposition","attachment;filename=\"" + fileName + "\"");
        readFile(response,filePath);
    }

}
