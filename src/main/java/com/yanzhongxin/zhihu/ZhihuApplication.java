package com.yanzhongxin.zhihu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yanzhongxin.zhihu.dao") //扫描dao层
public class ZhihuApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZhihuApplication.class, args);
	}

}

