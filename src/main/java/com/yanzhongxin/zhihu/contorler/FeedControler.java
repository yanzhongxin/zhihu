package com.yanzhongxin.zhihu.contorler;

import com.yanzhongxin.zhihu.model.EntityType;
import com.yanzhongxin.zhihu.model.Feed;
import com.yanzhongxin.zhihu.model.HostHolder;
import com.yanzhongxin.zhihu.service.FeedService;
import com.yanzhongxin.zhihu.service.FollowService;
import com.yanzhongxin.zhihu.util.JedisAdapter;
import com.yanzhongxin.zhihu.util.RedisKeyUtil;
import org.omg.PortableServer.LIFESPAN_POLICY_ID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/30 14:02
 */
@Controller
public class FeedControler {
    @Autowired
    FollowService followService;
    @Autowired
    FeedService feedService;
    @Autowired
    HostHolder hostHolder;

    @Autowired
    JedisAdapter jedisAdapter;
    //用户登录后，直接从数据库中拉，用户关注的人最新信息
    @RequestMapping(path = {"/pullfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
    private String pullfeeds(Model model){
        //判断当前用户是否已经登陆，如果登陆的话就从数据库中拉，他关注的人的信息
        int uid = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<Integer> followeeUid=new ArrayList<>();
        if (uid!=0){//如果用户非空，获得该用户的关注人物
            followeeUid= followService.getFollees(uid, EntityType.User, Integer.MAX_VALUE);
        }
        //followeeUid中存放用户的所有关注人
        //查询数据库，这些关注的人的新鲜事
        List<Feed> usersFeed = feedService.getUsersFeed(Integer.MAX_VALUE, followeeUid, 10);//查询用户关注人的十条新鲜事
        model.addAttribute("feeds",usersFeed);
        return "feeds";

    }

    //推模式，比如a发送一条信息之后，推送给所有的粉丝。
   /* 1 从redis中的个人用户时间轴中，找到最新的十条feed流id
    2 查询feed数据库，把这些feed找出来，放到list集合中。
    3 返回给页面这个用户最新的10条动态。*/
    @RequestMapping(path = {"/pushfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
    private String getPushFeeds(Model model){
        //判断当前用户是否已经登陆，如果登陆的话就从数据库中拉，他关注的人的信息
        int uid = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        String timelinekey= RedisKeyUtil.getTimelineKey(uid);
        //当前用户时间轴，最新的十条feed流id，然后查出具体的Feed信息放大list集合中，返回给前端。
        List<String> feedIds = jedisAdapter.lrange(timelinekey, 0, 10);
        List<Feed> list=new ArrayList<>();
        for (String feedid:feedIds){
            Feed feedById = feedService.getFeedById(Integer.parseInt(feedid));
            if (feedById!=null){
                list.add(feedById);
            }
        }
        //把新鲜事交给detail页面
        model.addAttribute("feeds",list);

        return "feeds";
    }


    //用户登录后，直接从数据库中我自己的个人时间轴信息
    @RequestMapping(value = "/mytimeline1", method = {RequestMethod.GET, RequestMethod.POST})
    private String pullfeedsMyselfTimeLine(Model model, @RequestParam("uid") int id){
        //判断当前用户是否已经登陆，如果登陆的话就从数据库中拉，他关注的人的信息
        int uid = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<Integer> followeeUid=new ArrayList<>();
        List<Feed> list=null;
        if (uid!=0){//如果用户非空，获得该用户的关注人物
            list=feedService.getMyPersonTimeLine(id);
        }
        model.addAttribute("feeds",list);
        return "personalfeeds";
    }
    @RequestMapping(value = "/mytimeline",method = {RequestMethod.GET, RequestMethod.POST})
    public String test(Model model){
        //判断当前用户是否已经登陆，如果登陆的话就从数据库中拉，他关注的人的信息
        int uid = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<Integer> followeeUid=new ArrayList<>();
        List<Feed> list=null;
        if (uid!=0){//如果用户非空，获得该用户的关注人物
            list=feedService.getMyPersonTimeLine(uid);
        }
        for (Feed feed:list){
            System.out.println("feed="+feed.getCreatedDate());
        }
        model.addAttribute("feeds",list);
        return "personalfeeds";
    }

}
