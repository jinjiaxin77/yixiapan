package com.jinjiaxin.yixiapan.mappers;

import com.jinjiaxin.yixiapan.entity.pojo.User;
import org.apache.ibatis.annotations.Param;

/**
 * 用户信息 数据库操作接口
 */

public interface UserInfoMapper{

	void add(@Param("user") User user);

	/**
	 * 根据UserId更新
	 */
	 Integer update(@Param("user") User user);



	/**
	 * 根据UserId删除
	 */
	 Integer deleteByUserId(@Param("userId") String userId);


	/**
	 * 根据UserId获取对象
	 */
	 User selectByUserId(@Param("userId") String userId);


	/**
	 * 根据Email删除
	 */
	 Integer deleteByEmail(@Param("email") String email);


	/**
	 * 根据Email获取对象
	 */
	 User selectByEmail(@Param("email") String email);


	/**
	 * 根据QqOpenId删除
	 */
	 Integer deleteByQqOpenId(@Param("qqOpenId") String qqOpenId);


	/**
	 * 根据QqOpenId获取对象
	 */
	 User selectByQqOpenId(@Param("qqOpenId") String qqOpenId);


	/**
	 * 根据NickName删除
	 */
	 Integer deleteByNickName(@Param("nickName") String nickName);


	/**
	 * 根据NickName获取对象
	 */
	 User selectByNickName(@Param("nickName") String nickName);


}
