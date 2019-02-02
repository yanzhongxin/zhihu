package com.yanzhongxin.zhihu.service;

import com.yanzhongxin.zhihu.util.JedisAdapter;
import com.yanzhongxin.zhihu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/22 10:47
 */
@Service
public class LikeService {
    @Autowired
    JedisAdapter jedisAdapter;

    //一个问题，或者回答对应两个set集合，分别是点赞集合，点彩集合。根据问题类型，问题id生成对应的key
    public long like(Integer useid, int type, int commentId) {
        //某个人userid，喜欢类型为type，id=commmentid的问题或者回答,1表示问题，2表示回答
        //点赞的集合加一
        String likeQuestionCommentKey= RedisKeyUtil.getLikeKey(type,commentId);
        jedisAdapter.sadd(likeQuestionCommentKey,useid+"");//把点赞或者点踩的用户id放到set集合中

        //点彩的集合删除当前的用户
        String disLikeKey=RedisKeyUtil.getDisLikeKey(type,commentId);
        jedisAdapter.srem(disLikeKey,useid+"");

        //返回所有的点赞人数
        return jedisAdapter.scard(likeQuestionCommentKey);
    }

    public long dislike(Integer useid, int type, int commentId) {

        //点彩的集合加上当前的用户
        String disLikeKey=RedisKeyUtil.getDisLikeKey(type,commentId);
        jedisAdapter.sadd(disLikeKey,useid+"");

        //某个人userid，喜欢类型为type，id=commmentid的问题或者回答,1表示问题，2表示回答
        //点赞的集合加一
        String likeQuestionCommentKey= RedisKeyUtil.getLikeKey(type,commentId);
        jedisAdapter.srem(likeQuestionCommentKey,useid+"");//把点赞或者点踩的用户id放到set集合中



        //返回所有的点赞人数
        return jedisAdapter.scard(disLikeKey);
    }

    public long getLikeCount(int type,int id){
        String likeKey=RedisKeyUtil.getLikeKey(type,id);
        long scard = jedisAdapter.scard(likeKey);
        return scard;
    }

    public long getLikeStatus(int userid,int type,int qid){
        String likeKey=RedisKeyUtil.getLikeKey(type,qid);
        if (jedisAdapter.sismember(likeKey,userid+"")){
            //判断当前用户是否在点赞的集合中，如果在的话，返回1。表示喜欢
            return 1;
        }
        //判断是否在点踩的集合中，如果在的话，就返回-1，否则说明。用户不喜欢，也不讨厌。返回0
        String disLikeKey=RedisKeyUtil.getDisLikeKey(type,qid);
        return jedisAdapter.sismember(disLikeKey,userid+"")?-1:0;


    }


}
