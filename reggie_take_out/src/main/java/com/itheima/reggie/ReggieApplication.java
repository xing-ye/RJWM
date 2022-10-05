package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j //用于记录log
@SpringBootApplication //用于启动SpringBoot
@ServletComponentScan //扫描过滤器的注解
@EnableTransactionManagement //开启对事务控制注解（事务控制是指对多张表进行处理时，防止操作间的冲突，可以自动回滚）
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动成功。。。");
    }
}
