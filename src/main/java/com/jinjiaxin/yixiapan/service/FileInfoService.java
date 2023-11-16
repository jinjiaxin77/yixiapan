package com.jinjiaxin.yixiapan.service;

import com.jinjiaxin.yixiapan.entity.dto.SessionWebUserDto;
import com.jinjiaxin.yixiapan.entity.dto.UploadResultDto;
import com.jinjiaxin.yixiapan.entity.pojo.FileInfo;
import com.jinjiaxin.yixiapan.entity.query.FileInfoQuery;
import com.jinjiaxin.yixiapan.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;



/**
 *  业务接口
 */
public interface FileInfoService {

	/**
	 * 根据条件查询列表
	 */
	List<FileInfo> findListByParam(FileInfoQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(FileInfoQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<FileInfo> findListByPage(FileInfoQuery param);

	/**
	 * 新增
	 */
	Integer add(FileInfo bean);

	/**
	 * 根据FileIdAndUserId查询对象
	 */
	FileInfo getFileInfoByFileIdAndUserId(String fileId,String userId);


	/**
	 * 根据FileIdAndUserId修改
	 */
	Integer updateFileInfoByFileIdAndUserId(FileInfo bean,String fileId,String userId);


	/**
	 * 根据FileIdAndUserId删除
	 */
	Integer deleteFileInfoByFileIdAndUserId(String fileId,String userId);

	UploadResultDto uploadFile(SessionWebUserDto userDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunks);

    void transferFile(String fileId, SessionWebUserDto userDto);

    FileInfo newFolder(String filePid, String userId, String folderName);

	FileInfo rename(String userId, String fileId, String fileName);

	void changeFileFolder(String fileIds, String filePid, String userId);

	void removeFile2RecycleBatch(String userId, String fileIds);

	void recoverFileBatch(String userId, String fileIds);

	void delFileBatch(String userId, String fileIds, Boolean adminOp);

	void checkRootFilePid(String rootFilePid, String userId, String fileId);

	void saveShare(String shareRootFilePid, String shareFileIds, String myFolderId, String shareUserId, String currentUserId);
}