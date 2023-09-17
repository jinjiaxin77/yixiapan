package com.jinjiaxin.yixiapan.service;

import java.util.List;

import com.jinjiaxin.yixiapan.entity.query.UserInfoQuery;
import com.jinjiaxin.yixiapan.entity.pojo.User;
import com.jinjiaxin.yixiapan.entity.vo.PaginationResultVO;


/**
 * 用户信息 业务接口
 */
public interface UserInfoService {

    User getUserByEmail(String email);

}