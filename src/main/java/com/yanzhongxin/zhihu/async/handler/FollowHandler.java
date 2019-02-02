package com.yanzhongxin.zhihu.async.handler;

import com.yanzhongxin.zhihu.async.EventHandler;
import com.yanzhongxin.zhihu.async.EventModel;
import com.yanzhongxin.zhihu.async.EventType;
import com.yanzhongxin.zhihu.model.EntityType;
import com.yanzhongxin.zhihu.model.Message;
import com.yanzhongxin.zhihu.service.MessageService;
import com.yanzhongxin.zhihu.util.WendaUtil;
import org.apache.solr.client.solrj.SolrRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/23 10:50
 */
@Component
public class FollowHandler implements EventHandler {
    @Autowired
    MessageService messageService;
    @Override
    public void doHandle(EventModel model) {
        EventType type = model.getType(); //获取时间类型，比如EventType.follow。或者

        if(type==EventType.FOLLOW){//点击关注的这里处理，可以是关注问题，或者是关注人
            Message message=new Message();
            message.setFromId(WendaUtil.SYSTEM_USERID);//设置发送信息的为系统管理员账号
            message.setToId(model.getEntityOwnerId());
            Date date=new Date();
            message.setCreatedDate(date);

            if (model.getEntityType()== EntityType.User){
                //说明，用户a关注了关注了用户b
                message.setContent("知乎用户"+model.getExt("username") +"关注了你"+
                        "<a href=\"http://127.0.0.1:8080/user/"+ model.getActorId()+"\">点击这里，快速查看谁关注了你</a>");
            }else if (model.getEntityType()==EntityType.Question){
                //说明，用户a关注了关注了用户b发布的问题
                message.setContent("知乎用户"+model.getExt("username") +"关注了你的问题"+
                        "<a href=\"http://127.0.0.1:8080/question/"+ model.getEntityId()+"\">点击这里，查看你的提问</a>");

            }

            messageService.addMessage(message);
        }else if (type==EventType.UNFOLLOW){ //取消关注这里决定是否进行通知。一般不进行通知，比如a取消了对b的关注，那么不应该通知b

        }

    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.FOLLOW,EventType.UNFOLLOW); //处理关注和不关注的问题
    }
}
