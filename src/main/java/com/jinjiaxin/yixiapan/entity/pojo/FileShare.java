package com.jinjiaxin.yixiapan.entity.pojo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 分享信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileShare implements Serializable {


	/**
	 * 分享id
	 */
	private String shareId;

	/**
	 * 文件id
	 */
	private String fileId;

	/**
	 * 分享用户id
	 */
	private String userId;

	/**
	 * 有效期类型 0：一天  1：7天   2：30天   3：永久有效
	 */
	private Integer validType;

	/**
	 * 失效时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date expireTime;

	/**
	 * 分享时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date shareTime;

	/**
	 * 提取码
	 */
	private String code;

	/**
	 * 查看次数
	 */
	private Integer showCount;

	private String fileName;

	private String fileCover;

	private Integer folderType;

	private Integer fileCategory;

	private Integer fileType;

}
