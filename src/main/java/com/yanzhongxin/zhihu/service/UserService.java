package com.yanzhongxin.zhihu.service;

import com.ctc.wstx.util.StringUtil;
import com.sun.corba.se.spi.servicecontext.UEInfoServiceContext;
import com.yanzhongxin.zhihu.dao.UserMapper;
import com.yanzhongxin.zhihu.dao.loginTicketMapper;
import com.yanzhongxin.zhihu.model.User;
import com.yanzhongxin.zhihu.model.UserExample;
import com.yanzhongxin.zhihu.model.loginTicket;
import com.yanzhongxin.zhihu.model.loginTicketExample;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import sun.security.provider.MD5;

import java.util.*;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/20 19:54
 */
@Service
public class UserService {
    @Autowired
    UserMapper userMapper;

    @Autowired
    loginTicketMapper loginTicketMapper;
    public User getUserById(int id){
        User user = userMapper.selectByPrimaryKey(id);
        return user;
    }

    //根据用户名查询用户
    public User getUserByName(String username){
        UserExample userExample=new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andNameEqualTo(username);
        List<User> users = userMapper.selectByExample(userExample);
        if (users.size()>0){
            return users.get(0);
        }else {
            return null;
        }

    }    public Map<String,String> register(String username,String password){
        Map<String,String> map=new HashMap();
        if (StringUtils.isBlank(username)){
            map.put("msg","用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("msg","密码不能为空");
            return map;
        }
        UserExample example=new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(username);
        List<User> users = userMapper.selectByExample(example);
        if (users.size()>=1){
            map.put("msg","用户名已经存在");
            return map;
        }

        //插入用户到数据库
        User user=new User();
        user.setName(username);
        user.setSalt(UUID.randomUUID().toString().substring(0,5));
        user.setPassword(DigestUtils.md5DigestAsHex((user.getSalt()+password).getBytes()));
        userMapper.insertSelective(user);
        String ticket = addLoginTicket(user.getId());
        map.put("ticket",ticket);
        return  map;

    }

    /*用户登陆*/

    public Map<String,String> login(String username,String password){
        Map<String,String> map=new HashMap();
        if (StringUtils.isBlank(username)){
            map.put("msg","用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("msg","密码不能为空");
            return map;
        }
        UserExample example=new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(username);
        List<User> users = userMapper.selectByExample(example);
        if (users.size()<1){
            map.put("msg","用户名或者密码错误");
            return map;
        }else {//803d52622e81d3c3a7a3ca7f28591870
            String pass=DigestUtils.md5DigestAsHex((users.get(0).getSalt()+password).getBytes());
            if (!users.get(0).getPassword().equals(pass)){
                //用户名密码验证失败。
                map.put("msg","用户名或者密码错误");
                return map;
            }else {
                //验证成功
                String ticket = addLoginTicket(users.get(0).getId());
                map.put("ticket",ticket);
                map.put("userId",users.get(0).getId()+"");
            }
        }
        return  map;

    }

    private String addLoginTicket(int userId) {
        loginTicket ticket = new loginTicket();
        ticket.setUserId(userId);
        Date date = new Date();
        date.setTime(date.getTime() + 1000*3600*24);
        ticket.setExpired(date);
        ticket.setStatus(0);
        ticket.setTicket(UUID.randomUUID().toString().replaceAll("-", ""));
        loginTicketMapper.insertSelective(ticket);
        return ticket.getTicket();
    }
    //退出登录
    public void logout(String ticket){
        loginTicketExample loginTicketExample=new loginTicketExample();
        com.yanzhongxin.zhihu.model.loginTicketExample.Criteria criteria = loginTicketExample.createCriteria();
        criteria.andTicketEqualTo(ticket);
        loginTicket loginTicket=new loginTicket();
        loginTicket.setStatus(1);
        loginTicketMapper.updateByExampleSelective(loginTicket,loginTicketExample);
    }

}
