package com.jinjiaxin.yixiapan.mappers;

import com.jinjiaxin.yixiapan.entity.pojo.FileInfo;
import com.jinjiaxin.yixiapan.entity.query.FileInfoQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  数据库操作接口
 */
public interface FileInfoMapper {

	Integer insert(@Param("file") FileInfo param);

	void insertBatch(@Param("list") List<FileInfo> fileInfoList);

	/**
	 * 根据FileIdAndUserId更新
	 */
	 Integer updateByFileIdAndUserId(@Param("bean") FileInfo t, @Param("fileId") String fileId, @Param("userId") String userId);


	/**
	 * 根据FileIdAndUserId删除
	 */
	 Integer deleteByFileIdAndUserId(@Param("fileId") String fileId,@Param("userId") String userId);


	/**
	 * 根据FileIdAndUserId获取对象
	 */
	 FileInfo selectByFileIdAndUserId(@Param("fileId") String fileId,@Param("userId") String userId);


    List<FileInfo> selectList(@Param("file")FileInfoQuery query);

	Integer selectCount(@Param("file") FileInfoQuery fileInfo);

	Long selectUseSpace(@Param("userId") String userId);

	Integer selectCountByParams(@Param("file") FileInfo fileInfo);

    void updateFileByFileUserIdAndOldStatus(@Param("fileId")String fileId, @Param("userId") String userId, @Param("oldStatus") Integer status, @Param("bean") FileInfo updateInfo);

	int updateFileDelFlagBatch(@Param("bean")FileInfo fileInfo, @Param("userId") String userId, @Param("filePidList") List<String> filePidList, @Param("fileIdList") List<String> fileIdList, @Param("oldDelFlag") Integer oldDelFlag);

	void delFileBatch(@Param("userId") String userId, @Param("filePidList") List<String> filePidList, @Param("fileIdList") List<String> fileIdList, @Param("oldDelFlag") Integer oldDelFlag);

    void deleteAllFileByUserId(@Param("userId") String userId);
}
