package com.example.petserver.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtils {

	// 密钥，生产环境应放在配置文件中
	private static final String SECRET_KEY = "PetPlanetSecretKey2025";
	// 过期时间：24小时
	private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;

	/**
	 * 生成 Token
	 */
	public static String createToken(Long userId, String role) {
		return Jwts.builder()
				.setSubject(userId.toString()) // 主题存 userId
				.claim("role", role)           // 存角色
				.setIssuedAt(new Date())       // 签发时间
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME)) // 过期时间
				.signWith(SignatureAlgorithm.HS256, SECRET_KEY) // 签名
				.compact();
	}

	/**
	 * 解析 Token
	 */
	public static Claims getClaims(String token) {
		try {
			return Jwts.parser()
					.setSigningKey(SECRET_KEY)
					.parseClaimsJws(token)
					.getBody();
		} catch (Exception e) {
			return null; // 解析失败（过期或篡改）
		}
	}

	/**
	 * 校验 Token 是否有效
	 */
	public static boolean validateToken(String token) {
		return getClaims(token) != null;
	}
}
