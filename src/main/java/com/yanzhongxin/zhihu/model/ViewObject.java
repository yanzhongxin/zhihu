package com.yanzhongxin.zhihu.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author www.yanzhongxin.com
 * @date 2019/1/20 19:59
 */
public class ViewObject {
    private Map<String, Object> objs = new HashMap<String, Object>();
    public void set(String key, Object value) {
        objs.put(key, value);
    }

    public Object get(String key) {
        return objs.get(key);
    }
}
