package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * dish菜品管理，主要用于对文件的上传下载进行管理
 * @RestController 是 @ResponseBody和 @Controller 的组合注解。
 * 即回复可以自动转为json格式
 * Controller用于控制层，交付spring管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    public DishFlavorService dishFlavorService;

    @Autowired
    public DishService dishService;

    @Autowired
    public CategoryService categoryService;

    /**
     * 添加菜品，主要是在saveWithFlavor进行操作的
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功！");
    }

    /**
     * 菜品信息分页查询
     * 这里name，因为前端有搜索框，所以有时候可能有name
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);
        /**
         * 由于前端显示的信息含有 dish 的信息 以及 dish 不含有的 categoryName 的信息所以需要返回 DishDto
         * 但是由于我们并没有这样一个表，即没有 DishDto 对应的 service 和 mapper
         * 所以，先用 dish 的进行查询，然后在赋值到 DishDto 上
         */
        // 构造分页构造器
        Page<Dish> pageInfo=new Page<>(page,pageSize);

        // 要注意，dishDtoPage 并没有设置 page 和 pageSize，因为 dishDtoPage 并没有参与查询，而是只用作返回前端
        //如果这里也设置了，那么就会导致前端只能显示 pageSize 条信息
        Page<DishDto> dishDtoPage=new Page<>();


        //构造条件构造器，如果需要使用name
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper();
        //添加一个过滤条件
        /**
         * 这里需要注意，StringUtils.isNotEmpty判断name不为空后才会进行比较，也可以没有这个
         * StringUtils的包是org.apache.commons.lang
         */
        queryWrapper.like(StringUtils.isNotEmpty(name),Dish::getName,name);
        //添加一个排序条件,按照更新时间进行排序，ByDesc代表降序排序
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行查询,按照pageinfo的要求进行分页查询,这里不需要一个返回值，会自动的放入pageinfo里
        dishService.page(pageInfo,queryWrapper);

        // 对象拷贝,将dish的信息拷贝到dishDtopage,records是一个打包好的页面显示的列表信息，
        // 而这两个页面是不同的，所以不能直接拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        // 对records的信息进行处理，主要区别在于dish种含有的是 CategoryId
        // 而返回前端的DishDto含有的是 categoryName，所以要进行替换
        List<Dish> records=pageInfo.getRecords();
        /**
         * 下面主要是对pageInfo中的 CategoryId 转换成 categoryName ，步骤如下：
         * 首先创建一个 dishDto 对象，将item中的其他一样的值直接拷贝过去
         * 然后，对于不一样的就进行提取、查询、替换
         * 最后，将信息重新打包成一个list，用于后续对 dishDtoPage 的 records 进行赋值
         */
        List<DishDto> collect = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category!=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        //对 dishDtoPage 的 records 进行赋值
        dishDtoPage.setRecords(collect);

        return R.success(dishDtoPage);
    }

    /**
     * PathVariable表示id是一个路径变量
     * 根据id查询对应的菜品信息和口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        DishDto dishDto= dishService.getByIdWithFlavor(id);
        if (dishDto!=null){
            return R.success(dishDto);
        }
        return R.error("没有查询到菜品及口味信息！");
    }

    /**
     * 修改菜品，需要同时对口味表和菜品表进行更新
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功！");
    }
    /**
     * 根据条件查询分类数据
     * 这里主要是根据类别，以List返回菜品的列表或者套餐的列表
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
        // 条件构造器
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper();
        // 设置查询条件
         queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
         // 查询状态为1的，即启售状态的
         queryWrapper.eq(Dish::getStatus,1);
        // 添加排序条件，以sort升序排序，然后如果sort相同在以更新时间，降序排序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);
    }

}
