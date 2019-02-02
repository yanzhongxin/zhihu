package com.yanzhongxin.zhihu.dao;

import com.yanzhongxin.zhihu.model.loginTicket;
import com.yanzhongxin.zhihu.model.loginTicketExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface loginTicketMapper {
    int countByExample(loginTicketExample example);

    int deleteByExample(loginTicketExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(loginTicket record);

    int insertSelective(loginTicket record);

    List<loginTicket> selectByExample(loginTicketExample example);

    loginTicket selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") loginTicket record, @Param("example") loginTicketExample example);

    int updateByExample(@Param("record") loginTicket record, @Param("example") loginTicketExample example);

    int updateByPrimaryKeySelective(loginTicket record);

    int updateByPrimaryKey(loginTicket record);
}