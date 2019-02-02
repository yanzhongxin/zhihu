package com.yanzhongxin.zhihu.contorler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yanzhongxin.zhihu.dao.UserMapper;
import com.yanzhongxin.zhihu.model.HostHolder;
import com.yanzhongxin.zhihu.model.Message;
import com.yanzhongxin.zhihu.model.User;
import com.yanzhongxin.zhihu.model.ViewObject;
import com.yanzhongxin.zhihu.service.MessageService;
import com.yanzhongxin.zhihu.service.UserService;
import com.yanzhongxin.zhihu.util.WendaUtil;
import org.omg.CORBA.MARSHAL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.awt.geom.AreaOp;

import java.util.*;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/21 19:48
 */
@Controller
public class MessageControler {

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    MessageService messageService;
    //发私信,比如a发给b一条私信}
    @RequestMapping(value = "/msg/addMessage",method = RequestMethod.POST)
    @ResponseBody
    public String sendPrivateMessage(@RequestParam("toName") String toname,
                                     @RequestParam("content") String letterContent){
        //1 检查用户必须登陆才能发私信，否则转到登陆页面。
        try {
            if (hostHolder.getUser()==null){
                return WendaUtil.getJSONString(999,"请先登录");
            }
            //查询用户名是否存在
            User userByName = userService.getUserByName(toname);
            if (userByName==null){
                return WendaUtil.getJSONString(1,"用户名不存在");
            }
            if (letterContent.trim().length()==0){
                return WendaUtil.getJSONString(1,"发送信息不能为空");
            }
            //构造消息对象
            Message message=new Message();
            message.setFromId(hostHolder.getUser().getId());
            message.setToId(userByName.getId());
            message.setContent(letterContent);
            message.setCreatedDate(new Date());

            int i = messageService.addMessage(message);
            return WendaUtil.getJSONString(0);//插入消息成功
        } catch (Exception e){
            e.printStackTrace();
            return WendaUtil.getJSONString(1, "发信失败");
        }

    }

    @RequestMapping(path = {"/msg/list"}, method = {RequestMethod.GET})
    public String getConversationList(Model model) {
        if (hostHolder.getUser() == null) {
            return "redirect:/reglogin";
        }
        int localUserId = hostHolder.getUser().getId();
        List<Message> conversationList = messageService.getConversationList(localUserId, 0, 10);
        List<ViewObject> conversations = new ArrayList<ViewObject>();
        for (Message message : conversationList) {
            ViewObject vo = new ViewObject();
            vo.set("message", message);
            int targetId = message.getFromId() == localUserId ? message.getToId() : message.getFromId();
            vo.set("user", userService.getUserById(targetId));
            vo.set("unread", messageService.getConversationUnreadCount(localUserId, message.getConversationId()));
            conversations.add(vo);
        }
        model.addAttribute("conversations", conversations);
        return "letter";
    }
    @RequestMapping(path = {"/msg/detail"}, method = {RequestMethod.GET})
    public String getConversationDetail(Model model, @RequestParam("conversationId") String conversationId) {
        try {
            List<Message> messageList = messageService.getConversationDetail(conversationId, 0, 10);

            List<ViewObject> messages = new ArrayList<ViewObject>();
            for (Message message : messageList) {
                ViewObject vo = new ViewObject();
                vo.set("message", message);
                vo.set("user", userService.getUserById(message.getFromId()));
                messages.add(vo);
            }
            model.addAttribute("messages", messages);
        } catch (Exception e) {
           // logger.error("获取详情失败" + e.getMessage());
        }
        return "letterDetail";
    }

    //删除message
    @RequestMapping(value = "/msg/deleteMessage",method = RequestMethod.POST)
    @ResponseBody
    public String deleteMessage(@RequestParam("messageid") int messageid){

        Map<String,String> map=new HashMap<>();

        //1 判断用户是否已经登陆，如果没登陆的话，请先登录
        if (hostHolder.getUser()==null){
           return  "redirect:reglogin";
        }else {
            String conversationId = messageService.getMessageById(messageid).getConversationId();
            boolean successs= messageService.deleteMessageById(messageid);

            JSONObject jsonObject=new JSONObject();
            jsonObject.put("url","http://localhost:8080/msg/detail?conversationId="+conversationId);
            return jsonObject.toJSONString();
        }


    }
}
