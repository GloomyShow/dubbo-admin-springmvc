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
import com.alibaba.dubbo.common.utils.StringUtils;

import com.shawn.dubbo.common.route.OverrideUtils;
import com.shawn.dubbo.dao.User;
import com.shawn.dubbo.service.ConsumerService;
import com.shawn.dubbo.service.OverrideService;
import com.shawn.dubbo.service.ProviderService;
import com.shawn.dubbo.dao.Override;
import com.shawn.dubbo.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/application")
public class ApplicationController {
    
    @Autowired
    private ProviderService providerService;
    
    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private OverrideService overrideService;


    @RequestMapping(value="/findAllApplication",method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<Map<String,Object>> findAllApplication(String service, String application) {
    	JsonResult<Map<String,Object>> mapJsonResult = new JsonResult<Map<String, Object>>();
        Map<String,Object> resultMap = new HashMap<String, Object>();

    	if (!StringUtils.isBlank(service)) {
    		Set<String> applications = new TreeSet<String>();
            List<String> providerApplications = providerService.findApplicationsByServiceName(service);
            if (providerApplications != null && providerApplications.size() > 0) {
                applications.addAll(providerApplications);
            }
            List<String> consumerApplications = consumerService.findApplicationsByServiceName(service);
            if (consumerApplications != null && consumerApplications.size() > 0) {
                applications.addAll(consumerApplications);
            }
            resultMap.put("applications", applications);
            resultMap.put("providerApplications", providerApplications);
            resultMap.put("consumerApplications", consumerApplications);
            if (service != null && service.length() > 0) {
            	List<Override> overrides = overrideService.findByService(service);
            	Map<String, List<Override>> application2Overrides = new HashMap<String, List<Override>>();
                if (overrides != null && overrides.size() > 0 
                        && applications != null && applications.size() > 0) {
                    for (String a : applications) {
                        if (overrides != null && overrides.size() > 0) {
                        	List<Override> appOverrides = new ArrayList<Override>();
                            for (Override override : overrides) {
                                if (override.isMatch(service, null, a)) {
                                	appOverrides.add(override);
                                }
                            }
                            Collections.sort(appOverrides, OverrideUtils.OVERRIDE_COMPARATOR);
                            application2Overrides.put(a, appOverrides);
                        }
                    }
                }
                resultMap.put("overrides", application2Overrides);
            }
            mapJsonResult = JsonResultUtils.getJsonResult(resultMap, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
            return mapJsonResult;
    	}

        Set<String> applications = new TreeSet<String>();
        List<String> providerApplications = providerService.findApplications();
        if (providerApplications != null && providerApplications.size() > 0) {
            applications.addAll(providerApplications);
        }
        List<String> consumerApplications = consumerService.findApplications();
        if (consumerApplications != null && consumerApplications.size() > 0) {
            applications.addAll(consumerApplications);
        }
        
        Set<String> newList = new HashSet<String>();
        Set<String> newProviders = new HashSet<String>();
        Set<String> newConsumers = new HashSet<String>();
        resultMap.put("applications", applications);
        resultMap.put("providerApplications", providerApplications);
        resultMap.put("consumerApplications", consumerApplications);
        

        if (StringUtils.isNotEmpty(application)) {
            application = application.toLowerCase();
            for (String o : applications) {
                if (o.toLowerCase().indexOf(application) != -1) {
                    newList.add(o);
                }
            }
            for (String o : providerApplications) {
                if (o.toLowerCase().indexOf(application) != -1) {
                    newProviders.add(o);
                }
            }
            for (String o : consumerApplications) {
                if (o.toLowerCase().indexOf(application) != -1) {
                    newConsumers.add(o);
                }
            }
            resultMap.put("applications", newList);
            resultMap.put("providerApplications", newProviders);
            resultMap.put("consumerApplications", newConsumers);
        }
        mapJsonResult = JsonResultUtils.getJsonResult(resultMap, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return mapJsonResult;
    }


    /**
     * 屏蔽
     * @param service
     * @param applications
     * @param request
     * @return
     */
    @RequestMapping(value="/shieldApplication",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult shieldApplication(String service, String applications, HttpServletRequest request) {
    	return mock( service, applications, request, "force:return null");
    }

    /**
     * 容错
     * @param service
     * @param applications
     * @param request
     * @return
     */
    @RequestMapping(value="/tolerantApplication",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult tolerantApplication(String service, String applications, HttpServletRequest request){
    	return mock(service, applications, request, "fail:return null");
    }

    /**
     * 恢复
     * @param service
     * @param applications
     * @param request
     * @return
     */
    @RequestMapping(value="/recoverApplication",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult recoverApplication(String service, String applications, HttpServletRequest request) {
    	return mock(service, applications, request, "");
    }

    /**
     * 屏蔽，容错，恢复共同类
     * @param service
     * @param applications
     * @param request
     * @param mock
     * @return
     */
    private JsonResult mock(String service, String applications, HttpServletRequest request, String mock){

        JsonResult jsonResult = new JsonResult();
        /*User currentUser = (User)request.getSession().getAttribute(Constants.CURRENT_USER);

        String operator = currentUser.getUsername();*/
        String operator = null;
        String operatorAddress = NetUtils.getLocalHost();


        if (service == null || service.length() == 0
        		|| applications == null || applications.length() == 0){
            jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

            return jsonResult;
        }

        for (String application : Constants.SPACE_SPLIT_PATTERN.split(applications)) {
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

    /**
     * 缺省屏蔽
     * @param service
     * @param applications
     * @param request
     * @return
     */
    @RequestMapping(value="/allshieldApplication",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult allshieldApplication(String service, String applications, HttpServletRequest request) {
    	return allmock(service, applications, request, "force:return null");
    }

    /**
     * 缺省容错
     * @param service
     * @param applications
     * @param request
     * @return
     */
    @RequestMapping(value="/alltolerantApplication",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult alltolerantApplication(String service, String applications, HttpServletRequest request) {
    	return allmock(service, applications, request, "fail:return null");
    }

    /**
     * 缺省恢复
     * @param service
     * @param applications
     * @param request
     * @return
     */
    @RequestMapping(value="/allrecoverApplication",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult allrecoverApplication(String service, String applications, HttpServletRequest request) {
    	return allmock(service, applications, request, "");
    }
    
    private JsonResult allmock(String service, String applications, HttpServletRequest request, String mock) {

        JsonResult jsonResult = new JsonResult();
        /*User currentUser = (User)request.getSession().getAttribute(Constants.CURRENT_USER);

        String operator = currentUser.getUsername();*/
        String operator = null;
        String operatorAddress = NetUtils.getLocalHost();

        if (service == null || service.length() == 0) {
            jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

            return jsonResult;
        }

        List<Override> overrides = overrideService.findByService(service);
        Override allOverride = null;
        if (overrides != null && overrides.size() > 0) {
            for (Override override: overrides) {
            	if (override.isDefault()) {
            		allOverride = override;
            		break;
            	}
            }
        }
        if (allOverride != null) {
        	Map<String, String> map = StringUtils.parseQueryString(allOverride.getParams());
            if (mock == null || mock.length() == 0) {
                map.remove("mock");
            } else {
                map.put("mock", URL.encode(mock));
            }
            if (map.size() > 0) {
            	allOverride.setParams(StringUtils.toQueryString(map));
            	allOverride.setEnabled(true);
            	allOverride.setOperator(operator);
            	allOverride.setOperatorAddress(operatorAddress);
                overrideService.updateOverride(allOverride);
            } else {
            	overrideService.deleteOverride(allOverride.getId());
            }
        } else if (mock != null && mock.length() > 0) {
            Override override = new Override();
            override.setService(service);
            override.setParams("mock=" + URL.encode(mock));
            override.setEnabled(true);
            override.setOperator(operator);
            override.setOperatorAddress(operatorAddress);
            overrideService.saveOverride(override);
        }

        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;
    }

}
