package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service用于业务层，用于声明给spring控制
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * Transactional 事务管理注解，对于多个表处理，防止数据冲突，比如操作失败后可以回滚
     * 新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品的基本信息到菜品表dish,因为是对DishService的实现，所以可以直接调用
        this.save(dishDto);

        //保存菜品口味数据到菜品口味表dish_flavor

        Long dishId = dishDto.getId();// 获得菜品的id

        List<DishFlavor> flavors=dishDto.getFlavors(); // 获得菜品的口味数据
        // 使用lamda表达式，将id和口味结合起来，整合成dish_flavor的表的内容
        List<DishFlavor> collect = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(collect);


    }
}
