package com.yanzhongxin.zhihu.service;

import com.yanzhongxin.zhihu.dao.FeedMapper;
import com.yanzhongxin.zhihu.model.Feed;
import com.yanzhongxin.zhihu.util.JedisAdapter;
import com.yanzhongxin.zhihu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/30 11:14
 */
@Service
public class FeedService {

    @Autowired
    FeedMapper feedMapper;
    @Autowired
    JedisAdapter jedisAdapter;
    //插入一条Feed新鲜事
    public boolean addFeed(Feed feed){
        int insert = feedMapper.insert(feed);
        return insert>0; //插入数据大于o表示插入成功
    }

    //根据id查询Feed流信息
    public Feed getFeedById(int id){
        Feed feed = feedMapper.selectByPrimaryKey(id);
        return feed;
    }

    //查询我关注的用户的最新新鲜事
    public List<Feed> getUsersFeed(int maxId,List<Integer> ids,int count ){
        return feedMapper.queryUserFeeds(maxId,ids,count);
    }

    //根据用户id获取个人隐私的时间轴
    public List<Feed> getMyPersonTimeLine(int uid) {
        List<String> lrange = jedisAdapter.lrange(RedisKeyUtil.getBIZ_PersonTimeline(uid), 0, Integer.MAX_VALUE);
        List<Feed> list=new ArrayList<>();
        for (String feedid:lrange){
            Feed feed = feedMapper.selectByPrimaryKey(Integer.parseInt(feedid));
            list.add(feed);
        }
        return list;//返回所有的feed信息
    }

    public static void main(String[] args) {

    }
}
