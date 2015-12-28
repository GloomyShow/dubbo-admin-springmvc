package com.shawn.dubbo.utils;


import java.util.Date;


public final class JsonResultUtils {

	public static <T> JsonResult<T> getJsonResult(T data, String code, String errorCode, String message) {
		JsonResult<T> result = new JsonResult<T>();
		result.setCode(code);
		result.setErrorCode(errorCode);
		result.setData(data);
		result.setMessage(message);
		result.setTimestamp(DateUtils.DateToStr(new Date()));
		
		return result;
	}
}
