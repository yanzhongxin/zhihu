package com.yanzhongxin.zhihu.interceptor;

import com.yanzhongxin.zhihu.dao.UserMapper;
import com.yanzhongxin.zhihu.dao.loginTicketMapper;
import com.yanzhongxin.zhihu.model.HostHolder;
import com.yanzhongxin.zhihu.model.User;
import com.yanzhongxin.zhihu.model.loginTicket;
import com.yanzhongxin.zhihu.model.loginTicketExample;
import org.apache.commons.lang.CharEncoding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/21 10:40
 */
@Component
public class LoginIntecepter implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        if (hostHolder.getUser()==null){
            //如果没有用户的话
            //跳转到登陆页面
            httpServletResponse.sendRedirect("/reglogin?next="+httpServletRequest.getRequestURL());

        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
