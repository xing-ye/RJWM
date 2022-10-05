package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO即 data transfer object 数据传输对象，
 * 一般用于展示层(前端)和服务层（后端）之间的数据传输
 * 主要在当前端的数据无法和现存的实体类属性一一对应时使用
 * DishDto 用于封装页面传来的数据，主要是在添加菜品时，生成的类集包含dish 也包含 dishflavor的情况
 * 所以DishDto 相当于dish的基础上在加入dishflavor 的一些属性
 */
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
