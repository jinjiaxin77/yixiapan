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
import com.jinjiaxin.yixiapan.utils.ProcessUtils;
import com.jinjiaxin.yixiapan.utils.ScaleFilter;
import com.jinjiaxin.yixiapan.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


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
			redisComponent.saveFileTempSize(userDto.getUserId(), fileId, file.getSize());
			if (chunkIndex < chunks - 1) {
				resultDto.setStatus(UploadStatusEnum.UPLOADING.getCode());
				return resultDto;
			}
			//最后一个分片上传完成，记录数据库，异步合成分片
			String month = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
			Integer index = StringUtils.lastIndexOf(fileName, '.');
			String fileSuffix = fileName.substring(index);
			String realFileName = curUserFolderName + fileSuffix;
			FileTypeEnum fileType = FileTypeEnum.getFileTypeBySuffix(fileSuffix);
			fileName = autoRename(filePid, userDto.getUserId(), realFileName);
			Date date = new Date();
			Long totalSize = redisComponent.getFileTempSize(userDto.getUserId(), fileId);

			FileInfo fileInfo = new FileInfo(fileId, userDto.getUserId(), fileMd5, filePid, null, fileName, null, month + "/" + realFileName, date, date, FileFolderTypeEnum.FILE.getType(), fileType.getCategory().getCategory(), fileType.getType(), FileStatusEnum.TRANSFER.getStatus(), null, FileDelFlagEnums.USING.getFlag(),null);
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
				cutFileForVideo(fileId,targetFilePath);
				//视频生成缩略图
				cover = month + "/" + currentUserFolderName + Constants.IMAGE_PNG_SUFFIX;
				String coverPath = targetFolderName + "/" + cover;
				ScaleFilter.createCover4Video(new File(targetFilePath),Constants.LENGTH_150,new File(coverPath));
			}else if(fileType == FileTypeEnum.IMAGE){
				//生成缩略图
				cover = month + "/" + realFileName.replace(".","_.");
				String coverPath = targetFolderName + "/" + cover;
				Boolean created = ScaleFilter.createThumbnailWidthFFmpeg(new File(targetFilePath),Constants.LENGTH_150,new File(coverPath),false);
				if(!created){
					FileCopyUtils.copy(new File(targetFilePath),new File(coverPath));
				}
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

	@Override
	public FileInfo newFolder(String filePid, String userId, String folderName) {
		checkFileName(filePid,userId,folderName,FileFolderTypeEnum.FOLDER.getType());
		Date curDate = new Date();
		FileInfo fileInfo = new FileInfo();
		fileInfo.setFileId(StringTools.getRandomNumber(Constants.LENGTH_10));
		fileInfo.setUserId(userId);
		fileInfo.setFilePid(filePid);
		fileInfo.setFileName(folderName);
		fileInfo.setFolderType(FileFolderTypeEnum.FOLDER.getType());
		fileInfo.setCreateTime(curDate);
		fileInfo.setLastUpdateTime(curDate);
		fileInfo.setStatus(FileStatusEnum.USING.getStatus());
		fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
		fileInfoMapper.insert(fileInfo);

		return fileInfo;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public FileInfo rename(String userId, String fileId, String fileName) {
		FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId,userId);
		if(fileInfo == null){
			throw new BusinessException("当前文件不存在");
		}
		//校验新名字是否重复
		String filePid = fileInfo.getFilePid();
		checkFileName(filePid,userId,fileName,fileInfo.getFolderType());
		//为新名字加上文件后缀
		if(FileFolderTypeEnum.FILE.getType().equals(fileInfo.getFolderType())){
			fileName = fileName + fileInfo.getFileName().substring(fileInfo.getFileName().lastIndexOf('.'));
		}
		FileInfo newFileInfo = new FileInfo();
		Date date = new Date();
		newFileInfo.setFileName(fileName);
		newFileInfo.setLastUpdateTime(date);
		fileInfoMapper.updateByFileIdAndUserId(newFileInfo, fileId, userId);

		FileInfoQuery query = new FileInfoQuery();
		query.setFileId(fileId);
		query.setUserId(userId);
		query.setFileName(fileName);
		query.setDelFlag(FileDelFlagEnums.USING.getFlag());
		Integer i = fileInfoMapper.selectCount(query);
		if(i > 1){
			throw new BusinessException("文件名已经存在");
		}
		fileInfo.setFileName(fileName);
		fileInfo.setLastUpdateTime(date);
		return fileInfo;
	}

	private void checkFileName(String filePid, String userId, String fileName, Integer folderType){
		FileInfoQuery fileInfo = new FileInfoQuery();
		fileInfo.setFilePid(filePid);
		fileInfo.setFolderType(folderType);
		fileInfo.setUserId(userId);
		fileInfo.setFileName(fileName);
		fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
		Integer count = fileInfoMapper.selectCount(fileInfo);
		if(count > 0){
			throw new BusinessException("此目录下已经存在同名文件，请修改名称");
		}
	}

	@Override
	public void changeFileFolder(String fileIds, String filePid, String userId) {
		if(fileIds.equals(filePid)){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(!Constants.ZERO_STR.equals(filePid)){
			FileInfo fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(filePid,userId);
			if(fileInfo == null || FileDelFlagEnums.DEL.getFlag().equals(fileInfo.getDelFlag())){
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
		}
		String[] fileIdArray = fileIds.split(",");
		//判断是否存在重名，若存在重名，则对即将放入文件重命名
		FileInfoQuery query = new FileInfoQuery();
		query.setFilePid(filePid);
		query.setUserId(userId);
		List<FileInfo> list = fileInfoService.findListByParam(query);
		Map<String,FileInfo> fileNameMap = list.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(),(file1,file2) -> file2));
		query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		List<FileInfo> selectList = fileInfoService.findListByParam(query);

		for(FileInfo fileInfo : selectList){
			FileInfo updateQuery = new FileInfo();
			if(fileNameMap.containsKey(fileInfo.getFileName())){
				String name = fileInfo.getFileName();
				updateQuery.setFileName(name.substring(0,name.lastIndexOf('.')) + "_" + StringTools.getRandomNumber(Constants.LENGTH_5) + name.substring(name.lastIndexOf('.')));
			}
			updateQuery.setFilePid(filePid);
			fileInfoMapper.updateByFileIdAndUserId(updateQuery,fileInfo.getFileId(),userId);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeFile2RecycleBatch(String userId, String fileIds) {
		String[] fileIdArray = fileIds.split(",");
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		query.setDelFlag(FileDelFlagEnums.USING.getFlag());
		List<FileInfo> fileInfoList = fileInfoMapper.selectList(query);
		if(fileInfoList.isEmpty()){
			return;
		}
		//更新所有包含在文件夹中的文件(夹)
		List<String> delFilePidList = new ArrayList<>();
		for(FileInfo fileInfo : fileInfoList){
			findAllPid(delFilePidList,userId,fileInfo.getFileId(),FileDelFlagEnums.USING.getFlag());
		}
		FileInfo updateFileInfo = new FileInfo();
		updateFileInfo.setRecoveryTime(new Date());
		if(!delFilePidList.isEmpty()){
			updateFileInfo.setDelFlag(FileDelFlagEnums.DEL.getFlag());
			this.fileInfoMapper.updateFileDelFlagBatch(updateFileInfo,userId,delFilePidList,null,FileDelFlagEnums.USING.getFlag());
		}
		//更新不包含在文件夹中的文件(夹)
		List<String> fileIdList = Arrays.asList(fileIdArray);
		updateFileInfo.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
		this.fileInfoMapper.updateFileDelFlagBatch(updateFileInfo,userId,null,fileIdList,FileDelFlagEnums.USING.getFlag());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void recoverFileBatch(String userId, String fileIds) {
		String[] fileIdArray = fileIds.split(",");
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
		List<FileInfo> fileInfoList = fileInfoMapper.selectList(query);
		if(fileInfoList.isEmpty()){
			return;
		}
		//更新所有包含在文件夹中的文件(夹)
		List<String> recoverFilePidList = new ArrayList<>();
		for(FileInfo fileInfo : fileInfoList){
			if(FileFolderTypeEnum.FOLDER.getType().equals(fileInfo.getFolderType())) findAllPid(recoverFilePidList,userId,fileInfo.getFileId(),FileDelFlagEnums.DEL.getFlag());
		}
		//由于文件将被还原到根目录下，所以应该先查询根目录下所有文件，防止重名
		query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFilePid(Constants.ZERO_STR);
		query.setDelFlag(FileDelFlagEnums.USING.getFlag());
		List<FileInfo> allRootFileInfoList = this.findListByParam(query);
		Map<String,FileInfo> rootFileNameMap = allRootFileInfoList.stream().collect(Collectors.toMap(FileInfo::getFileName, Function.identity(),(file1,file2) -> file2));

		//查询所有所选文件，将目录下的所有删除的文件更新为USING
		FileInfo updateFileInfo = new FileInfo();
		updateFileInfo.setLastUpdateTime(new Date());
		updateFileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
		if(!recoverFilePidList.isEmpty()){
			this.fileInfoMapper.updateFileDelFlagBatch(updateFileInfo,userId,recoverFilePidList,null,FileDelFlagEnums.DEL.getFlag());
		}

		//更新不包含在文件夹中的文件(夹)，若命名重复，则重命名
		List<String> fileIdList = Arrays.asList(fileIdArray);
		updateFileInfo.setFilePid(Constants.ZERO_STR);
		this.fileInfoMapper.updateFileDelFlagBatch(updateFileInfo,userId,null,fileIdList,FileDelFlagEnums.RECYCLE.getFlag());

		//将所选文件重命名
		for(FileInfo fileInfo : fileInfoList){
			if(rootFileNameMap.containsKey(fileInfo.getFileName())){
				String former = fileInfo.getFileName();
				String newName;
				if(fileInfo.getFolderType().equals(FileFolderTypeEnum.FILE.getType())){
					newName = former.substring(0,former.lastIndexOf(".")) + "_" + StringTools.getRandomNumber(Constants.LENGTH_5) + former.substring(former.lastIndexOf("."));
				}else{
					newName = former + "_" + StringTools.getRandomNumber(Constants.LENGTH_5);
				}
				FileInfo query1 = new FileInfo();
				query1.setFileName(newName);
				this.fileInfoMapper.updateByFileIdAndUserId(query1,fileInfo.getFileId(),userId);
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delFileBatch(String userId, String fileIds, Boolean adminOp) {
		String[] fileIdArray = fileIds.split(",");
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(userId);
		query.setFileIdArray(fileIdArray);
		query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
		List<FileInfo> fileInfoList = this.fileInfoMapper.selectList(query);

		List<String> filePidList = new ArrayList<>();
		for(FileInfo file : fileInfoList){
			if(FileFolderTypeEnum.FOLDER.getType().equals(file.getFolderType())){
				findAllPid(filePidList,userId,file.getFileId(),FileDelFlagEnums.DEL.getFlag());
			}
		}

		if(!filePidList.isEmpty()){
			this.fileInfoMapper.delFileBatch(userId,filePidList,null,adminOp?null:FileDelFlagEnums.DEL.getFlag());
		}

		List<String> fileIdList = Arrays.asList(fileIdArray);
		this.fileInfoMapper.delFileBatch(userId,null,fileIdList,adminOp?null:FileDelFlagEnums.RECYCLE.getFlag());

		Long useSpace = this.fileInfoMapper.selectUseSpace(userId);
		User user = new User();
		user.setUseSpace(useSpace);
		this.userInfoMapper.updateByUserId(userId,user);

		UserSpaceDto userSpaceDto = redisComponent.getUserSpaceDto(userId);
		userSpaceDto.setUseSpace(useSpace);
		redisComponent.saveUserSpace(userId,userSpaceDto);
	}

	private void findAllPid(List<String> fileInfoList, String userId, String fileId, Integer delFlag){
		fileInfoList.add(fileId);

		FileInfoQuery fileInfo = new FileInfoQuery();
		fileInfo.setFilePid(fileId);
		fileInfo.setUserId(userId);
		fileInfo.setDelFlag(delFlag);
		fileInfo.setFolderType(FileFolderTypeEnum.FOLDER.getType());
		List<FileInfo> list = this.fileInfoMapper.selectList(fileInfo);

		if(list.isEmpty()){
			return;
		}
		for(FileInfo fileInfo1 : list){
			findAllPid(fileInfoList,userId,fileInfo1.getFileId(),delFlag);
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
		//生成.ts文件
		String cmd = String.format(CMD_TRANSFER_2TS,videoFilePath,tsPath);
		ProcessUtils.executeCommand(cmd,false);
		//生成索引文件.m3u8和切片.ts
		cmd = String.format(CMD_CUT_TS,tsPath,tsFolder.getPath() + "/" + Constants.M3U8_NAME,tsFolder.getPath(),fileId);
		ProcessUtils.executeCommand(cmd,false);
		//删除index.ts
		new File(tsPath).delete();
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

	@Override
	public void checkRootFilePid(String rootFilePid, String userId, String fileId) {
		if(StringTools.isEmpty(rootFilePid)){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(rootFilePid.equals(fileId)){
			return;
		}
		checkFilePid(rootFilePid,fileId,userId);
	}

	@Override
	public void saveShare(String shareRootFilePid, String shareFileIds, String myFolderId, String shareUserId, String currentUserId) {
		String[] shareFileIdArray = shareFileIds.split(",");
		FileInfoQuery query = new FileInfoQuery();
		query.setUserId(currentUserId);
		query.setFilePid(myFolderId);
		List<FileInfo> currentFIleList = this.fileInfoMapper.selectList(query);
		Map<String,FileInfo> currentFileMap = currentFIleList.stream().collect(Collectors.toMap(FileInfo::getFileName,Function.identity(),(date1,date2) -> date2));

		query = new FileInfoQuery();
		query.setUserId(shareUserId);
		query.setFileIdArray(shareFileIdArray);
		List<FileInfo> shareFileList = this.fileInfoMapper.selectList(query);
		List<FileInfo> copyFileList = new ArrayList<>();
		Date curDate = new Date();
		for( FileInfo fileInfo : shareFileList){
			FileInfo haveFile = currentFileMap.get(fileInfo.getFileName());
			if(haveFile != null){
				String name = fileInfo.getFileName();
				String newName = "";
				if(fileInfo.getFolderType().equals(FileFolderTypeEnum.FOLDER.getType())){
					newName = name + "_" + StringTools.getRandomNumber(Constants.LENGTH_5);
				}else{
					newName = name.substring(0,name.lastIndexOf(".")) + "_" + StringTools.getRandomNumber(Constants.LENGTH_5) + name.substring(name.lastIndexOf("."));
				}
				fileInfo.setFileName(newName);
			}
			findAllSubFile(copyFileList,fileInfo,shareUserId,currentUserId,curDate,myFolderId);
		}
		this.fileInfoMapper.insertBatch(copyFileList);
	}

	private void findAllSubFile(List<FileInfo> copyFileList, FileInfo fileInfo, String sourceUserId, String currentUserId, Date curDate, String newFilePid){
		String sourceFileId = fileInfo.getFileId();
		fileInfo.setCreateTime(curDate);
		fileInfo.setLastUpdateTime(curDate);
		fileInfo.setFilePid(newFilePid);
		fileInfo.setUserId(currentUserId);
		String newFileId = StringTools.getRandomNumber(Constants.LENGTH_10);
		fileInfo.setFileId(newFileId);
		copyFileList.add(fileInfo);
		if(FileFolderTypeEnum.FOLDER.getType().equals(fileInfo.getFolderType())){
			FileInfoQuery query = new FileInfoQuery();
			query.setFilePid(sourceFileId);
			query.setUserId(sourceUserId);
			List<FileInfo> sourceFileList = this.fileInfoMapper.selectList(query);
			for(FileInfo item : sourceFileList){
				findAllSubFile(copyFileList,item,sourceUserId,currentUserId,curDate,newFileId);
			}
		}
	}

	private void checkFilePid(String rootFilePid, String fileId, String userId){
		FileInfo fileInfo = this.fileInfoMapper.selectByFileIdAndUserId(fileId,userId);
		if(fileInfo == null){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(Constants.ZERO_STR.equals(fileInfo.getFilePid())){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(fileInfo.getFilePid().equals(rootFilePid)){
			return;
		}
		checkFilePid(rootFilePid,fileInfo.getFilePid(),userId);
	}

}