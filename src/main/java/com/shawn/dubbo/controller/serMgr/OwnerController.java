/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.shawn.dubbo.controller.serMgr;


import com.shawn.dubbo.dao.Owner;
import com.shawn.dubbo.serMgrHelper.Tool;
import com.shawn.dubbo.service.OwnerService;
import com.shawn.dubbo.service.ProviderService;
import com.shawn.dubbo.utils.JsonResult;
import com.shawn.dubbo.utils.JsonResultUtils;
import com.shawn.dubbo.utils.SystemConstants;
import com.shawn.dubbo.utils.SystemErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/owner")
public class OwnerController {

	@Autowired
	private OwnerService ownerService;

	@Autowired
	private ProviderService providerService;


	/**
	 * 获得所有符合条件的负责人
	 */
	@RequestMapping(value = "/findAllOwner",method = RequestMethod.GET)
	@ResponseBody
	public JsonResult<List<Owner>> findAllOwner(String service) {

		JsonResult<List<Owner>> listJsonResult = new JsonResult<List<Owner>>();

		List<Owner> owners;
		if (service != null && service.length() > 0) {
			owners = ownerService.findByService(service);
		} else {
			owners = ownerService.findAll();
		}
		listJsonResult = JsonResultUtils.getJsonResult(owners,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
		return listJsonResult;
	}


	/**
	 * 装载数据，用于新增
	 * @param service
	 * @return
     */
	@RequestMapping(value = "/loadOwner",method = RequestMethod.GET)
	@ResponseBody
	public JsonResult<List<String>> loadOwner(String service) {

		JsonResult jsonResult = new JsonResult();
		if (service == null || service.length() == 0) {
            List<String> serviceList = Tool.sortSimpleName(new ArrayList<String>(providerService.findServices()));
			jsonResult = JsonResultUtils.getJsonResult(serviceList,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
			return jsonResult;
        }
		jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
		return jsonResult;
	}

	/**
	 * 创建所有者
	 * @param owner
	 * @return
     */
	@RequestMapping(value = "/createOwner",method = RequestMethod.POST)
	@ResponseBody
	public JsonResult createOwner(Owner owner) {
		JsonResult jsonResult = new JsonResult();
		String service = owner.getService();
		String username = owner.getUsername();
		if (service == null || service.length() == 0
        		|| username == null || username.length() == 0){
			jsonResult = JsonResultUtils.getJsonResult(null, SystemConstants.RESPONSE_STATUS_FAILURE,
					SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

			return jsonResult;
        }

		ownerService.saveOwner(owner);
		jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
		return jsonResult;
	}

	/**
	 * 删除所有者
	 * @param owner
	 * @return
     */
	@RequestMapping(value = "/deleteOwner",method = RequestMethod.POST)
	@ResponseBody
	public JsonResult deleteOwner(Owner owner) {
		JsonResult jsonResult = new JsonResult();

		String service =  owner.getService();
		String username = owner.getUsername();

		if (service == null || service.length() == 0
        		|| username == null || username.length() == 0){
			jsonResult = JsonResultUtils.getJsonResult(null, SystemConstants.RESPONSE_STATUS_FAILURE,
					SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);
			return jsonResult;
        }

		ownerService.deleteOwner(owner);
		jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
		return jsonResult;
	}

}
