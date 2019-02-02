package com.yanzhongxin.zhihu.async.handler;

import com.alibaba.fastjson.JSONObject;
import com.yanzhongxin.zhihu.async.EventHandler;
import com.yanzhongxin.zhihu.async.EventModel;
import com.yanzhongxin.zhihu.async.EventType;
import com.yanzhongxin.zhihu.model.EntityType;
import com.yanzhongxin.zhihu.model.Feed;
import com.yanzhongxin.zhihu.model.Question;
import com.yanzhongxin.zhihu.model.User;
import com.yanzhongxin.zhihu.service.*;
import com.yanzhongxin.zhihu.util.JedisAdapter;
import com.yanzhongxin.zhihu.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;



/* @Author Zhongxin Yan
 * @Description 这个处理器的主要功能是，用户添加问题后，自动把这问题放到索引库中
 *
 * @Date 2019/2/2 20:41
 * @Param
 * @return
 */
@Component
public class AddQuestionHandler implements EventHandler {


    @Autowired
    SearchService searchService;
    @Override
    public void doHandle(EventModel model) {
        searchService.addQuestionToIndexDB(model.getEntityId(),
                model.getExt("title"),model.getExt("content"));//把问题标题内容同步到索引库
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.ADD_QUESTION);
    }
}
