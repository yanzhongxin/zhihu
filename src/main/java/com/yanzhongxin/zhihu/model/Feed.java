package com.yanzhongxin.zhihu.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Date;

public class Feed {
    private Integer id;//数据库的id

    private Date createdDate; //feed流创建时间

    private Integer userId; //产生feed的用户id

    private String data; //产生feed的时间

    private Integer type; //feed流的类型

    private JSONObject jsonObject;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data == null ? null : data.trim();
        jsonObject= JSONObject.parseObject(data);
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String get(String key){
        return  jsonObject==null?null:jsonObject.getString(key);
    }
}