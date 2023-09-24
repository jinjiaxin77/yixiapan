package com.jinjiaxin.yixiapan.service;

import java.util.List;

import com.jinjiaxin.yixiapan.entity.dto.SessionWebUserDto;
import com.jinjiaxin.yixiapan.entity.query.UserInfoQuery;
import com.jinjiaxin.yixiapan.entity.pojo.User;
import com.jinjiaxin.yixiapan.entity.vo.PaginationResultVO;


/**
 * 用户信息 业务接口
 */
public interface UserInfoService {

    public void register(String email, String nickName, String password, String emailCode);

    User getUserByEmail(String email);

    SessionWebUserDto login(String email, String password);

    void resetPwd(String email, String password, String emailCode);
}