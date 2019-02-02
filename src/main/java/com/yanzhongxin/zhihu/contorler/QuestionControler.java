package com.yanzhongxin.zhihu.contorler;

import com.yanzhongxin.zhihu.async.EventModel;
import com.yanzhongxin.zhihu.async.EventProducer;
import com.yanzhongxin.zhihu.async.EventType;
import com.yanzhongxin.zhihu.model.*;
import com.yanzhongxin.zhihu.service.*;
import com.yanzhongxin.zhihu.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.management.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.AbstractDocument;
import javax.swing.text.View;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/21 12:57
 */
@Controller
public class QuestionControler {

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;
    @Autowired
    QuestionService questionService;

    @Autowired
    CommentService commentService;

    @Autowired
    LikeService likeService;

    @Autowired
    FollowService followService;

    @Autowired
    EventProducer eventProducer;
    @RequestMapping(value = "/question/add",method = RequestMethod.POST)
    @ResponseBody
    public String addQuersion(@RequestParam("title") String title, @RequestParam("content")String content, HttpServletResponse response,
                              HttpServletRequest request){

        try {
            Question question=new Question();
            question.setContent(content);
            question.setTitle(title);
            question.setCreatedDate(new Date());
            if (hostHolder.getUser()==null){
                //说明用户没有登陆，设置匿名用户
               // question.setUserId(WendaUtil.ANONYMOUS_USERID);
                //应该让用户去登陆，然后才能发表评论
                return "redirect:reglogin?next="+request.getRequestURL();
            }else{
                question.setUserId(hostHolder.getUser().getId());
            }
            if (questionService.addQuestion(question)>0){
                //插入问题成功，
                //异步队列，发送事件模型，发布问题模型，模仿知乎：某某的问题 等你来回答
                eventProducer.fireEvent(new EventModel(EventType.ANSWER_QUESTION) //不关注的话，可以不发问题。
                        .setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.Question)
                        .setEntityId(question.getId()).setEntityOwnerId(hostHolder.getUser().getId()));

               eventProducer.fireEvent(new EventModel(EventType.ADD_QUESTION) //个人时间轴问题。
                        .setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.Question)
                        .setEntityId(question.getId()).setEntityOwnerId(hostHolder.getUser().getId())
               .setExt("title",title).setExt("content",content));


                return WendaUtil.getJSONString(0);
            }

        }catch (Exception e){

        }
        return WendaUtil.getJSONString(1, "失败");


    }

    @RequestMapping(value = "/question/{qid}",method = RequestMethod.GET)
    public String showQuestion(@PathVariable("qid") Integer qid, Model model){

        Question question = questionService.getQuerstionByid(qid);
        model.addAttribute("question", question);

        List<Comment> commentList = commentService.getCount(1, qid);
        List<ViewObject> comments = new ArrayList<ViewObject>();
        for (Comment comment : commentList) {
            ViewObject vo = new ViewObject();
            vo.set("comment", comment);

            //设置当前用户对这个评论的喜欢状态
            if (hostHolder.getUser()==null){
                vo.set("liked",0); //如果用户没有登陆,不现实喜欢的状态。
            }else {
                //登陆的话，显示喜欢状态 1 -1 0
                vo.set("liked",likeService.getLikeStatus(hostHolder.getUser().getId(),2,comment.getId()));
            }
            //设置每个问题的喜欢人数。
            vo.set("likeCount",likeService.getLikeCount(2,comment.getId()));
            vo.set("user", userService.getUserById(comment.getUserId()));
            comments.add(vo);
        }


        model.addAttribute("comments", comments);
       // model.addAttribute("answerNum",commentList.size());//返回前端回答此问题的人数
        //获取关注此问题的20个用户，显示他们的头像，姓名
        List<ViewObject>  list=new ArrayList<>();
        List<Integer> follers = followService.getFollers(EntityType.Question, qid, 20);
        for (int id:follers){
            ViewObject vo=new ViewObject();
            User u = userService.getUserById(id);
            if (u==null){
                continue;
            }
            vo.set("name", u.getName());
            vo.set("headUrl", u.getHeadUrl());
            vo.set("id", u.getId());
            list.add(vo);
        }
        model.addAttribute("followUsers",list);//给问题20个关注的用户信息
        if (hostHolder.getUser()!=null){
            //当前观看问题的用户是否为当前问题的关注者
            model.addAttribute("followed",followService.isFolloer(hostHolder.getUser()
            .getId(),EntityType.Question,qid));
        }else {//没有登陆的话，默认不是关注的
            model.addAttribute("followed",false);
        }
        return "detail";
    }

    //根据userid获得该用户所有的提出的问题
    @RequestMapping(value = "/question/getQuestionsByUserId",method = RequestMethod.GET)
    @ResponseBody
    public List<Question> getQuesiontsByUserid(@RequestParam("uid") int uid,HttpServletRequest request){

        List<Question> questionsByUserId = questionService.getQuestionsByUserId(uid);
        return questionsByUserId;
    }
}
