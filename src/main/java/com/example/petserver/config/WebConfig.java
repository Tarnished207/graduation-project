package com.example.petserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Autowired
	private AuthInterceptor authInterceptor;

	@Value("${app.upload-dir}")
	private String uploadDir;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authInterceptor)
				.addPathPatterns("/**")
				.excludePathPatterns("/sysUser/login", "/sysUser/register", "/sysUser/sendEmail", "/sysUser/resetPass", "/images/**", "/error");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 静态资源映射已通过 ImageController 接管，此处配置已失效或仅作为备份
		/*
		String dir = uploadDir;
		if (dir != null && !dir.endsWith("/")) {
			dir = dir + "/";
		}
		
		// 关键修正：使用 toURI() 自动处理路径中的空格和特殊字符
		String location;
		try {
			location = new java.io.File(dir).toURI().toString();
			System.out.println("WebConfig Static Resource Location: " + location);
		} catch (Exception e) {
			// 降级处理
			location = "file:///" + dir;
			System.out.println("WebConfig Static Resource Location (Fallback): " + location);
		}
		
		registry.addResourceHandler("/images/**")
				.addResourceLocations(location);
		*/
	}

	//配置全局跨域 (CORS)
	//允许前端 (Vue) 访问后端接口
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**") // 允许所有路径
				.allowedOriginPatterns("*") // 允许所有来源 (localhost:8080 等)
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的方法
				.allowCredentials(true) // 允许携带 Cookie
				.maxAge(3600); // 预检请求缓存 1 小时
	}

}
