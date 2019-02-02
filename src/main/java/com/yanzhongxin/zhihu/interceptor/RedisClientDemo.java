package com.yanzhongxin.zhihu.interceptor;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/22 9:58
 */
public class RedisClientDemo {
    public static void main(String[] args) {
        JedisPool jedisPool=new JedisPool("192.168.25.128",6379);



    }
}
