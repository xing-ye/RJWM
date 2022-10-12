package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

//    用户controller调用，以操作数据库
public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    // 根据菜品id查询对应的菜品信息和口味信息
    public DishDto getByIdWithFlavor(Long id);

    // 修改菜品，需要同时对口味表和菜品表进行更新
    public void updateWithFlavor(DishDto dishDto);
}
