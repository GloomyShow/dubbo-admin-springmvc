package com.shawn.dubbo.utils;

/**
 * 统一返回json的结果封装类
 * 
 * @author <a href="mailto:tangjimo@sf-express.com">709166</a>
 * @since version1.0 
 */
public class JsonResult<T> {
	private String code;
	
	private String errorCode;
	
	private String message;
	
	private String timestamp;
	
	private T data;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
	
}
