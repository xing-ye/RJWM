package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
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

    /**
     * 根据菜品id查询对应的菜品信息和口味信息
     * @param id
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {

        DishDto dishDto=new DishDto();
        // 查询菜品基本信息，从dish表查询
        Dish dish=this.getById(id);

        // 查询当前菜品对应的口味信息，从dish_falvor 表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        // 拷贝到dishDto中
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    /**
     * 修改菜品，需要同时对口味表和菜品表进行更新
     * @param dishDto
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {

        // 更新 dish 表基本信息
        this.updateById(dishDto);

        // 更新口味表信息

        // 先清理当前菜品对应的口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        // 再重新添加当前提交的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();

        Long dishId = dishDto.getId();// 获得菜品的id
        // 这里与前面新增相同，因为flavors 不含有dish的id，所以这里加上去
        List<DishFlavor> collect = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(collect);

    }

    @Override
    public void removeWithFlavor(List<Long> ids) {

        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper();

        // 查询状态，确定是否可以删除(启售中的不能删除)
        queryWrapper.in(Dish::getId,ids); // 判断是否在要删除的id中
        queryWrapper.eq(Dish::getStatus,1); // 判断是否为启售状态(1)

        // 如果不能删除，抛出业务异常
        int count=this.count(queryWrapper);// 统计查询到的数据条数
        if(count>0){
            // 如果存在要删除的启售数据，抛出异常
            throw new CustomException("菜品正在售卖中，不能删除！");
            // 这里CustomException是我们自定义的异常，可以将信息展现到前端中
        }

        // 如果可以删除，先删除套餐中的数据--setmeal
        this.removeByIds(ids);
        // 删除关系表中的数据--dish_falvor
        // 因为不能直接用removeByIds，因为这里传入的ids是套餐的id并不是setmeal_dish的主键值
        //所以，可以使用下面的方法，利用查询的方式删除
        LambdaQueryWrapper<DishFlavor> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.in(DishFlavor::getDishId,ids); // 找到所有getSetmealId在ids中的数据
        dishFlavorService.remove(queryWrapper1);


    }
}
