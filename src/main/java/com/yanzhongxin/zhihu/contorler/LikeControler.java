package com.yanzhongxin.zhihu.contorler;

import com.yanzhongxin.zhihu.async.EventModel;
import com.yanzhongxin.zhihu.async.EventProducer;
import com.yanzhongxin.zhihu.async.EventType;
import com.yanzhongxin.zhihu.model.Comment;
import com.yanzhongxin.zhihu.model.HostHolder;
import com.yanzhongxin.zhihu.service.CommentService;
import com.yanzhongxin.zhihu.service.LikeService;
import com.yanzhongxin.zhihu.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/22 10:47
 */
@Controller
public class LikeControler {

    @Autowired
    LikeService likeService;
    @Autowired
    HostHolder hostHolder;
    @Autowired
    CommentService commentService;

    @Autowired
    EventProducer eventProducer;
    @RequestMapping(path = {"/like"}, method = {RequestMethod.POST})
    @ResponseBody
    public String like(@RequestParam("commentId") int commentId) {
        if (hostHolder.getUser()==null){
            return WendaUtil.getJSONString(99,"请先登录");//
        }

        Comment commentById = commentService.getCommentById(commentId);

        //异步开始发送信息
        eventProducer.fireEvent(new EventModel(EventType.LIKE).setActorId(hostHolder.getUser().getId())
        .setEntityType(2).setEntityId(commentId).setEntityOwnerId(commentById.getUserId())
        .setExt("questionId",commentById.getEntityId()+""));


        long likeNum=likeService.like(hostHolder.getUser().getId(),2,commentId);//喜欢的类型，1表示问题喜欢，二表示对评论喜欢

        return WendaUtil.getJSONString(0,likeNum+"");//返回问答喜欢的个数
    }

    @RequestMapping(path = {"/dislike"}, method = {RequestMethod.POST})
    @ResponseBody
    public String dislike(@RequestParam("commentId") int commentId) {
        if (hostHolder.getUser()==null){
            return WendaUtil.getJSONString(99);//
        }
        long dislike=likeService.dislike(hostHolder.getUser().getId(),2,commentId);//喜欢的类型，1表示问题喜欢，二表示对评论喜欢

        return WendaUtil.getJSONString(0,dislike+"");//返回问答喜欢的个数
    }
}
