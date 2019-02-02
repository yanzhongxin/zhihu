package com.yanzhongxin.zhihu.async;

/**
 * Created by nowcoder on 2016/7/30.
 */
public enum EventType {
    LIKE(0),
    COMMENT(1),
    LOGIN(2),
    MAIL(3),
    FOLLOW(4),
    UNFOLLOW(5),
    ADD_QUESTION(6),
    ANSWER_QUESTION(7), //粉丝回答明星的问题
    FOLLOW_USER(8); //关注用户
   // PERSONAL_TIMELINE(8);//个人时间轴信息

    private int value;
    EventType(int value) { this.value = value; }
    public int getValue() { return value; }
}
