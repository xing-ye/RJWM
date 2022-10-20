package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    /**
     * 添加套餐
     * @param setmealDto
     * @return
     * RequestBody 指接受的是json
     */
    @PostMapping
    public R<String > save(@RequestBody SetmealDto setmealDto){
        log.info("{}套餐信息保存",setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("套餐新增成功！");

    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        // 分页构造器对象
        Page<Setmeal> pageInfo=new Page<>(page,pageSize);
        // 这里的dtoPage不能直接设置page和pagesize，因为用来保存pageinfo的内容的
        Page<SetmealDto> dtoPage =new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();

        // 添加查询条件，根据name进行模糊查询
        queryWrapper.like(name!=null,Setmeal::getName,name);
        // 排序条件，根据更新时间降序排
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo,queryWrapper);

        // 由于pageinfo的类型是Setmeal，而其只包含categoryId而不包含categoryName，所以需要进行处理

        // 对象拷贝pageInfo内容复制到dtoPage，忽略 records
        // records是一个页表展示的泛型list，而这两个页面的内容是不一样的
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();

        //具体解释可以参考dishController的page函数
        List<SetmealDto> collect = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            // 分类的id
            Long categoryId = item.getCategoryId();
            // 根据id查询名字
            Category serviceById = categoryService.getById(categoryId);
            if (serviceById != null) {
                String name1 = serviceById.getName();
                setmealDto.setCategoryName(name1);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(collect);

        return R.success(dtoPage);
    }

    /**
     * 删除套餐
     * RequestParam 表示要从url中获取参数
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info(ids.toString());
        setmealService.removeWithDish(ids);
        return R.success("删除成功！");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * PathVariable表示id是一个路径变量
     * 根据id查询对应的套餐信息，用于修改时讲数据回应到页面
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){
        log.info("根据id查询套餐信息...");
        SetmealDto setmealDto = setmealService.getByIdWithDishes(id);
        if (setmealDto!=null){
            return R.success(setmealDto);
        }
        return R.error("没有查询到套餐及对应菜品关系信息！");
    }

    /**
     * 批量更改停售状态
     * 解@RequestParam接收的参数是来自HTTP请求体或请求url的QueryString中。
     * 而@RequestBody接收的参数是来自requestBody中，即请求体。
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
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("状态更改成功！");
    }


    /**
     * 移动端点击套餐图片查看套餐具体内容
     * 这里返回的是dto 对象，因为前端需要copies这个属性
     * 前端主要要展示的信息是:套餐中菜品的基本信息，图片，菜品描述，以及菜品的份数
     * @param SetmealId
     * @return
     */
    //这里前端是使用路径来传值的，要注意，不然你前端的请求都接收不到，就有点尴尬哈
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> dish(@PathVariable("id") Long SetmealId){
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,SetmealId);
        //获取套餐里面的所有菜品  这个就是SetmealDish表里面的数据
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        List<DishDto> dishDtos = list.stream().map((setmealDish) -> {
            DishDto dishDto = new DishDto();
            //其实这个BeanUtils的拷贝是浅拷贝，这里要注意一下
            BeanUtils.copyProperties(setmealDish, dishDto);
            //这里是为了把套餐中的菜品的基本信息填充到dto中，比如菜品描述，菜品图片等菜品的基本信息
            Long dishId = setmealDish.getDishId();
            Dish dish = dishService.getById(dishId);
            BeanUtils.copyProperties(dish, dishDto);

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtos);
    }


}
