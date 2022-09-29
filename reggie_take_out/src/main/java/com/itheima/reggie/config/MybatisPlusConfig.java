package com.itheima.reggie.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurations说明是配置类
 * 这些配置会自动的加装的
 */
@Configuration
public class MybatisPlusConfig {
    /**
     *  配置MybatisPlus的分页插件
     *  用于给前端分页显示的数据
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        // 创建一个MP的拦截器
        MybatisPlusInterceptor mybatisPlusInterceptor=new MybatisPlusInterceptor();
        // 创建一个页面分页的拦截器
        PaginationInnerInterceptor paginationInnerInterceptor =new PaginationInnerInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(paginationInnerInterceptor);

        return mybatisPlusInterceptor;
    }
}
