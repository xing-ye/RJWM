package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service用于业务层，用于声明给spring控制
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，
     * 在删除之前需要判断该数据是否关联其他表(菜品表和套餐表)
     * 所以没有使用 IService 提供的默任的removeById方法
     * @param ids
     */
    @Override
    public void remove(Long ids) {
        // 1. 查询当前分类是否关联菜品，如果已关联则抛出一个业务异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,ids);
        int count = dishService.count(dishLambdaQueryWrapper);// 计算CategoryId为ids的数目

        if (count>0){
            // 抛出业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除！");
        }
        // 2. 查询当前分类是否关联套餐，如果已关联则抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,ids);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);// 计算CategoryId为ids的数目

        if (count2>0){
            // 抛出业务异常
            throw new CustomException("当前分类下关联了套餐，不能删除！");
        }
        //3.正常删除
        super.removeById(ids);

    }
}
