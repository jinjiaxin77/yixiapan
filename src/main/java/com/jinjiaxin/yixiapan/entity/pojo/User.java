package com.jinjiaxin.yixiapan.entity.pojo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 用户信息
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {


	/**
	 * 用户id
	 */
	private String userId;

	/**
	 * 用户昵称
	 */
	private String nickName;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * qq号
	 */
	private String qqOpenId;

	/**
	 * qq头像
	 */
	private String qqAvatar;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * 注册时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date regisTime;

	/**
	 * 最后一次登录时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastLoginTime;

	/**
	 * 0.禁用 1.启用
	 */
	private Integer status;

	/**
	 * 使用空间(_byte)
	 */
	private Long useSpace;

	/**
	 * 总空间(_byte)
	 */
	private Long totalSpace;

}
