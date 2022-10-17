package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
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
     * 这里返回DishDTO是为了手机端的内容显示风味等规格信息
     * 要注意，这里不需要搞什么records，因为这里返回的不是Page类，没有这个参数
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            // 把item复制到dishDto
            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }

    /**
     * 根据条件，这里主要是 categoryId
     * 查询对应的菜品数据
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//
//        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
//        // 根据 categoryId 查询对应的菜品信息
//        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        // 现根据sort升序排，再根据更新时间降序排
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> dishList = dishService.list(queryWrapper);
//
//        return R.success(dishList);
//    }

    /**
     * 更改停售状态
     * @param status 为要更改为的状态 为1表示是要设为启售状态
     * @param ids 要更改的dish的id们
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable("status") Integer status, @RequestParam List<Long> ids){
        // 这里接受数组，前端传来的是以“，”分隔的数据，虽然没有以json的格式，但是为了可以接受正确，可以强制用RequestParam进行匹配
        log.info("状态为{}，id为{}",status,ids.toString());
        /**
         * 还可以使用queryWrapper.in(ids!=null,Dish::getId,ids)
         * 查询出所有的dish： List<Dish> list = dishService.list(queryWrapper);
         * 注意不能用 queryWrapper.eq,这样是查询不出来的，关键还是对这些语句要够熟悉
         */
        for(Long id:ids){
            Dish dish = dishService.getById(id);
            dish.setStatus(status);
            dishService.updateById(dish);
        }
        return R.success("状态更改成功！");
    }
}
