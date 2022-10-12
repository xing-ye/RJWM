package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service用于业务层，用于声明给spring控制
 */
@Service
public class SetmealServiceImpl  extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增套餐，同时保存套餐和菜品的关联关系
     * @param setmealDto
     * Transactional 事务注解，因为涉及到两张表
     * 要么全成功，要么全失败
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {

        // 保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        //保持套餐和菜品的关联信息，操作setmeal_dish，执行insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);

    }

    /**
     * 删除套餐，同时删除套餐和菜品的关联关系
     * @param ids
     * Transactional 事务注解，因为涉及到两张表
     * 要么全成功，要么全失败
     */
    @Transactional
    @Override
    public void removeWithDish(List<Long> ids) {

        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper();

        // 查询状态，确定是否可以删除(启售中的不能删除)
        queryWrapper.in(Setmeal::getId,ids); // 判断是否在要删除的id中
        queryWrapper.eq(Setmeal::getStatus,1); // 判断是否为启售状态(1)

        // 如果不能删除，抛出业务异常
        int count=this.count(queryWrapper);// 统计查询到的数据条数
        if(count>0){
            // 如果存在要删除的启售数据，抛出异常
            throw new CustomException("套餐正在售卖中，不能删除！");
            // 这里CustomException是我们自定义的异常，可以将信息展现到前端中
        }

        // 如果可以删除，先删除套餐中的数据--setmeal
        this.removeByIds(ids);
        // 删除关系表中的数据--setmeal_dish
        // 因为不能直接用removeByIds，因为这里传入的ids是套餐的id并不是setmeal_dish的主键值
        //所以，可以使用下面的方法，利用查询的方式删除
        LambdaQueryWrapper<SetmealDish> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,ids); // 找到所有getSetmealId在ids中的数据
        setmealDishService.remove(queryWrapper1);

    }
}
