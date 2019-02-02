package com.yanzhongxin.zhihu.contorler;

import com.sun.org.apache.xpath.internal.operations.Mod;
import com.yanzhongxin.zhihu.dao.UserMapper;
import com.yanzhongxin.zhihu.service.CommentService;
import com.yanzhongxin.zhihu.service.FollowService;
import com.yanzhongxin.zhihu.service.QuestionService;
import com.yanzhongxin.zhihu.service.UserService;
import org.apache.zookeeper.data.Id;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.yanzhongxin.zhihu.model.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/20 16:54
 */
@Controller
public class IndexConroler {

    @Autowired
    UserMapper  userMapper;

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    FollowService followService;
    @Autowired
    HostHolder hostHolder;

    /* @Author Zhongxin Yan
     * @Description To Do,查询某个用户最新的十条记录
     * @Date 2019/1/20 20:44
     * @Param 
     * @return 
     */
    @RequestMapping(value = "/user/{userid}",method = RequestMethod.GET)
    public String showUserIndex(@PathVariable int userid,Model model){
        //获得该用户最新的十条动态
        model.addAttribute("vos",getQuestion(userid,0,10));

        /*${profileUser.user.name}
        ${profileUser.followerCount}粉丝 /
       ${profileUser.followeeCount}关注 /
        ${profileUser.commentCount} 回答 / 548 赞同*/

        User userById = userService.getUserById(userid);
        ViewObject viewObject=new ViewObject();
        //设置用户信息，包含了用户名
        viewObject.set("user",userById);
        //设置用户回答的个数 vo.question.commentCount
        viewObject.set("commentCount", commentService.commentByUserId(userid));
        //设置用户粉丝数
        viewObject.set("followerCount",followService.getFolloerCount(
                EntityType.User,userid
        ));
        //设置跟随了多少人。
        viewObject.set("followeeCount",followService.getFollweeCount(
                userid, EntityType.User
        ));

        //判断当前用户是不是访问页面用户的粉丝
        if (hostHolder.getUser()!=null){
            viewObject.set("followed",
                    followService.isFolloer(hostHolder.getUser().getId(),
                            EntityType.User,userid));
        }else {
            viewObject.set("followed",false);
        }

        /*http://localhost:8080/user/19*/
       /* 查询redis数据库，该用户获得过多少个赞*/
        long zanNum = commentService.getAllZanCommentByUserId(userid);
        model.addAttribute("zanNum",zanNum);

        model.addAttribute("profileUser",viewObject);
        return "profile";
    }


    /* @Author Zhongxin Yan
     * @Description To Do 查询最新的十条记录
     * @Date 2019/1/20 20:54
     * @Param
     * @return
     */
    @RequestMapping(path = {"/","index"},method = RequestMethod.GET)
    public String showIndex(Model model){
        //查询最新  的十个问题，显示到index网页中
        model.addAttribute("vos",getQuestion(0,0,10));

        //followCount
        return "index";

    }

    @RequestMapping(value = "insertUser",method = RequestMethod.GET)
    public String addUser(){
        User user=new User();
        user.setId(8);
        user.setName("zhongxin");
        user.setPassword("123");
        user.setSalt("111");
        user.setHeadUrl("http");
        userMapper.insert(user);

        return "index";
    }


    //根据用户id，查询最新的十条问题
    private List<ViewObject> getQuestion(int id, int off, int limit){
        List<Question> lastQuersion = questionService.getLastQuersion(id, off, limit);
        List<ViewObject> vos=new ArrayList<>();
        for (int i = 0; i < lastQuersion.size(); i++) {
            Question question =  lastQuersion.get(i);
            ViewObject viewObject=new ViewObject();
            viewObject.set("question",question);
            viewObject.set("user",userService.getUserById(question.getUserId()));
            long folloerCount = followService.getFolloerCount(EntityType.Question, question.getId());
            //设置首页，每次显示十个问题，每个问题的关注人数。根据 实体类型EntityType.question 实体id
            viewObject.set("followCount",folloerCount);
            vos.add(viewObject);
        }
        return vos;

    }
}
