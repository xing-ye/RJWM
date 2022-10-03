package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;

//    用户controller调用，以操作数据库

public interface CategoryService extends IService<Category> {
    // 删除数据，自定义版本
    public void remove(Long ids);
}
