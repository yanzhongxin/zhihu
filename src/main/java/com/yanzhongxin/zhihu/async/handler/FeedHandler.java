package com.yanzhongxin.zhihu.async.handler;

import com.alibaba.fastjson.JSONObject;
import com.yanzhongxin.zhihu.async.EventHandler;
import com.yanzhongxin.zhihu.async.EventModel;
import com.yanzhongxin.zhihu.async.EventType;
import com.yanzhongxin.zhihu.model.*;
import com.yanzhongxin.zhihu.service.*;
import com.yanzhongxin.zhihu.util.JedisAdapter;
import com.yanzhongxin.zhihu.util.RedisKeyUtil;
import com.yanzhongxin.zhihu.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/23 10:50
 */
@Component
public class FeedHandler implements EventHandler {
    @Autowired
    FollowService followService;

    @Autowired
    UserService userService;

    @Autowired
    FeedService feedService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    QuestionService questionService;

    private String buildFeedDate(EventModel eventModel){
        Map<String,String> map=new HashMap<>();
        // 触发用户是通用的
        User userById = userService.getUserById(eventModel.getActorId());
        if (userById==null){
         return null;
        }

        map.put("userId",String.valueOf(userById.getId()));
        map.put("userHead",userById.getHeadUrl());
        map.put("userName",userById.getName());
        //如果事件模型中是评论问题，或者是关注问题的话。把这个问题相关的问题id，问题的标题放到map中。
        //如果这个是用户A关注了某个问题，或者用户A评论了某个问题。就会把新鲜事推荐给A的粉丝。
       /* if (eventModel.getType()==EventType.COMMENT||
                (eventModel.getType()==EventType.FOLLOW&&eventModel.getEntityType()
                ==EntityType.Question)){
            Question question=questionService.getQuerstionByid(eventModel.getEntityId());
            if (question == null) {
                return null;
            }
            map.put("questionId",String.valueOf(question.getId()));
            map.put("questionTitle",question.getTitle());
            return JSONObject.toJSONString(map);
        }*/
       //用户a关注了某个问题
        if (eventModel.getType()==EventType.FOLLOW&&eventModel.getEntityType()
                        ==EntityType.Question){
            Question question=questionService.getQuerstionByid(eventModel.getEntityId());
            if (question == null) {
                return null;
            }
            map.put("questionId",String.valueOf(question.getId()));
            map.put("questionTitle",question.getTitle());
            return JSONObject.toJSONString(map);
        }
        //用户a关注了某个人,加入时间轴
        if (eventModel.getType()==EventType.FOLLOW_USER){
            /*.setEntityId(userid).setExt("username",hostHolder.getUser().getName()).setEntityOwnerId(userid).
           */
            map.put("followeename",eventModel.getExt("username"));
            map.put("userid",eventModel.getEntityId()+"");
            return JSONObject.toJSONString(map);
        }
        //用户a评论了某个问题,这时候就需要把评论的内容，评论的问题也加上。
        if (eventModel.getType()==EventType.COMMENT){
            Question question=questionService.getQuerstionByid(eventModel.getEntityId());
            if (question == null) {
                return null;
            }
            map.put("questionId",String.valueOf(question.getId()));//设置问题的id
            map.put("questionTitle",question.getTitle());//设置评论的问题标题
            map.put("comment", eventModel.getExts().get("comment"));//设置评论的内容
            return JSONObject.toJSONString(map);
        }
        //如果是用户A刚刚发布了一个问题，那么提醒他的粉丝去评论
        if (eventModel.getType()==EventType.ANSWER_QUESTION){
            Question question=questionService.getQuerstionByid(eventModel.getEntityId());
            if (question == null) {
                return null;
            }
            map.put("questionId",String.valueOf(question.getId()));//设置问题的id
            map.put("questionTitle",question.getTitle());//设置评论的问题标题
            return JSONObject.toJSONString(map);
        }

        //如果用户刚刚发布了一个问题的话，那么需要把这个新鲜事放到他自己的时间轴队列中
        if (eventModel.getType()==EventType.ADD_QUESTION){
            Question question=questionService.getQuerstionByid(eventModel.getEntityId());
            if (question == null) {
                return null;
            }
            map.put("questionId",String.valueOf(question.getId()));//设置问题的id
            map.put("questionTitle",question.getTitle());//设置评论的问题标题
            return JSONObject.toJSONString(map);
        }



        return null;
    }

    @Override
    public void doHandle(EventModel model) {
        //为了测试，把model的useriD随机一下
        Random random=new Random();
        //设置事件模型的的触发人。
        model.setActorId(model.getActorId());

        //创建一个新鲜事
        Feed feed=new Feed();
        feed.setCreatedDate(new Date());
        feed.setType(model.getType().getValue());
        feed.setUserId(model.getActorId());
        feed.setData(buildFeedDate(model));
        
        if (feed.getData()==null){
         return;   
        }
        feedService.addFeed(feed);
        
        //获取所有的粉丝
        List<Integer> follers = followService.getFollers(EntityType.User, model.getActorId(), Integer.MAX_VALUE);
        //系统队列
        follers.add(0);
        //给所有的粉丝推送事件
        for (int followerr:follers){
            String timelineKey = RedisKeyUtil.getTimelineKey(followerr);
            //把这次新鲜事feed的id推送给粉丝的时间轴timeline中。等该用户从时间轴pull中拉数据
            jedisAdapter.lpush(timelineKey,String.valueOf(feed.getId()));
            // 限制最长长度，如果timelineKey的长度过大，就删除后面的新鲜事

        }

        //除了把这些信息推送到粉丝的时间轴redis key中之外，还应该推送到自己的个人时间轴上
        //比如时间轴上有我提出了什么问题，我关注了什么问题，我评论了什么问题，我关注了谁。
        jedisAdapter.lpush(RedisKeyUtil.getBIZ_PersonTimeline(model.getActorId()), String.valueOf(feed.getId()));
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(new EventType[]{EventType.COMMENT, EventType.FOLLOW,EventType.ANSWER_QUESTION,
        EventType.ADD_QUESTION,EventType.FOLLOW_USER});
    }
}
