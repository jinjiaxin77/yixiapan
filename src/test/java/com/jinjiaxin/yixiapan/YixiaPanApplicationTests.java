package com.jinjiaxin.yixiapan;

import com.jinjiaxin.yixiapan.component.RedisComponent;
import com.jinjiaxin.yixiapan.component.RedisUtils;
import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.dto.UserSpaceDto;
import com.jinjiaxin.yixiapan.entity.pojo.EmailCode;
import com.jinjiaxin.yixiapan.mappers.EmailCodeMapper;
import com.jinjiaxin.yixiapan.service.EmailCodeService;
import com.jinjiaxin.yixiapan.utils.StringTools;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class YixiaPanApplicationTests {

    @Autowired
    RedisComponent redisComponent;

    @Autowired
    RedisUtils redisUtils;

    @Test
    void contextLoads() {
        String userId = "qqdyvcnzzxz5vni";
//        UserSpaceDto userSpaceDto = new UserSpaceDto(0L, (long) (10*Constants.MB));
//        System.out.println(userSpaceDto);
//        redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USED + userId, userSpaceDto, Constants.REDIS_KEY_EXPIRES_DAY);
//        userSpaceDto = redisComponent.getUserSpaceDto("qqdyvcnzzxz5vni");
//        System.out.println(userSpaceDto);

//        String fileName = "123.jpg";
//        Integer index = StringUtils.lastIndexOf(fileName,'.');
//        String newName = fileName.substring(0,index) + "_" + StringTools.getRandomNumber(Constants.LENGTH_5) + fileName.substring(index);
//        System.out.println(newName);

        UserSpaceDto spaceDto = redisComponent.getUserSpaceDto(userId);
        System.out.println(spaceDto);
        System.out.println(spaceDto.getTotalSpace());
    }

}
