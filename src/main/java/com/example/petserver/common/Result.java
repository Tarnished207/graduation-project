package com.example.petserver.common;

import java.io.Serializable;

/**
 * 统一返回结果类
 * 作用：让所有接口都返回统一的格式 { code: "200", msg: "...", data: ... }
 */
public class Result<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	private String code; // 状态码
	private String msg;  // 提示信息
	private T data;      // 数据

	// 成功（无数据）
	public static <T> Result<T> success() {
		Result<T> result = new Result<>();
		result.setCode("200");
		result.setMsg("操作成功");
		return result;
	}

	// 成功（带数据）
	public static <T> Result<T> success(T data) {
		Result<T> result = new Result<>();
		result.setCode("200");
		result.setMsg("操作成功");
		result.setData(data);
		return result;
	}

	// 失败
	public static <T> Result<T> error(String code, String msg) {
		Result<T> result = new Result<>();
		result.setCode(code);
		result.setMsg(msg);
		return result;
	}

	// Getters & Setters
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}