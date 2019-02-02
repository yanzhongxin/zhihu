package com.yanzhongxin.zhihu.dao;

import com.yanzhongxin.zhihu.model.Feed;
import com.yanzhongxin.zhihu.model.FeedExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FeedMapper {
    int countByExample(FeedExample example);

    int deleteByExample(FeedExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Feed record);

    int insertSelective(Feed record);

    List<Feed> selectByExample(FeedExample example);

    Feed selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Feed record, @Param("example") FeedExample example);

    int updateByExample(@Param("record") Feed record, @Param("example") FeedExample example);

    int updateByPrimaryKeySelective(Feed record);

    int updateByPrimaryKey(Feed record);

    //比如用户id=1（张三） 需要查找他关注的用户List<ids> 最近发生的新鲜事，
    List<Feed> queryUserFeeds(@Param("maxId") int maxId,@Param("ids")List<Integer> ids,
                              @Param("count") int count);
}