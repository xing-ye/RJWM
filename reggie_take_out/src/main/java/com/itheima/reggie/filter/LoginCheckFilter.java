package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经完成了登录
 * 要注意filterName的名字第一个要小写
 * urlPatterns表示要拦截哪些路径，这里是所有的都拦截
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    // 专门的路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;

        //1. 获取本次请求的URI
        String requestURI=request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);
        // 设置需要放行的url,主要是登录、登出以及静态资源
        String[] urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",// 移动端发送短信
                "/user/login" // 移动端登录
        };
        //2. 判断本次请求是否需要处理
        boolean check=check(urls,requestURI);

        //3. 如果不需要处理，立即放行
        if (check){
            filterChain.doFilter(request,response);//放行
            return;
        }
        log.info("本次请求{}不需要处理",requestURI);
        //4-1. 判断登录状态，如果已登录，则立即放行
        if(request.getSession().getAttribute("employee")!=null){
            //如果可以从session中取出来login时放入的内容，则说明已登录
            Long empId = (Long) request.getSession().getAttribute("employee");
            log.info("用户已登录，id为{}",empId);
            //将用户的id放入本地线程里，以供后期调用
            BaseContext.SetCurrentId(empId);
            filterChain.doFilter(request,response);//放行
            return;
        }
        //4-2. 判断移动端用户登录状态，如果已登录，则立即放行
        if(request.getSession().getAttribute("user")!=null){
            //如果可以从session中取出来login时放入的内容，则说明已登录
            Long usrId = (Long) request.getSession().getAttribute("user");
            log.info("移动端用户已登录，id为{}",usrId);
            //将用户的id放入本地线程里，以供后期调用
            BaseContext.SetCurrentId(usrId);
            filterChain.doFilter(request,response);//放行
            return;
        }
        log.info("用户未登录！");
        //5.如果未登录则返回未登录结果,通过输出流的方式想客户端相应数据(这个要结合前端，backend/js/request.js去看)
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
//

    }

    /**
     * 路径匹配函数，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
       for (String url: urls){
           boolean match=PATH_MATCHER.match(url,requestURI);
//           判断是地址是否匹配
           if(match){
               return true;
           }
       }
       return false;
    }


}
