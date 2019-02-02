package com.yanzhongxin.zhihu.interceptor;

import com.yanzhongxin.zhihu.dao.UserMapper;
import com.yanzhongxin.zhihu.dao.loginTicketMapper;
import com.yanzhongxin.zhihu.model.HostHolder;
import com.yanzhongxin.zhihu.model.User;
import com.yanzhongxin.zhihu.model.loginTicket;
import com.yanzhongxin.zhihu.model.loginTicketExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.channels.Pipe;
import java.util.Date;
import java.util.List;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/21 10:40
 */
@Component
public class PassportIntecepter implements HandlerInterceptor {
    @Autowired
    private loginTicketMapper loginTicketMapper;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {

        String ticket=null;
       if (httpServletRequest.getCookies()!=null){
           for (Cookie cookie:httpServletRequest.getCookies()){
               if (cookie.getName().equals("ticket")){
                   //
                   ticket=cookie.getValue();
                   break;
               }
           }
       }
       //获取前端的ticket,
        if (ticket!=null){
            loginTicketExample loginTicketExample=new loginTicketExample();
            com.yanzhongxin.zhihu.model.loginTicketExample.Criteria criteria = loginTicketExample.createCriteria();
            criteria.andTicketEqualTo(ticket);
            List<loginTicket> loginTickets = loginTicketMapper.selectByExample(loginTicketExample);
           if (loginTickets.size()<1||loginTickets.get(0).getExpired().before(new Date())||
                   loginTickets.get(0).getStatus()!=0){
               //说明没有查找到了ticket,或者过期了
                return true;
           }

           //如果找到了，而且没有过期的话
            User user = userMapper.selectByPrimaryKey(loginTickets.get(0).getUserId());
            hostHolder.setUser(user);
       }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && hostHolder.getUser() != null) {
            modelAndView.addObject("user", hostHolder.getUser());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        hostHolder.clear();

    }
}
