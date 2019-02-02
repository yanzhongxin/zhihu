package com.yanzhongxin.zhihu.contorler;

import com.yanzhongxin.zhihu.async.EventModel;
import com.yanzhongxin.zhihu.async.EventProducer;
import com.yanzhongxin.zhihu.async.EventType;
import com.yanzhongxin.zhihu.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author www.yanzhongxin.com 用户登录功能
 * @date 2019/1/20 23:04
 */
@Controller
public class LoginControler {

    @Autowired
    UserService userService;

    @Autowired
    EventProducer eventProducer;
    //注册用户功能
    @RequestMapping(value = "/reg/",method = RequestMethod.POST)
    public String reg(@RequestParam("username") String username, @RequestParam("password")String password, Model model,
                      HttpServletResponse response,@RequestParam(value = "next",required = false) String next){

        try {
            Map<String, String> register = userService.register(username, password);
            if (register.containsKey("ticket")){
                Cookie cookie=new Cookie("ticket",register.get("ticket"));
                cookie.setPath("/");
                response.addCookie(cookie);
                if (StringUtils.isNotBlank(next)){
                    return "redirect:/"+next;
                }
                return "redirect:/"; //注册成功，重定向到首页
            }else {
                model.addAttribute("msg",register.get("msg"));//
                return "login";
            }

        }catch (Exception e){

            return "login"; //注册失败
        }
    }

    //用户登陆功能
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public String login(@RequestParam("username") String username, @RequestParam("password")String password, Model model,
                        @RequestParam("rememberme") String rememberme,
                        HttpServletResponse response,@RequestParam(value = "next",required = false) String next){

        try {
            Map<String, String> login = userService.login(username, password);
            if (login.containsKey("ticket")){
                Cookie cookie=new Cookie("ticket",login.get("ticket"));
                cookie.setPath("/");
                response.addCookie(cookie);


                //用户登录完成以后，这里经过检查，发现用户ip异常，发送一个邮件
                eventProducer.fireEvent(new EventModel(EventType.LOGIN)
                        .setExt("username", username).setExt("email", "845713694@qq.com")
                        .setActorId(Integer.parseInt(login.get("userId"))));


                if (StringUtils.isNotBlank(next)){
                    return "redirect:"+next;
                }
                return "redirect:/"; //注册成功，重定向到首页
            }else {
                model.addAttribute("msg",login.get("msg"));//
                return "login";
            }

        }catch (Exception e){

            return "login"; //注册失败
        }
    }

    @RequestMapping(value = "/reglogin",method = RequestMethod.GET)
    public String reglogin(@RequestParam(value = "next",required = false) String next,Model model){
        model.addAttribute("next",next);
        return "login"; //注册失败

    }

    @RequestMapping(path = {"/logout"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/";
    }
}
