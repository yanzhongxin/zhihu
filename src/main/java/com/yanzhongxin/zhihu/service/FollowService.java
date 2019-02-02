package com.yanzhongxin.zhihu.service;

import com.yanzhongxin.zhihu.model.EntityType;
import com.yanzhongxin.zhihu.model.Question;
import com.yanzhongxin.zhihu.util.JedisAdapter;
import com.yanzhongxin.zhihu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


/* @Author Zhongxin Yan
 * @Description a关注B。a是b的粉丝。
 * @Date 2019/1/23 9:54
 * @Param
 * @return
 */
@Service
public class FollowService {
    @Autowired
     JedisAdapter jedisAdapter;

    @Autowired
    QuestionService questionService;
    //用户关注的对象可以是1，问题，2 回答 3 用户。
     //如果用户id,关注问题EntityType, 问题id=entityId。等价于用户id是这个问题的粉丝

     public boolean follow(int userid,int entitiyType,int entityId){

         if (userid==entityId&&(entitiyType==EntityType.User)){
             //说明是关注自己。
             return  false;
         }

            //这个问题的粉丝key=entityType+entityId。
            String followerKey= RedisKeyUtil.getFollowerKey(entitiyType,entityId);
            /*用户id的关注key,表示用户id,关注了那些问题。
            这个key就表示用户(id) 关注了那些问题。
            一个用户关注的类型不同key也不同，比如用户的关注问题，关注人这是两个key*/
            String followeeKey=RedisKeyUtil.getFolloweeKey(userid,entitiyType);

         Date date=new Date();
         //jedis开始事务
         //给实体添加粉丝。
         Jedis jedis = jedisAdapter.getJedis();
         Transaction tx = jedisAdapter.multi(jedis);
      /*   1 事务1 给实体entity + entity添加粉丝。把用户放到实体的粉丝集合中
                 按照zset集合中，用时间表示权重，越重表示是最新的关注*/
         tx.zadd(followerKey,date.getTime(),String.valueOf(userid));
       /*  事务2 给当前用户的id+关注类型 的zset集合中加上entityid*/
        tx.zadd(followeeKey,date.getTime(),String.valueOf(entityId));

         List<Object> ret = jedisAdapter.exec(tx, jedis);

         return ret.size() == 2 && (Long) ret.get(0) > 0 && (Long) ret.get(1) > 0;
     }

     //取消关注
    public boolean unfollow(int userid,int entitiyType,int entityId){

        //这个问题的粉丝key=entityType+entityId。
        String followerKey= RedisKeyUtil.getFollowerKey(entitiyType,entityId);
            /*用户id的关注key,表示用户id,关注了那些问题。
            这个key就表示用户(id) 关注了那些问题。
            一个用户关注的类型不同key也不同，比如用户的关注问题，关注人这是两个key*/
        String followeeKey=RedisKeyUtil.getFolloweeKey(userid,entitiyType);

        Date date=new Date();
        //jedis开始事务
        //给实体添加粉丝。
        Jedis jedis = jedisAdapter.getJedis();
        Transaction tx = jedisAdapter.multi(jedis);
      /*   1 事务1 给实体entity + entity添加粉丝。把用户从实体的粉丝集合中删除
                 按照zset集合中，用时间表示权重，越重表示是最新的关注*/
        tx.zrem(followerKey,String.valueOf(userid));
       /*  事务2 给当前用户的id+关注类型 的zset集合中取消entityid*/
        tx.zrem(followeeKey,String.valueOf(entityId));

        List<Object> ret = jedisAdapter.exec(tx, jedis);

        return ret.size() == 2 && (Long) ret.get(0) > 0 && (Long) ret.get(1) > 0;
    }

    //获取某个实体的粉丝个数，比如对某个问题的关注人数。对某个人物的关注人数
    public List<Integer> getFollers(int entityType,int entityId,int count){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        Set<String> zrange = jedisAdapter.zrange(followerKey, 0, count);
        //这个set集合中放的是粉丝的userid
        List<Integer> folloerIdsFromSet = getFolloerIdsFromSet(zrange);
        return folloerIdsFromSet;
    }

    //从偏移量offset开始找count个粉丝,倒叙查找，从权重最大的开始找，也就是
    //最新的粉丝
    public List<Integer> getFollers(int entityType,int entityId,int offset,int count){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        Set<String> zrange = jedisAdapter.zrevrange(followerKey, offset, offset+count);
        //这个set集合中放的是粉丝的userid
        List<Integer> folloerIdsFromSet = getFolloerIdsFromSet(zrange);
        return folloerIdsFromSet;
    }


    //获取某个实体的关注对象的所有id。比如用户id,关注的问题id.
    //followeeKey=用户id+关注问题类型：value表示用户关注的很多个问题的id
    public List<Integer> getFollees(int userid,int entityType,int count){
        //1 获取用户关注类型的key,按照时间从旧开始查找count个
        String followeeKey = RedisKeyUtil.getFolloweeKey(userid, entityType);
        Set<String> zrange = jedisAdapter.zrange(followeeKey, 0, count);

        return getFolloerIdsFromSet(zrange);
    }

    //从偏移量offset开始找count个关注的问题，或者人,倒叙查找，从权重最大的开始找，也就是
    //最新的关注类型的id。比
    public List<Integer> getFollees(int entityType,int userId,int offset,int count){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        Set<String> zrevrange = jedisAdapter.zrevrange(followeeKey, offset, offset + count);
        return getFolloerIdsFromSet(zrevrange);
    }
    /*public static void main(String[] args) {
        JedisPool     pool = new JedisPool("192.168.25.128",6379);
        Jedis resource = pool.getResource();
        Set<String> zrevrange = resource.zrevrange(RedisKeyUtil.getFolloweeKey(19, EntityType.User), 0, 10);
        System.out.println(RedisKeyUtil.getFolloweeKey(19, EntityType.User));
       // FOLLOWEE:3:19
        System.out.println(zrevrange.size());
        resource.close();;
    }*/
    //获取实体的粉丝个数
    public long getFolloerCount(int entityType,int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return jedisAdapter.zcard(followerKey);
    }



    //获取关注的个数
    public long getFollweeCount(int userid,int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userid, entityType);
        return jedisAdapter.zcard(followeeKey);
    }
    //把set<string>集合转化成 list<integer>集合
    public List<Integer> getFolloerIdsFromSet(Set<String> set){
        List<Integer> list=new ArrayList<>();
        for (String id:set){
            list.add(Integer.parseInt(id));
        }

        return list;
    }

    //判断某个用户是否关注了某个实体
    public boolean isFolloer(int userid,int entityType,int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

       return jedisAdapter.zscore(followerKey,String.valueOf(userid))!=null;
    }

    public List<Question> getFollowQuestionsByUserId(int uid) {
        Set<String> zrange = jedisAdapter.zrange(RedisKeyUtil.getFolloweeKey(uid, EntityType.Question), 0, Integer.MAX_VALUE);
        List<Integer> questionids = getFolloerIdsFromSet(zrange);
        List<Question> list=new ArrayList<>();
        for (Integer qid:questionids){
            list.add(questionService.getQuerstionByid(qid));
        }
        return list;//返回用户id关注的所有的问题
    }
}
