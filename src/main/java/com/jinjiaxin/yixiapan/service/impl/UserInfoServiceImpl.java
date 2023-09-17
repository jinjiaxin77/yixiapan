package com.jinjiaxin.yixiapan.service.impl;

import java.util.List;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinjiaxin.yixiapan.entity.enums.PageSize;
import com.jinjiaxin.yixiapan.entity.query.UserInfoQuery;
import com.jinjiaxin.yixiapan.entity.pojo.User;
import com.jinjiaxin.yixiapan.entity.vo.PaginationResultVO;
import com.jinjiaxin.yixiapan.entity.query.SimplePage;
import com.jinjiaxin.yixiapan.mappers.UserInfoMapper;
import com.jinjiaxin.yixiapan.service.UserInfoService;
import com.jinjiaxin.yixiapan.utils.StringTools;


/**
 * 用户信息 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {
    @Autowired
    UserInfoMapper userInfoMapper;

    @Override
    public User getUserByEmail(String email) {
        return userInfoMapper.selectByEmail(email);
    }
}