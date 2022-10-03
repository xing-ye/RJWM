package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 分类管理
 * @RestController是@ResponseBody和@Controller的组合注解。
 * Controller用于控制层，交付spring管理
 */
@Slf4j
@RestController
@RequestMapping("/category") //通过这样，前端可以通过/employee来访问employee的相关方法
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * path":"/category"
     * RequestBody 表明接受的为json格式
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("新增分类：{}",category.toString());
        categoryService.save(category);
        return R.success("新增成功!");
    }

    /**
     * 分类信息分页查询
     * path":"/category/page"
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        log.info("page={},pageSize={},name={}",page,pageSize);
        // 构造分页构造器
        Page pageInfo=new Page(page,pageSize);


        //构造条件构造器，如果需要使用name
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper();
        //添加一个排序条件,按照sort进行排序,orderByAsc表示升序展示
        queryWrapper.orderByAsc(Category::getSort);

        //执行查询,按照pageinfo的要求进行分页查询,这里不需要一个返回值，会自动的放入pageinfo里
        categoryService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 根据id删除分类信息
     * 这里不使用PathVariable是因为，这里的ids只是传输的变量而已，例如：/category/?ids=1576928442561810434
     * 而只有访问格式为/employee/id,这种id作为路径的才需要使用PathVariable来获得id值
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        // 注意这里前端也是命名的ids，要对应起来
        log.info("删除分类，id为{}",ids);
        // 删除之前应该判断，要删除的数据是否和其他表相关联，否则不能删除
        categoryService.remove(ids);
        return R.success("分类信息删除成功！");

    }

    /**
     * 根据id修海分类信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息：{}",category);
        // 其他一些数据使用了自动填充
        categoryService.updateById(category);
        return R.success("修改分类信息成功！");

    }
}
