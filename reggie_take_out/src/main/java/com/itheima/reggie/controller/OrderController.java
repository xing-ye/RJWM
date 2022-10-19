package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.OrderService;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 分页查询订单信息，可以通过订单号码、订单时间进一步查询
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("page")
    public R<Page> page(int page, int pageSize, String number, String  beginTime, String  endTime) {

        /**
         * 需要注意:接受时间时，如果按照@DateTimeFormat(pattern = "yyyy-mm-dd HH:mm:ss") Date beginTime
         * 这种去接受时间，因为某种原因(我猜是时间配置问题)回自动的转换成其他时区的时间
         * 而使用String则避免了这一问题，这是因为底层数据库就可以用string来进行查询
         * 具体的还需要后续的学习
         */
        log.info("page={}，pageSize={}",page,pageSize);
        log.info("订单号{}的订单，订单时间：{}到{}",number,beginTime,endTime);
        // 前端需要的信息，oders表已经全包含了，这里我们假设没有userName，来联系一下多表查询
        // 具体的注解可以参考dish中的page函数  1580739507800838145
        Page<Orders> pageInfo=new Page<>(page,pageSize);

        Page<OrdersDto> dtoPage=new Page<>();
        // 先查询出除用户名字外的其他信息
        LambdaQueryWrapper<Orders> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(number),Orders::getNumber,number);
        // 时间大于beginTime，小于endTime
        queryWrapper.ge(beginTime != null,Orders::getOrderTime,beginTime);
        queryWrapper.le(endTime != null,Orders::getOrderTime,endTime);
        // 排序
        queryWrapper.orderByDesc(Orders::getCheckoutTime);
        orderService.page(pageInfo,queryWrapper);

        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Orders> records =pageInfo.getRecords();

        List<OrdersDto> collect = records.stream().map((item) -> {
            OrdersDto dto = new OrdersDto();
            BeanUtils.copyProperties(item, dto);
            Long userId = item.getUserId();
            User user = userService.getById(userId);
            if (user != null) {
                String userName = user.getName();
                dto.setUserName(userName);
            }
            return dto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(collect);

        return R.success(dtoPage);

    }

    /**
     * 更改订单派送信息
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> oder(@RequestBody Orders orders){
        /**
         * 这里不同于以前先用getByid()查询出具体用户，再用set方法设置的步骤
         * 而是直接使用用于更新操作的构造器LambdaUpdateWrapper ，更加方便
         */

        //构造条件构造器
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        //添加过滤条件
        updateWrapper.eq(Orders::getId, orders.getId());
        updateWrapper.set(Orders::getStatus,orders.getStatus());
        orderService.update(updateWrapper);

        return R.success("订单派送成功！");
    }
}