package com.jinjiaxin.yixiapan.service.impl;

import com.jinjiaxin.yixiapan.entity.pojo.User;
import com.jinjiaxin.yixiapan.mappers.UserInfoMapper;
import com.jinjiaxin.yixiapan.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jjx
 * @Description
 * @create 2023/9/18 16:12
 */

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Override
    public User getUserByEmail(String email) {
        return userInfoMapper.selectByEmail(email);
    }
}
