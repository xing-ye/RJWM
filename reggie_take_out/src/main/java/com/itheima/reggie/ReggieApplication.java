package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@Slf4j //用于记录log
@SpringBootApplication //用于启动SpringBoot
@ServletComponentScan //扫描过滤器的注解
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动成功。。。");
    }
}
