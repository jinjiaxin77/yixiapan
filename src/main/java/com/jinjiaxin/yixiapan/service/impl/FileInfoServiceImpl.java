package com.jinjiaxin.yixiapan.service.impl;

import com.jinjiaxin.yixiapan.component.RedisComponent;
import com.jinjiaxin.yixiapan.entity.config.AppConfig;
import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.dto.SessionWebUserDto;
import com.jinjiaxin.yixiapan.entity.dto.UploadResultDto;
import com.jinjiaxin.yixiapan.entity.dto.UserSpaceDto;
import com.jinjiaxin.yixiapan.entity.enums.*;
import com.jinjiaxin.yixiapan.entity.pojo.FileInfo;
import com.jinjiaxin.yixiapan.entity.pojo.User;
import com.jinjiaxin.yixiapan.entity.query.FileInfoQuery;
import com.jinjiaxin.yixiapan.entity.query.SimplePage;
import com.jinjiaxin.yixiapan.entity.vo.PaginationResultVO;
import com.jinjiaxin.yixiapan.exception.BusinessException;
import com.jinjiaxin.yixiapan.mappers.FileInfoMapper;
import com.jinjiaxin.yixiapan.mappers.UserInfoMapper;
import com.jinjiaxin.yixiapan.service.FileInfoService;
import com.jinjiaxin.yixiapan.utils.DateUtil;
import com.jinjiaxin.yixiapan.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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

	@Autowired
	@Lazy
	private FileInfoService fileInfoService;

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

	/**
	 * 根据FileIdAndUserId修改
	 */
	@Override
	public Integer updateFileInfoByFileIdAndUserId(FileInfo bean, String fileId, String userId) {
		return this.fileInfoMapper.updateByFileIdAndUserId(bean, fileId, userId);
	}

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
		Boolean successUpload = true;
		File tempFileFolder = null;
		try {
			if (StringTools.isEmpty(fileId)) {
				fileId = StringTools.getRandomNumber(Constants.LENGTH_10);
			}
			resultDto.setFileId(fileId);

			UserSpaceDto spaceDto = redisComponent.getUserSpaceDto(userDto.getUserId());
			if (chunkIndex == 0) {
				FileInfoQuery query = new FileInfoQuery();
				query.setFileMd5(fileMd5);
				query.setSimplePage(new SimplePage(0, 1));
				query.setStatus(FileStatusEnum.USING.getStatus());
				List<FileInfo> dbFIleList = this.fileInfoMapper.selectList(query);
				//秒传
				if (!dbFIleList.isEmpty()) {
					FileInfo dbFile = dbFIleList.get(0);
					if (dbFile.getFileSize() + spaceDto.getUseSpace() > spaceDto.getTotalSpace()) {
						throw new BusinessException(ResponseCodeEnum.CODE_904);
					} else {
						uploadSeconds(userDto, fileId, dbFile, fileName, filePid, fileMd5);
						resultDto.setStatus(UploadStatusEnum.UPLOAD_SECONDS.getCode());

						return resultDto;
					}
				}
			}
			//判断磁盘空间
			Long currentTempSize = redisComponent.getFileTempSize(userDto.getUserId(), fileId);
			if (currentTempSize + file.getSize() + spaceDto.getUseSpace() > spaceDto.getTotalSpace()) {
				throw new BusinessException(ResponseCodeEnum.CODE_904);
			}

			//暂存临时目录
			String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
			String curUserFolderName = userDto.getUserId() + fileId;

			tempFileFolder = new File(tempFolderName + curUserFolderName);
			if (!tempFileFolder.exists()) {
				tempFileFolder.mkdirs();
			}

			//上传文件
			File newFile = new File(tempFileFolder.getPath() + "/" + chunkIndex);
			file.transferTo(newFile);
			if (chunkIndex < chunks - 1) {
				resultDto.setStatus(UploadStatusEnum.UPLOADING.getCode());
				redisComponent.saveFileTempSize(userDto.getUserId(), fileId, file.getSize());
				return resultDto;
			}
			//最后一个分片上传完成，记录数据库，异步合成分片
			redisComponent.saveFileTempSize(userDto.getUserId(), fileId, file.getSize());
			String month = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
			Integer index = StringUtils.lastIndexOf(fileName, '.');
			String fileSuffix = fileName.substring(index);
			String realFileName = curUserFolderName + fileSuffix;
			FileTypeEnum fileType = FileTypeEnum.getFileTypeBySuffix(fileSuffix);
			fileName = autoRename(filePid, userDto.getUserId(), realFileName);
			Date date = new Date();
			Long totalSize = redisComponent.getFileTempSize(userDto.getUserId(), fileId);

			FileInfo fileInfo = new FileInfo(fileId, userDto.getUserId(), fileMd5, filePid, null, fileName, null, month + "/" + realFileName, date, date, FileFolderTypeEnum.FILE.getType(), fileType.getCategory().getCategory(), fileType.getType(), FileStatusEnum.TRANSFER.getStatus(), null, FileDelFlagEnums.USING.getFlag());
			fileInfoMapper.insert(fileInfo);

			updateUserSpace(userDto, totalSize);
			resultDto.setStatus(UploadStatusEnum.UPLOAD_FINISH.getCode());

			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					fileInfoService.transferFile(fileInfo.getFileId(),userDto);
				}
			});

			return resultDto;
		}catch (BusinessException e){
			log.error("文件上传失败");
			successUpload = false;
			throw e;
		}catch(Exception e){
			log.error("文件上传失败");
			successUpload = false;
		}finally {
			if(successUpload == false){
				try {
					FileUtils.deleteDirectory(tempFileFolder);
				} catch (IOException e) {
					log.error("删除临时目录失败",e);
				}
			}
		}

		return resultDto;
	}

	@Async
	public void transferFile(String fileId, SessionWebUserDto userDto){
		Boolean transferSuccess = true;
		String targetFilePath = null;
		String cover = null;
		FileTypeEnum fileType = null;
		FileInfo fileInfo = this.fileInfoMapper.selectByFileIdAndUserId(fileId,userDto.getUserId());
		try{
			if(fileInfo == null || !FileStatusEnum.TRANSFER.getStatus().equals(fileInfo.getStatus())){
				return;
			}
			//找到临时目录
			String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
			String currentUserFolderName = userDto.getUserId() + fileId;
			File fileFolder = new File(tempFolderName + currentUserFolderName);

			String fileSuffix = fileInfo.getFileName().substring(StringUtils.lastIndexOf(fileInfo.getFileName(),'.'));
			String month = DateUtil.format(fileInfo.getCreateTime(),DateTimePatternEnum.YYYYMM.getPattern());

			//目标目录
			String targetFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
			File targetFolder = new File(targetFolderName + "/" + month);
			if(!targetFolder.exists()){
				targetFolder.mkdirs();
			}
			//真实的文件名
			String realFileName = currentUserFolderName + fileSuffix;
			targetFilePath = targetFolder.getPath() + "/" + realFileName;

			//合并文件
			union(fileFolder.getPath(),targetFilePath,fileInfo.getFileName(),true);

			//视频文件切割
			fileType = FileTypeEnum.getFileTypeBySuffix(fileSuffix);
			if(fileType == FileTypeEnum.VIDEO){

			}else if(fileType == FileTypeEnum.IMAGE){

			}
		}catch(Exception e){
			log.error("文件转码失败，文件ID:{},userId:{}",fileId,userDto.getUserId(),e);
			transferSuccess = false;
		}finally {
			FileInfo updateInfo = new FileInfo();
			updateInfo.setFileSize(new File(targetFilePath).length());
			updateInfo.setFileCover(cover);
			updateInfo.setStatus(transferSuccess?FileStatusEnum.USING.getStatus() : FileStatusEnum.TRANSFER_FALT.getStatus());

			fileInfoMapper.updateFileByFileUserIdAndOldStatus(fileId,userDto.getUserId(),FileStatusEnum.TRANSFER.getStatus(), updateInfo);
		}
	}

	private void cutFileForVideo(String fileId, String videoFilePath){
		//创建同名切片目录
		File tsFolder = new File(videoFilePath.substring(0,videoFilePath.lastIndexOf('.')));
		if(!tsFolder.exists()){
			tsFolder.mkdirs();
		}
		final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s  -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";
		final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";
		String tsPath = tsFolder + "/" + Constants.TS_NAME;
		//生产.ts
		String cmd = String.format(CMD_TRANSFER_2TS,videoFilePath,tsPath);

	}

	private void union(String dirPath, String toFilePath, String fileName, Boolean delSource){
		File dir = new File(dirPath);
		if(!dir.exists()){
			throw new BusinessException("目录不存在");
		}

		File[] fileList = dir.listFiles();
		File targetFile = new File(toFilePath);
		RandomAccessFile writerFile = null;
		try{
			writerFile = new RandomAccessFile(targetFile,"rw");
			byte[] bw = new byte[1024*10];
			for(int i = 0; i < fileList.length; i++){
				int len = -1;
				File chunkFile = new File(dirPath + "/" + i);
				RandomAccessFile readFile = null;
				try{
					readFile = new RandomAccessFile(chunkFile,"r");
					while((len= readFile.read(bw)) != -1){
						writerFile.write(bw,0,len);
					}
				}catch(Exception e){
					log.error("合并分片失败",e);
					throw new BusinessException("合并分片失败");
				}finally {
					readFile.close();
				}
			}
		}catch(Exception e){
			log.error("合并文件失败");
			throw new BusinessException("合并文件" + fileName + "出错了");
		}finally {
			if(null != writerFile){
				try{
					writerFile.close();
				} catch (IOException e) {
                    e.printStackTrace();
                }
            }
			if(delSource&&dir.exists()){
				try {
					FileUtils.deleteDirectory(dir);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void updateUserSpace(SessionWebUserDto userDto, Long fileSize){
		UserSpaceDto spaceDto = redisComponent.getUserSpaceDto(userDto.getUserId());
		Long useSpace = spaceDto.getUseSpace() + fileSize;
		Integer count = userInfoMapper.updateUseSpace(userDto.getUserId(), useSpace,null);
		if (count == 0) {
			throw new BusinessException(ResponseCodeEnum.CODE_904);
		}
		spaceDto.setUseSpace(useSpace);
		redisComponent.saveUserSpace(userDto.getUserId(), spaceDto);
	}

	private void uploadSeconds(SessionWebUserDto userDto, String fileId, FileInfo dbFile, String fileName, String filePid, String fileMd5){
		Date curDate = new Date();
		UserSpaceDto spaceDto = redisComponent.getUserSpaceDto(userDto.getUserId());

		dbFile.setFileId(fileId);
		dbFile.setFilePid(filePid);
		dbFile.setUserId(userDto.getUserId());
		dbFile.setCreateTime(curDate);
		dbFile.setLastUpdateTime(curDate);
		dbFile.setStatus(FileStatusEnum.USING.getStatus());
		dbFile.setDelFlag(FileDelFlagEnums.USING.getFlag());
		dbFile.setFileMd5(fileMd5);
		fileName = autoRename(filePid, userDto.getUserId(), fileName);
		dbFile.setFileName(fileName);

		fileInfoMapper.insert(dbFile);

		Long useSpace = spaceDto.getUseSpace() + dbFile.getFileSize();
		Integer count = userInfoMapper.updateUseSpace(userDto.getUserId(), useSpace,null);
		if (count == 0) {
			throw new BusinessException(ResponseCodeEnum.CODE_904);
		}
		spaceDto.setUseSpace(useSpace);
		redisComponent.saveUserSpace(userDto.getUserId(), spaceDto);
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