package com.jinjiaxin.yixiapan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
<<<<<<< HEAD
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.jinjiaxin.yixiapan"})
@EnableTransactionManagement
@EnableScheduling
=======

@SpringBootApplication
>>>>>>> 122b63581d87ff10f333a562321c70ffd329d491
public class YixiaPanApplication {

    public static void main(String[] args) {
        SpringApplication.run(YixiaPanApplication.class, args);
    }

}
