package com.xzy.usercenter.filter;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xzy.usercenter.common.JsonUtil;
import com.xzy.usercenter.common.RedisUtil;
import com.xzy.usercenter.http.CookieUtil;
import com.xzy.usercenter.model.User;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 过滤器，重置redis中session有效期
 */
@Component
@WebFilter(urlPatterns = "/*",filterName = "sessionExpireFilter")
public class SessionExpireFilter implements Filter {

    /**
     * 无法正常注入，和spring的启动顺序有关，需要在init方法中引入，如果没有引入就是控制针异常
     */
    private RedisUtil redisUtil;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        redisUtil = context.getBean("redisUtil", RedisUtil.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        //读取loginToken
        String loginToken = CookieUtil.readUserLoginCookie(httpServletRequest);

        if (StringUtils.isNotBlank(loginToken)) {
            //从redis中获取
            String jsonStr = (String)redisUtil.get(loginToken);
            //转换
            User user = JsonUtil.string2Obj(jsonStr, User.class);
            if (user != null) {
                //重置时间
                redisUtil.expire(loginToken, 60*30);
            }
        }
        chain.doFilter(request,response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
