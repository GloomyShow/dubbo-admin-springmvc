package com.shawn.dubbo.controller.plugin.messageconverter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

import com.shawn.dubbo.utils.JsonResult;
import com.shawn.dubbo.utils.JsonResultUtils;
import com.shawn.dubbo.utils.SystemConstants;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 返回统一的json格式
 * @see JsonResult
 * 
 * @author <a href="mailto:tangjimo@sf-express.com">709166</a>
 * @since version1.0
 */
public class CustomizeFastJsonHttpMessageConverter extends FastJsonHttpMessageConverter {
	@Override
	protected void writeInternal(Object obj, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
		Object value = obj;
		if (obj != null && !(obj instanceof JsonResult)) {
			value = JsonResultUtils.getJsonResult(obj, SystemConstants.RESPONSE_STATUS_SUCCESS, null, null);
		}
		
		OutputStream out = outputMessage.getBody();
		String text = JSON.toJSONString(value, getFeatures());
		byte[] bytes = text.getBytes(getCharset());
		out.write(bytes);
	}
}
