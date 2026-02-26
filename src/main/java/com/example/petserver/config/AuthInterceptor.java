package com.example.petserver.config;

import com.example.petserver.annotation.RequireRole;
import com.example.petserver.entity.SysUser;
import com.example.petserver.service.ISysUserService;
import com.example.petserver.utils.JwtUtils;
import io.jsonwebtoken.Claims;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.lang.reflect.Method;

/**
 * 认证与授权拦截器
 * 1. 校验 JWT Token 是否有效
 * 2. 实时校验用户状态（强制下线）
 * 3. 检查 @RequireRole 权限注解
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

	@Autowired
	private ISysUserService sysUserService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// 如果不是映射到方法直接通过（比如静态资源）
		if (!(handler instanceof HandlerMethod)) {
			return true;
		}

		// 1. 获取 Token
		String token = request.getHeader("token");
		if (token == null || token.isEmpty()) {
			return true; // 未携带 Token，放行（交给后续业务逻辑处理未登录情况）
		}

		// 2. 校验 Token 签名与过期
		Claims claims = JwtUtils.getClaims(token);
		if (claims == null) {
			returnError(response, 401, "Token无效或已过期，请重新登录");
			return false;
		}

		String userId = claims.getSubject();
		String role = (String) claims.get("role");

		// 3. 【核心新增】实时查库，校验用户状态 (解决封号不踢出的问题)
		// 虽然增加了数据库查询，但为了安全性是必要的
		try {
			SysUser user = sysUserService.getById(Long.parseLong(userId));
			if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
				returnError(response, 401, "您的账号已被禁用，系统将强制下线");
				return false;
			}
		} catch (Exception e) {
			// 如果 ID 解析错误等，视为无效
			returnError(response, 401, "身份信息异常");
			return false;
		}

		// 4. 将用户信息存入 Request，方便 Controller 使用
		request.setAttribute("currentUserId", Long.parseLong(userId));
		request.setAttribute("currentUserRole", role);

		// 5. 权限校验 (RBAC)
		HandlerMethod handlerMethod = (HandlerMethod) handler;
		Method method = handlerMethod.getMethod();

		// 检查方法上是否有 @RequireRole 注解
		if (method.isAnnotationPresent(RequireRole.class)) {
			RequireRole requireRole = method.getAnnotation(RequireRole.class);
			String requiredRole = requireRole.value();

			// 如果注解要求的角色与当前用户角色不一致，且当前用户不是 ADMIN (管理员拥有所有权限)
			if (!requiredRole.equals(role) && !"ADMIN".equals(role)) {
				returnError(response, 403, "权限不足，拒绝访问");
				return false;
			}
		}

		return true;
	}

	private void returnError(HttpServletResponse response, int code, String msg) throws Exception {
		response.setStatus(code);
		response.setContentType("application/json;charset=utf-8");
		PrintWriter writer = response.getWriter();
		writer.write("{\"code\": " + code + ", \"msg\": \"" + msg + "\"}");
		writer.flush();
		writer.close();
	}
}
