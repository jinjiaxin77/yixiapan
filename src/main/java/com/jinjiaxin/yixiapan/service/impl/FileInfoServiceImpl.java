package com.jinjiaxin.yixiapan.service.impl;

import com.jinjiaxin.yixiapan.component.RedisComponent;
import com.jinjiaxin.yixiapan.entity.config.AppConfig;
import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.dto.SessionWebUserDto;
import com.jinjiaxin.yixiapan.entity.dto.UploadResultDto;
import com.jinjiaxin.yixiapan.entity.dto.UserSpaceDto;
import com.jinjiaxin.yixiapan.entity.enums.*;
import com.jinjiaxin.yixiapan.entity.pojo.FileInfo;
import com.jinjiaxin.yixiapan.entity.query.FileInfoQuery;
import com.jinjiaxin.yixiapan.entity.query.SimplePage;
import com.jinjiaxin.yixiapan.entity.vo.PaginationResultVO;
import com.jinjiaxin.yixiapan.exception.BusinessException;
import com.jinjiaxin.yixiapan.mappers.FileInfoMapper;
import com.jinjiaxin.yixiapan.mappers.UserInfoMapper;
import com.jinjiaxin.yixiapan.service.FileInfoService;
import com.jinjiaxin.yixiapan.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.List;



/**
 *  业务接口实现
 */
@Service("fileInfoService")
@Slf4j
public class FileInfoServiceImpl implements FileInfoService {

	@Autowired
	private FileInfoMapper fileInfoMapper;

	@Autowired
	private RedisComponent redisComponent;

	@Autowired
	private UserInfoMapper userInfoMapper;

	@Autowired
	private AppConfig appConfig;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<FileInfo> findListByParam(FileInfoQuery param) {
		return this.fileInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(FileInfoQuery param) {
		return this.fileInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<FileInfo> findListByPage(FileInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<FileInfo> list = this.findListByParam(param);
		PaginationResultVO<FileInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(FileInfo bean) {
		return this.fileInfoMapper.insert(bean);
	}

	/**
	 * 根据FileIdAndUserId获取对象
	 */
	@Override
	public FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId) {
		return this.fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
	}

	@Override
	public Integer updateFileInfoByFileIdAndUserId(FileInfo bean, String fileId, String userId) {
		return this.fileInfoMapper.updateByFileIdAndUserId(bean, fileId, userId);
	}

	/**
	 * 根据FileIdAndUserId修改
	 */

	/**
	 * 根据FileIdAndUserId删除
	 */
	@Override
	public Integer deleteFileInfoByFileIdAndUserId(String fileId, String userId) {
		return this.fileInfoMapper.deleteByFileIdAndUserId(fileId, userId);
	}

	/**
	 * 文件上传
	 *
	 * @param userDto
	 * @param fileId
	 * @param file
	 * @param fileName
	 * @param fileMd5
	 * @param chunkIndex
	 * @param chunks
	 */
	@Override
	public UploadResultDto uploadFile(SessionWebUserDto userDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunks) {
		UploadResultDto resultDto = new UploadResultDto();
		try{
			if(StringTools.isEmpty(fileId)){
				fileId = StringTools.getRandomNumber(Constants.LENGTH_10);
			}
			resultDto.setFileId(fileId);

			Date curDate = new Date();
			UserSpaceDto spaceDto = redisComponent.getUserSpaceDto(userDto.getUserId());
			if(chunkIndex == 0){
				FileInfoQuery query = new FileInfoQuery();
				query.setFileMd5(fileMd5);
				query.setSimplePage(new SimplePage(0,1));
				query.setStatus(FileStatusEnum.USING.getStatus());
				List<FileInfo> dbFIleList = this.fileInfoMapper.selectList(query);
				//秒传
				if(!dbFIleList.isEmpty()){
					FileInfo dbFile = dbFIleList.get(0);
					if(dbFile.getFileSize() + spaceDto.getUseSpace() > spaceDto.getTotalSpace()){
						throw new BusinessException(ResponseCodeEnum.CODE_904);
					}else{
						dbFile.setFileId(fileId);
						dbFile.setFilePid(filePid);
						dbFile.setUserId(userDto.getUserId());
						dbFile.setCreateTime(curDate);
						dbFile.setLastUpdateTime(curDate);
						dbFile.setStatus(FileStatusEnum.USING.getStatus());
						dbFile.setDelFlag(FileDelFlagEnums.USING.getFlag());
						dbFile.setFileMd5(fileMd5);
						fileName = autoRename(filePid,userDto.getUserId(),fileName);
						dbFile.setFileName(fileName);

						fileInfoMapper.insert(dbFile);
						resultDto.setStatus(UploadStatusEnum.UPLOAD_SECONDS.getCode());

						Long useSpace = spaceDto.getUseSpace() + dbFile.getFileSize();
						Integer count = userInfoMapper.updateUseSpace(userDto.getUserId(),useSpace);
						if(count == 0){
							throw new BusinessException(ResponseCodeEnum.CODE_904);
						}
						spaceDto.setUseSpace(useSpace);
						redisComponent.saveUserSpace(userDto.getUserId(), spaceDto);

						return resultDto;
					}
				}

				//判断磁盘空间
				Long currentTempSize = redisComponent.getFileTempSize(userDto.getUserId(),fileId);
				if(currentTempSize + file.getSize() + spaceDto.getUseSpace() > spaceDto.getTotalSpace()){
					throw new BusinessException(ResponseCodeEnum.CODE_904);
				}

				//暂存临时目录
				String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
				String curUserFolderName = userDto.getUserId() + fileId;

				File tempFileFolder = new File(tempFolderName + curUserFolderName);
				if(!tempFileFolder.exists()){
					tempFileFolder.mkdirs();
				}

				//上传文件
				File newFile = new File(tempFileFolder.getPath() + "/" + chunkIndex);
				file.transferTo(newFile);
			}
		}catch(Exception e){
			log.error("文件上传失败");
		}

		return resultDto;
	}

	private String autoRename(String filePid, String userId, String fileName){
		String newName = fileName;
		FileInfo fileInfo = new FileInfo();
		fileInfo.setFileName(fileName);
		fileInfo.setFilePid(filePid);
		fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
		fileInfo.setUserId(userId);
		Integer count = fileInfoMapper.selectCountByParams(fileInfo);
		if(count > 0){
			Integer index = StringUtils.lastIndexOf(fileName,'.');
			newName = fileName.substring(0,index) + "_" + StringTools.getRandomNumber(Constants.LENGTH_5) + fileName.substring(index);
		}

		return newName;
	}

}