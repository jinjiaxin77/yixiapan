package com.jinjiaxin.yixiapan.mappers;

import com.jinjiaxin.yixiapan.entity.pojo.FileShare;
import com.jinjiaxin.yixiapan.entity.query.FileShareQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分享信息 数据库操作接口
 */
public interface FileShareMapper {

	/**
	 * 根据ShearId更新
	 */
	 Integer updateByShareId(@Param("bean") FileShare t, @Param("shareId") String shareId);

	/**
	 * 根据ShearId删除
	 */
	 Integer deleteByShareId(@Param("shareId") String shareId);

	/**
	 * 根据ShearId获取对象
	 */
	 FileShare selectByShareId(@Param("shareId") String shareId);

	List<FileShare> selectList(@Param("query") FileShareQuery param);

	Integer selectCount(@Param("query") FileShareQuery param);

	Integer insert(@Param("bean") FileShare bean);

	Integer insertBatch(@Param("list") List<FileShare> listBean);

	Integer insertOrUpdateBatch(@Param("list") List<FileShare> listBean);

	Integer updateByParam(@Param("bean") FileShare bean, @Param("query") FileShareQuery param);

	Integer deleteByParam(@Param("query") FileShareQuery param);

	Integer deleteFileShareBatch(@Param("userId") String userId, @Param("shareIdArray") String[] shareIdArray);

    void updateShareShowCount(@Param("shareId") String shareId);
}
