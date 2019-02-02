package com.yanzhongxin.zhihu.async.handler;


import com.yanzhongxin.zhihu.async.EventHandler;
import com.yanzhongxin.zhihu.async.EventModel;
import com.yanzhongxin.zhihu.async.EventType;
import com.yanzhongxin.zhihu.util.MailSender;
import com.yanzhongxin.zhihu.util.SendEmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Component
public class LoginExceptionHandler implements EventHandler {


    @Override
    public void doHandle(EventModel model) {
        // xxxx判断发现这个用户登陆异常
       /* Map<String, Object> map = new HashMap<String, Object>();
        map.put("username", model.getExt("username"));
        mailSender.sendWithHTMLTemplate(model.getExt("email"), "登陆IP异常", "mails/login_exception.html", map);

*/
        //SendEmailUtil.sendMail(model.getExt("email"),"知乎账号安全提醒","知乎网管理员提醒：尊敬的知友"+model.getExt("username")+"您的账号异常登录，如非本人操作，请尽快修改密码");

    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LOGIN);
    }
}
