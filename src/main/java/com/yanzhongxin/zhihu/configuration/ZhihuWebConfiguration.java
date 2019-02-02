package com.yanzhongxin.zhihu.configuration;

import com.yanzhongxin.zhihu.interceptor.LoginIntecepter;
import com.yanzhongxin.zhihu.interceptor.PassportIntecepter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author www.yanzhongxin.com,拦截器配置文件
 * @date 2019/1/21 10:56
 */
@Component
public class ZhihuWebConfiguration extends WebMvcConfigurerAdapter {
    @Autowired
    PassportIntecepter passportIntecepter;

    @Autowired
    LoginIntecepter loginIntecepter;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passportIntecepter);
        registry.addInterceptor(loginIntecepter).addPathPatterns("/user/*");
        super.addInterceptors(registry);
    }
}
