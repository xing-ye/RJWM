package com.itheima.reggie.config;

import com.itheima.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

//说明是配置类
@Configuration
@Slf4j
public class WebMvcConfig extends WebMvcConfigurationSupport  {
    /*
    设置静态资源映射
    classpath是指resources的目录
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始进行静态资源映射...");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }

    /**
     * 扩展MVC框架的消息转换器
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 创建一个新的消息转换器对象
        log.info("扩展的消息转换器！");
        MappingJackson2HttpMessageConverter messageConverter=new MappingJackson2HttpMessageConverter();

        //设置对象转换器，底层使用Jackson将java对象转为Json，对象转换器由common包下的类实现的
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        // 添加对象转换器，要注意的是需要把自己的放在第一个(index=0)，否则默认还是会执行以前的第一个
        converters.add(0,messageConverter);
    }
}
