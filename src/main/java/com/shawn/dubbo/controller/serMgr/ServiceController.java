/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.shawn.dubbo.controller.serMgr;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.shawn.dubbo.common.route.OverrideUtils;
import com.shawn.dubbo.dao.User;
import com.shawn.dubbo.serMgrHelper.Tool;
import com.shawn.dubbo.service.ConsumerService;
import com.shawn.dubbo.service.OverrideService;
import com.shawn.dubbo.service.ProviderService;
import com.shawn.dubbo.dao.Override;
import com.shawn.dubbo.utils.*;
import com.alibaba.dubbo.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Providers. URI: /services/$service/providers /addresses/$address/services /application/$application/services
 * 
 * @author ding.lid
 */
@Controller
@RequestMapping("/service")
public class ServiceController {

    @Autowired
    private ProviderService providerService;
    
    @Autowired
    private ConsumerService consumerService;
    
    @Autowired
    private OverrideService overrideService;


    /**
     * 获得所有符合条件的服务
     * 入参为搜索条件
     * @param application 应用
     * @param address 地址（IP）
     * @param service 服务
     * @return
     */
    @RequestMapping(value="/findAllService", method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<Map<String,Object>> findAllService(String application,String address,String service) {

        JsonResult<Map<String,Object>> mapJsonResult = new JsonResult<Map<String, Object>>();
        Map<String,Object> resultMap = new HashMap<String, Object>();
        
        List<String> providerServices = null;
        List<String> consumerServices = null;
        List<Override> overrides = null;
        if (StringUtils.isNotEmpty(application)) {
            providerServices = providerService.findServicesByApplication(application);
            consumerServices = consumerService.findServicesByApplication(application);
            overrides = overrideService.findByApplication(application);
        } else if (StringUtils.isNotEmpty(address)) {
            providerServices = providerService.findServicesByAddress(address);
            consumerServices = consumerService.findServicesByAddress(address);
            overrides = overrideService.findByAddress(Tool.getIP(address));
        } else {
            providerServices = providerService.findServices();
            consumerServices = consumerService.findServices();
            overrides = overrideService.findAll();
        }
        
        Set<String> services = new TreeSet<String>();
        if (providerServices != null) {
            services.addAll(providerServices);
        }
        if (consumerServices != null) {
            services.addAll(consumerServices);
        }
        
        Map<String, List<Override>> service2Overrides = new HashMap<String, List<Override>>();
        if (overrides != null && overrides.size() > 0 
                && services != null && services.size() > 0) {
            for (String s : services) {
                if (overrides != null && overrides.size() > 0) {
                    for (Override override : overrides) {
                    	List<Override> serOverrides = new ArrayList<Override>();
                    	if (override.isMatch(s, address, application)) {
                        	serOverrides.add(override);
                        }
                        Collections.sort(serOverrides, OverrideUtils.OVERRIDE_COMPARATOR);
                        service2Overrides.put(s, serOverrides);
                    }
                }
            }
        }
        
        resultMap.put("providerServices", providerServices);
        resultMap.put("consumerServices", consumerServices);
        resultMap.put("services", services);
        resultMap.put("overrides", service2Overrides);
        

        if (StringUtils.isNotEmpty(service)) {
            service = service.toLowerCase();
            Set<String> newList = new HashSet<String>();
            Set<String> newProviders = new HashSet<String>();
            Set<String> newConsumers = new HashSet<String>();
            
            for (String o : services) {
                if (o.toLowerCase().toLowerCase().indexOf(service) != -1) {
                    newList.add(o);
                }
            }
            for (String o : providerServices) {
                if (o.toLowerCase().indexOf(service) != -1) {
                    newProviders.add(o);
                }
            }
            for (String o : consumerServices) {
                if (o.toLowerCase().indexOf(service) != -1) {
                    newConsumers.add(o);
                }
            }
            resultMap.put("services", newList);
            resultMap.put("providerServices", newProviders);
            resultMap.put("consumerServices", newConsumers);
        }

        mapJsonResult = JsonResultUtils.getJsonResult(resultMap, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return mapJsonResult;
    }

    /**
     * 屏蔽
     * @param services
     * @param application
     * @param request
     * @return
     */
    @RequestMapping(value="/shieldService", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult shieldService(String services, String application, HttpServletRequest request) {
    	return mock( services, application, request, "force:return null");
    }

    /**
     * 容错
     * @param services
     * @param application
     * @param request
     * @return
     */
    @RequestMapping(value="/tolerantService", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult tolerantService(String services, String application, HttpServletRequest request) {
    	return mock(services, application, request, "fail:return null");
    }

    /**
     * 恢复
     * @param services
     * @param application
     * @param request
     * @return
     */
    @RequestMapping(value="/recoverService", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult recoverService(String services, String application, HttpServletRequest request) {
    	return mock(services, application, request, "");
    }

    /**
     * 屏蔽，容错，恢复共同类
     * @param services
     * @param application
     * @param request
     * @param mock
     * @return
     * @throws Exception
     */
    private JsonResult mock(String services, String application, HttpServletRequest request, String mock)  {

        JsonResult jsonResult = new JsonResult();
       User currentUser = (User)request.getSession().getAttribute(Constants.CURRENT_USER);

        String operator = currentUser.getUsername();
       // String operator = null;
        String operatorAddress = NetUtils.getLocalHost();

        if (services == null || services.length() == 0
        		|| application == null || application.length() == 0){
            jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

            return jsonResult;
        }

        for (String service : Constants.SPACE_SPLIT_PATTERN.split(services)) {
	        List<Override> overrides = overrideService.findByServiceAndApplication(service, application);
	        if (overrides != null && overrides.size() > 0) {
	            for (Override override: overrides) {
	                Map<String, String> map = StringUtils.parseQueryString(override.getParams());
	                if (mock == null || mock.length() == 0) {
	                    map.remove("mock");
	                } else {
	                    map.put("mock", URL.encode(mock));
	                }
	                if (map.size() > 0) {
	                	override.setParams(StringUtils.toQueryString(map));
	                    override.setEnabled(true);
	                    override.setOperator(operator);
	                    override.setOperatorAddress(operatorAddress);
	                    overrideService.updateOverride(override);
	                } else {
	                	overrideService.deleteOverride(override.getId());
	                }
	            }
	        } else if (mock != null && mock.length() > 0) {
	            Override override = new Override();
	            override.setService(service);
	            override.setApplication(application);
	            override.setParams("mock=" + URL.encode(mock));
	            override.setEnabled(true);
	            override.setOperator(operator);
	            override.setOperatorAddress(operatorAddress);
	            overrideService.saveOverride(override);
	        }
        }
        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;
    }

}
