package com.shawn.dubbo.controller.plugin.view;


import com.alibaba.fastjson.support.spring.FastJsonJsonView;
import com.shawn.dubbo.utils.JsonResult;
import com.shawn.dubbo.utils.JsonResultUtils;
import com.shawn.dubbo.utils.SystemConstants;


import java.util.Map;

/**
 * @author <a href="mailto:tangjimo@sf-express.com">709166</a>
 * @since version1.0 
 */
public class CustomizeFastJsonView extends FastJsonJsonView {
	@Override
	protected Object filterModel(Map<String, Object> model) {
		Object value = super.filterModel(model);
		if (value != null && !(value instanceof JsonResult)) {
			value = JsonResultUtils.getJsonResult(value, SystemConstants.RESPONSE_STATUS_SUCCESS, null, null);
		}

		return value;
	}
}
