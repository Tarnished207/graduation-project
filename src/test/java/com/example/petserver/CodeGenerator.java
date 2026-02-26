package com.example.petserver;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.Collections;

public class CodeGenerator {
	public static void main(String[] args) {
		//数据库配置
		FastAutoGenerator.create("jdbc:mysql://localhost:3306/pet_db?serverTimezone=Asia/Shanghai", "root", "123456")
				.globalConfig(builder -> {
					builder.author("TAO") //
							.outputDir(System.getProperty("user.dir") + "/src/main/java")
							.disableOpenDir();
				})
				.packageConfig(builder -> {
					builder.parent("com.example.petserver")
							.pathInfo(Collections.singletonMap(OutputFile.xml, System.getProperty("user.dir") + "/src/main/resources/mapper")); // 设置 Mapper XML 生成路径
				})
				.strategyConfig(builder -> {
					// 设置需要生成的表名
					builder.addInclude("sys_user", "pet_info", "feed_plan", "health_record", "product", "orders", "chat_message")
							.entityBuilder().enableLombok(); // 实体类启用 Lombok (自动生成get/set)
					builder.controllerBuilder().enableRestStyle(); // Controller 启用 REST 风格 (返回JSON)
				})
				.templateEngine(new VelocityTemplateEngine()) // 使用 Velocity 模板引擎
				.execute();
	}
}