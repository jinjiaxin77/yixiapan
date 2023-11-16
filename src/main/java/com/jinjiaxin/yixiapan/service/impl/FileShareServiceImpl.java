package com.jinjiaxin.yixiapan.service.impl;

import java.util.Date;
import java.util.List;


import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.dto.SessionShareDto;
import com.jinjiaxin.yixiapan.entity.enums.FileDelFlagEnums;
import com.jinjiaxin.yixiapan.entity.enums.ResponseCodeEnum;
import com.jinjiaxin.yixiapan.entity.enums.ShareValidTypeEnum;
import com.jinjiaxin.yixiapan.entity.pojo.FileInfo;
import com.jinjiaxin.yixiapan.exception.BusinessException;
import com.jinjiaxin.yixiapan.mappers.FileInfoMapper;
import com.jinjiaxin.yixiapan.utils.DateUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import com.jinjiaxin.yixiapan.entity.enums.PageSize;
import com.jinjiaxin.yixiapan.entity.query.FileShareQuery;
import com.jinjiaxin.yixiapan.entity.pojo.FileShare;
import com.jinjiaxin.yixiapan.entity.vo.PaginationResultVO;
import com.jinjiaxin.yixiapan.entity.query.SimplePage;
import com.jinjiaxin.yixiapan.mappers.FileShareMapper;
import com.jinjiaxin.yixiapan.service.FileShareService;
import com.jinjiaxin.yixiapan.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 分享信息 业务接口实现
 */
@Service("fileShareService")
public class FileShareServiceImpl implements FileShareService {

	@Resource
	private FileShareMapper fileShareMapper;

	@Resource
	private FileInfoMapper fileInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<FileShare> findListByParam(FileShareQuery param) {
		return this.fileShareMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(FileShareQuery param) {
		return this.fileShareMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<FileShare> findListByPage(FileShareQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<FileShare> list = this.findListByParam(param);
		PaginationResultVO<FileShare> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(FileShare bean) {
		return this.fileShareMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<FileShare> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.fileShareMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<FileShare> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.fileShareMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(FileShare bean, FileShareQuery param) {
		StringTools.checkParam(param);
		return this.fileShareMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(FileShareQuery param) {
		StringTools.checkParam(param);
		return this.fileShareMapper.deleteByParam(param);
	}

	/**
	 * 根据ShearId获取对象
	 */
	@Override
	public FileShare getFileShareByShareId(String shareId) {
		return this.fileShareMapper.selectByShareId(shareId);
	}

	/**
	 * 根据ShearId修改
	 */
	@Override
	public Integer updateFileShareByShareId(FileShare bean, String shareId) {
		return this.fileShareMapper.updateByShareId(bean, shareId);
	}

	/**
	 * 根据ShearId删除
	 */
	@Override
	public Integer deleteFileShareByShareId(String shareId) {
		return this.fileShareMapper.deleteByShareId(shareId);
	}

	@Override
	public void saveShare(FileShare share) {
		ShareValidTypeEnum shareValidTypeEnum = ShareValidTypeEnum.getByType(share.getValidType());
		if(shareValidTypeEnum == null){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		String fileId = share.getFileId();
		FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, share.getUserId());
		if(fileInfo == null || !fileInfo.getDelFlag().equals(FileDelFlagEnums.USING.getFlag())){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		if(ShareValidTypeEnum.FOREVER != shareValidTypeEnum){
			share.setExpireTime(DateUtil.getAfterDay(shareValidTypeEnum.getDays()));
		}
		Date curDate = new Date();
		share.setShareTime(curDate);
		if(StringTools.isEmpty(share.getCode())){
			share.setCode(StringTools.getRandomNumber(Constants.LENGTH_5));
		}
		share.setShareId(StringTools.getRandomNumber(Constants.LENGTH_20));
		this.fileShareMapper.insert(share);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteShareBatch(String userId, String shareIds) {
		String[] shareIdArray = shareIds.split(",");
		Integer count = this.fileShareMapper.deleteFileShareBatch(userId, shareIdArray);
		if(count != shareIdArray.length){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
	}

	@Override
	public SessionShareDto checkShareCode(String shareId, String code) {
		FileShare fileShare = this.fileShareMapper.selectByShareId(shareId);
		if(fileShare == null || (fileShare.getExpireTime() != null && new Date().after(fileShare.getExpireTime()))){
			throw new BusinessException(ResponseCodeEnum.CODE_902);
		}
		if(!fileShare.getCode().equals(code)){
			throw new BusinessException("提取码错误");
		}
		this.fileShareMapper.updateShareShowCount(shareId);
		SessionShareDto shareDto = new SessionShareDto();
		shareDto.setShareId(shareId);
		shareDto.setShareUserId(fileShare.getUserId());
		shareDto.setFileId(fileShare.getFileId());
		shareDto.setExpireTime(fileShare.getExpireTime());
		return shareDto;
	}

}