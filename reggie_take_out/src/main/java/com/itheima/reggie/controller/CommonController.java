package com.itheima.reggie.controller;


import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 通用controller，主要用于对文件的上传下载进行管理
 * @RestController是@ResponseBody和@Controller的组合注解。
 * Controller用于控制层，交付spring管理
 */
@Slf4j
@RestController
@RequestMapping("/common") //通过这样，前端可以通过/employee来访问employee的相关方法
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;
    /**
     * 文件上传
     * MultipartFile,这里用于说明函数是以文件为参数的
     * 需要注意，这里的file是不能乱改的，需要和前端的请求保持一致，前端也叫file，所以这里就叫file
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        log.info(file.toString());
        //获取原始文件名，例如aaa.jpg
        String originalFilename = file.getOriginalFilename();
        // 获取文件类型后缀,例如 .jpg
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //使用UUID重新生成文件名，防止文件重复导致的文件覆盖
        String fileName= UUID.randomUUID().toString();
        // 创建一个目录对象，判断当前目录是否存在
        File dir=new File(basePath);
        if(!dir.exists()){
            // 不存则自动创建
            dir.mkdirs();
        }

        //file是一个临时文件，需要转存到相应的位置，否则在这次请求后，该临时文件就会删除
        file.transferTo(new File(basePath+fileName+suffix));
        return R.success(fileName+suffix);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {

        // 通过输入流读取文件内容，这个是文件的输入流
        FileInputStream fileInputStream=new FileInputStream(new File(basePath+name));

        //通过输出流将文件协会浏览器，在浏览器显示图片
        ServletOutputStream outputStream = response.getOutputStream(); //获取输出流(浏览器的)
        // 设置传给浏览器的内容格式为图像
        response.setContentType("/image/jpeg");
        int len=0;
        byte[] bytes=new byte[1024];
        while((len=fileInputStream.read(bytes))!=-1){
            // 将输入流读取的内容存入bytes,一直读取到最后一位(-1)

            // 然后将读取的内容都输出到浏览器
            outputStream.write(bytes,0,len);
        }
        // 关闭资源
        outputStream.close();
        fileInputStream.close();

    }
}
