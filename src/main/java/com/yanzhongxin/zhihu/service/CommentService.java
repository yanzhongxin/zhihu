package com.yanzhongxin.zhihu.service;

import com.yanzhongxin.zhihu.dao.CommentMapper;
import com.yanzhongxin.zhihu.model.Comment;
import com.yanzhongxin.zhihu.model.CommentExample;
import com.yanzhongxin.zhihu.model.EntityType;
import com.yanzhongxin.zhihu.util.JedisAdapter;
import com.yanzhongxin.zhihu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import redis.clients.jedis.JedisPool;

import javax.security.auth.login.CredentialException;
import java.util.List;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/21 16:33
 */
@Service
public class CommentService {
    @Autowired
    CommentMapper commentMapper;
    @Autowired
    SensitiveService sensitiveService;
    @Autowired
    JedisAdapter jedisAdapter;
    //添加对用户问题的评论
    public int addComment(Comment comment){
        //敏感词和html成分过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveService.filter(comment.getContent()));

        int i = commentMapper.insertSelective(comment);
        return i>0?1:0;
    }

    //根据评论的类型和合评论的id获得评论的个数

    public  List<Comment>  getCount(int commentType,int  questinoId){
        CommentExample commentExample=new CommentExample();
        CommentExample.Criteria criteria = commentExample.createCriteria();
        criteria.andEntityTypeEqualTo(commentType); //评论类型，1表示对问题的评论。2表示对评论的评论
        criteria.andEntityIdEqualTo(questinoId); //具体的id

        List<Comment> comments = commentMapper.selectByExample(commentExample);

        return comments;
    }

    public Comment getCommentById(int id){
        Comment comment = commentMapper.selectByPrimaryKey(id);
        return comment;
    }

    //根据用户id查询用户一共有多少个回答
    public int commentByUserId(int id){
        CommentExample commentExample=new CommentExample();
        CommentExample.Criteria criteria = commentExample.createCriteria();
        criteria.andUserIdEqualTo(id);
        List<Comment> comments = commentMapper.selectByExample(commentExample);
        return comments.size();
    }

    //获得一个用户评论赞的总个数
    public long getAllZanCommentByUserId(int id){
        CommentExample commentExample=new CommentExample();
        CommentExample.Criteria criteria = commentExample.createCriteria();
        criteria.andUserIdEqualTo(id);
        List<Comment> comments = commentMapper.selectByExample(commentExample);
        long zan=0;
        for (Comment comment:comments){ //用户id的所有的评论
            zan+=jedisAdapter.scard(RedisKeyUtil.getLikeKey(EntityType.Comment,comment.getId()));
        }
        return zan;
    }

    //根据用户id获取他发布的评论
    public List<Comment> getCommentsByUserId(int uid) {
        List<Comment> list= commentMapper.getCommentsByUseId(uid);
        return list;
    }
}
