/*
 * @Description: 全局统一返回对象
 * @Version: 2.0
 * @Autor: fan
 * @Date: 2021-02-18 13:13:04
 * @LastEditors: fan
 * @LastEditTime: 2021-04-27 18:07:41
 */
package com.example.gaokaoproject.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "统一响应类")
public class Result<T> {
	/***
	 * 默认返回的错误码成功为200，失败为500
	 */
	private int code;
	/***
	 * 错误消息描述，可自定义
	 */
	private String msg;
	/***
	 * 返回的数据
	 */
	private T data;

	public Result(int code, String msg, T data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
	}

	public Result(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public static <T> Result<T> ok(T data) {
		return new Result<>(200, "ok", data);
	}

	public static <T> Result<T> ok() {
		return new Result<>(200, "ok");
	}

	public static <T> Result<T> error(int code, String msg) {
		return new Result<T>(code, msg);
	}

	public static <T> Result<T> error(String msg) {
		return new Result<T>(500, msg);
	}
}