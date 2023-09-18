package com.jinjiaxin.yixiapan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.jinjiaxin.yixiapan"})
@EnableTransactionManagement
@EnableScheduling
@MapperScan("com.jinjiaxin.yixiapan.mappers")
public class YixiaPanApplication {

    public static void main(String[] args) {
        SpringApplication.run(YixiaPanApplication.class, args);
    }

}
