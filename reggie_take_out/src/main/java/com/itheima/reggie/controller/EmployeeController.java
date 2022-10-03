package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 用户管理
 * @RestController是@ResponseBody和@Controller的组合注解。
 * Controller用于控制层，交付spring管理
 */
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

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
//        清理session中保存的员工ID
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * 这里mapping不设置地址是因为，前端那里保存只访问到 /employee即可
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工信息：{}",employee.toString());
        //设置初始密码123456，使用MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        Long employeeId= (Long) request.getSession().getAttribute("employee");
        /**
         * 这里使用自动填充去添加：首先Employee要设置填充声明，然后通过MyMetaObjectHandler 实现
        * employee.setCreateTime(LocalDateTime.now());// 设置用户创建时间
        * employee.setUpdateTime(LocalDateTime.now());//设置更新时间

        // 获取登录者id，跟当时放入session中的名字要对应
        * employee.setCreateUser(employeeId);//设置创建人id
        * employee.setUpdateUser(employeeId);//设置更新人
         **/
        employeeService.save(employee);
        return  R.success("新增员工成功！");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);
        // 构造分页构造器
        Page pageInfo=new Page(page,pageSize);


        //构造条件构造器，如果需要使用name
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper();
        //添加一个过滤条件
        /**
         * 这里需要注意，StringUtils.isNotEmpty判断name不为空后才会进行比较，也可以没有这个
         * StringUtils的包是org.apache.commons.lang
         */
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加一个排序条件,按照更新时间进行排序，ByDesc代表降序排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询,按照pageinfo的要求进行分页查询,这里不需要一个返回值，会自动的放入pageinfo里
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * PutMapping是因为前端传来的是put格式
     * 根据id修改员工信息
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info("要更新的员工信息{}",employee.toString());
        Long empId = (Long) request.getSession().getAttribute("employee");
        /**
         * 要注意长整型ID长度为19，而json使用16位，会对后三位进行取舍而丢失精度、
         * 因此，服务端在传递回数据时需要将ID换位string传递
         * 该功能用消息转换器实现
         */
        /**
         * 这里使用自动填充去添加：首先Employee要设置填充声明，然后通过MyMetaObjectHandler 实现
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);
         */
        //employeeService调用map来更新数据
        employeeService.updateById(employee);
        return R.success("员工信息修改成功！");
    }

    /**
     * PathVariable表示id是一个路径变量
     * 根据id查询信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee employee=employeeService.getById(id);
        if (employee!=null){
            return R.success(employee);
        }
        return R.error("没有查询到员工信息");
    }

}
