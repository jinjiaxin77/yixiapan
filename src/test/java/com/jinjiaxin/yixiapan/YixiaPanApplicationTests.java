package com.jinjiaxin.yixiapan;

import com.jinjiaxin.yixiapan.entity.pojo.EmailCode;
import com.jinjiaxin.yixiapan.mappers.EmailCodeMapper;
import com.jinjiaxin.yixiapan.service.EmailCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class YixiaPanApplicationTests {

    @Autowired
    EmailCodeMapper mapper;

    @Test
    void contextLoads() {
        EmailCode emailCode = mapper.selectEmailCode("1665353392@qq.com");
        mapper.update(emailCode);
        System.out.println(emailCode);
    }

}
