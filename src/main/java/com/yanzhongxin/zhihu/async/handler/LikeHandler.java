package com.yanzhongxin.zhihu.async.handler;


import com.yanzhongxin.zhihu.async.EventHandler;
import com.yanzhongxin.zhihu.async.EventModel;
import com.yanzhongxin.zhihu.async.EventType;
import com.yanzhongxin.zhihu.model.Message;
import com.yanzhongxin.zhihu.model.User;
import com.yanzhongxin.zhihu.service.MessageService;
import com.yanzhongxin.zhihu.service.UserService;
import com.yanzhongxin.zhihu.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by nowcoder on 2016/7/30.
 */
@Component
public class LikeHandler implements EventHandler {
    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Override
    public void doHandle(EventModel model) {
        Message message = new Message();
        message.setFromId(WendaUtil.SYSTEM_USERID);
        message.setToId(model.getEntityOwnerId());
        message.setCreatedDate(new Date());
        User user = userService.getUserById(model.getActorId());
        String content="用户" + user.getName()
                + "赞了你的评论 <a href=\"http://127.0.0.1:8080/question/"+ model.getExt("questionId")+"\">猛戳这里</a>";

        message.setContent(content);

        //如果用户多次点击，只发送一条提醒信息。搜索数据库中是否已经包含了相同的content,如果找到，说明已经发送过信息了
        boolean existMessageByContent = messageService.isExistMessageByContent(content);
        // true表示已经存在，不能再发信息了；false表示不存在可以发。

        //排除掉自己的点赞和，同一个人多次点赞同一个信息，发送多条消息。
        if ((model.getActorId()!=model.getEntityOwnerId())&&(!existMessageByContent))  //点赞自己，不发送信息。
             messageService.addMessage(message);

    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LIKE);
    }
}
