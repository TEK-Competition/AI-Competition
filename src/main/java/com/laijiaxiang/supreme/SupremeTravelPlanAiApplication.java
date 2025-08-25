package com.laijiaxiang.supreme;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan(basePackages = "com.laijiaxiang.supreme.mapper")
public class SupremeTravelPlanAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupremeTravelPlanAiApplication.class, args);
	}

}
