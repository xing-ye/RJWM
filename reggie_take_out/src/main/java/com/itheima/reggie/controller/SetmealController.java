package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
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
}
