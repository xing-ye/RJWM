package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工实体
 * 可以自动的提供一些类方法，以及封装提供类对象
 * 在映射实体或者属性时，将数据库中表名和字段名中的下划线去掉，按照驼峰命名法映射
 * 这样可以自动的根据实体的名字找到相应的表，就不需要去指定了
 */
@Data
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    private String idNumber;//身份证号码,驼峰命名法

    private Integer status;

    @TableField(fill = FieldFill.INSERT)//插入(第一填写时)时填充字段
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)//插入和更新时插入字段
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)//插入时填充字段
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)//插入和更新时插入字段
    private Long updateUser;

}
