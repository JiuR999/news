package com.swust.wemedia;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication(scanBasePackages = {"com.swust.wemedia","com.swust.common.config"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.swust.apis")
@MapperScan("com.swust.wemedia.mapper")
@EnableAsync
public class WemediaApplication {

    public static void main(String[] args) {
        SpringApplication.run(WemediaApplication.class,args);
    }
}
