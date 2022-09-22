package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee") //通过这样，前端可以通过/employee来访问employee的相关方法
public class EmployeeController {
    //自动填装对象
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * 这里使用RequestBody是为了将接受的json的数据读取出来
     * request是用于将数据存入session中
     * @param employee
     * @param request
     * @return
     */
    @PostMapping("/login") //前端是通过post访问的,访问地址是/employee/login
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1、将页面提交的密码password进行md5加密处理
        String password=employee.getPassword();
        password= DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>(); // 查询对象
        queryWrapper.eq(Employee::getUsername,employee.getUsername()); //设置查询条件
        Employee emp = employeeService.getOne(queryWrapper); //查询
        //因为用户名载数据库中设置的是unique(唯一的),所以可以使用getone查询

        //3、如果没有查询到则返回登录失败结果
        if(emp==null){
            return R.error("未注册!");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("密码错误!");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus()==0){
//            0表示禁用
            return R.error("账号已禁用");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }
}
