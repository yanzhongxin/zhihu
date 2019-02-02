package com.yanzhongxin.zhihu.contorler;

import com.yanzhongxin.zhihu.async.EventModel;
import com.yanzhongxin.zhihu.async.EventProducer;
import com.yanzhongxin.zhihu.async.EventType;
import com.yanzhongxin.zhihu.model.*;
import com.yanzhongxin.zhihu.service.CommentService;
import com.yanzhongxin.zhihu.service.FollowService;
import com.yanzhongxin.zhihu.service.QuestionService;
import com.yanzhongxin.zhihu.service.UserService;
import com.yanzhongxin.zhihu.util.WendaUtil;
import org.omg.PortableServer.LIFESPAN_POLICY_ID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.BinaryClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/23 9:54
 */
@Controller
public class FollowControler {
    @Autowired
    FollowService followService;

    @Autowired
    HostHolder hostHolder;
    @Autowired
    EventProducer eventProducer;

    @Autowired
    UserService userService;
    @Autowired
    QuestionService questionService;

    @Autowired
    CommentService commentService;
    /*1。用户a关注用户b*/
    @RequestMapping(value = "/followUser",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String followerUser(@RequestParam("userId") int userid){
        if (hostHolder.getUser()==null){
            return WendaUtil.getJSONString(999);
        }
        //有可能失败，因为如果同一个用户两次点击同一个关注，第二次不能添加到zset集合中
        //就会倒是添加失败
        boolean ret=followService.follow(hostHolder.getUser().getId(), EntityType.User,userid);

        //添加成功的话，就给关注着发一条信息。说明某某用户关注了您
        if (ret){
            //关注成功发送信息
            eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
            .setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.User)
            .setEntityId(userid).setExt("username",hostHolder.getUser().getName()).setEntityOwnerId(userid));
            //给自己的时间轴加上。我关注了谁谁谁
            eventProducer.fireEvent(new EventModel(EventType.FOLLOW_USER)
                    .setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.User)
                    .setEntityId(userid).setExt("username",userService.getUserById(userid).getName()).setEntityOwnerId(userid));
        }
        //返回用户hostholder.getuser的关注人数

        //1 表示成功
        return WendaUtil.getJSONString(ret?0:1,String.valueOf(
           followService.getFolloerCount(hostHolder.getUser().getId(),EntityType.User)
        ));
    }

    /*1。用户a取消用户b*/
    @RequestMapping(value = "/unfollowUser",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String unfollowUser(@RequestParam("userId") int userid){
        if (hostHolder.getUser()==null){
            return WendaUtil.getJSONString(999);
        }
        //有可能失败，因为如果同一个用户两次点击同一个关注，第二次不能添加到zset集合中
        //就会倒是添加失败
        boolean ret=followService.unfollow(hostHolder.getUser().getId(), EntityType.User,userid);

        //添加成功的话，就给关注着发一条信息。说明某某用户关注了您
        if (ret){
            //取消关注成功后发送信息
            eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW) //不关注的话，可以不发问题。
                    .setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.User)
                    .setEntityId(userid).setExt("username",hostHolder.getUser().getName()).setEntityOwnerId(userid));
        }
        //返回用户hostholder.getuser的关注人数

        //1 表示成功
        return WendaUtil.getJSONString(ret?0:1,String.valueOf(
                followService.getFolloerCount(hostHolder.getUser().getId(),EntityType.User)
        ));
    }

    /*1。用户a关注问题b*/
    @RequestMapping(value = "/followQuestion",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String followQuestion(@RequestParam("questionId") int questionId){
        if (hostHolder.getUser()==null){
            return WendaUtil.getJSONString(999);
        }
        Question querstionByid = questionService.getQuerstionByid(questionId);

        if (querstionByid==null){
            return WendaUtil.getJSONString(1,"问题不存在");
        }

        //有可能失败，因为如果同一个用户两次点击同一个关注，第二次不能添加到zset集合中
        //就会倒是添加失败
        boolean ret=followService.follow(hostHolder.getUser().getId(), EntityType.Question,questionId);

        //添加成功的话，就给关注者发一条信息。说明某某用户关注了您
        if (ret){
            //取消关注成功后发送信息
            eventProducer.fireEvent(new EventModel(EventType.FOLLOW) //不关注的话，可以不发问题。
                    .setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.User)
                    .setEntityId(questionId).setExt("username",hostHolder.getUser().getName()).setEntityOwnerId(querstionByid.getUserId()));
            //我关注了某个问题
            eventProducer.fireEvent(new EventModel(EventType.FOLLOW) //不关注的话，可以不发问题。
                    .setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.Question)
                    .setEntityId(questionId).setExt("username",hostHolder.getUser().getName()).setEntityOwnerId(querstionByid.getUserId()));

        }
        Map<String,Object> info=new HashMap<>();
        info.put("headUrl",hostHolder.getUser().getHeadUrl());
        info.put("name",hostHolder.getUser().getName());
        info.put("id",hostHolder.getUser().getId());
        info.put("count",followService.getFolloerCount(EntityType.Question,questionId));

        return WendaUtil.getJSONString(ret?0:1,info);
    }

    /*1。用户a取消关注问题b*/
    @RequestMapping(value = "/unfollowQuestion",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String unfollowQuestion(@RequestParam("questionId") int questionId){
        if (hostHolder.getUser()==null){
            return WendaUtil.getJSONString(999);
        }
        Question querstionByid = questionService.getQuerstionByid(questionId);

        if (querstionByid==null){
            return WendaUtil.getJSONString(1,"问题不存在");
        }

        //有可能失败，因为如果同一个用户两次点击同一个关注，第二次不能添加到zset集合中
        //就会倒是添加失败
        boolean ret=followService.unfollow(hostHolder.getUser().getId(), EntityType.Question,questionId);

        //添加成功的话，就给关注着发一条信息。说明某某用户关注了您
        if (ret){
            //取消关注成功后发送信息
            eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW) //不关注的话，可以不发问题。
                    .setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.User)
                    .setEntityId(questionId).setExt("username",hostHolder.getUser().getName()).setEntityOwnerId(querstionByid.getUserId()));
        }
        Map<String,Object> info=new HashMap<>();
        info.put("id",hostHolder.getUser().getId());
        info.put("count",followService.getFolloerCount(EntityType.Question,questionId));

        return WendaUtil.getJSONString(ret?0:1,info);
    }

    @RequestMapping(value = "/user/{uid}/followers",method = RequestMethod.GET) //查看某个用户的粉丝列表
    public String followers(Model model, @PathVariable("uid") int userid){
        List<Integer> follers = followService.getFollers(EntityType.User, userid, 10);
        if (hostHolder.getUser()!=null){
            model.addAttribute("followers",getUsersInfo(hostHolder.getUser().getId(),follers));
        }else {
            model.addAttribute("followers",getUsersInfo(
                    0,follers
            ));//用户没有登陆，也是可以查看该用户的所有的粉丝
        }

        //设置访问用户有多少个粉丝
        model.addAttribute("followerCount",followService.getFolloerCount(
                EntityType.User,userid
        ));
        //设置访问的用户的基本信息
        model.addAttribute("curUser",userService.getUserById(userid));
        return "followers";
    }

    @RequestMapping(value = "/user/{uid}/followees",method = RequestMethod.GET) //查看某个用户所有的关注列表
    public String followees(Model model,@PathVariable("uid") int userId){
        //查看用户uid,的所有10个用户列表，
        List<Integer> followeeIds = followService.getFollees(EntityType.User,userId , 0, 10);

        if (hostHolder.getUser() != null) {
            model.addAttribute("followees", getUsersInfo(hostHolder.getUser().getId(), followeeIds));
        } else {
            model.addAttribute("followees", getUsersInfo(0, followeeIds));
        }
        model.addAttribute("followeeCount", followService.getFollweeCount(userId, EntityType.User));
        model.addAttribute("curUser", userService.getUserById(userId));
        return "followees";
    }

    //找出List<Integer> userIds 粉丝对象所有的信息，包括当前访问者和他的信息，以及他的关注人数，粉丝人数等等
    private List<ViewObject> getUsersInfo(int localUseid,List<Integer> userIds){
        List<ViewObject> userInfos=new ArrayList<>();
        for (Integer uid:userIds) {
            //查询每个用户信息，包括用户基本信息，用户粉丝，用户关注，用户评论
            User userById = userService.getUserById(uid);
            if (userById == null) continue;
            ViewObject vo = new ViewObject();
            //设置用户基本信息
            vo.set("user", userById);
            //设置用户评论个数
            vo.set("commentCount", commentService.commentByUserId(uid));
            //设置用户粉丝个数
            vo.set("followerCount", followService.getFolloerCount(
                    EntityType.User, uid
            ));
            //设置每个用户赞的个数
            vo.set("zanNum",commentService.getAllZanCommentByUserId(uid));
            //设置跟随个数
            vo.set("followeeCount", followService.getFollweeCount(
                    uid, EntityType.User
            ));
            if (localUseid != 0) {//设置当前查看用户的粉丝,和我的关系。
                vo.set("followed", followService.isFolloer(localUseid, EntityType.User,
                        uid));
            } else {
                vo.set("followed", false);
            }
            userInfos.add(vo);

        }

        return userInfos;

    }

    //根据用户id获取他关注的问题id
    @RequestMapping("/getFollowQuerstionsByUserId")
    @ResponseBody
    public List<Question> getFollowQuestionsByUserId(@RequestParam("uid")int uid){
        List<Question> list=followService.getFollowQuestionsByUserId(uid);
        return list;
    }
}
