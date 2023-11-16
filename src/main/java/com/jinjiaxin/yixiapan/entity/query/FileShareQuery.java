package com.jinjiaxin.yixiapan.entity.query;

import lombok.Data;

import java.util.Date;


/**
 * 分享信息参数
 */
@Data
public class FileShareQuery extends BaseParam {


	/**
	 * 分享id
	 */
	private String shareId;

	private String shareIdFuzzy;

	/**
	 * 文件id
	 */
	private String fileId;

	private String fileIdFuzzy;

	/**
	 * 分享用户id
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 有效期类型 0：一天  1：7天   2：30天   3：永久有效
	 */
	private Integer validType;

	/**
	 * 失效时间
	 */
	private String expireTime;

	private String expireTimeStart;

	private String expireTimeEnd;

	/**
	 * 分享时间
	 */
	private String shareTime;

	private String shareTimeStart;

	private String shareTimeEnd;

	/**
	 * 提取码
	 */
	private String code;

	private String codeFuzzy;

	/**
	 * 查看次数
	 */
	private Integer showCount;

	private boolean queryFileName;
}
