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
import com.shawn.dubbo.common.route.RouteRule;
import com.shawn.dubbo.common.route.RouteRule.MatchPair;
import com.shawn.dubbo.common.route.RouteUtils;
import com.shawn.dubbo.dao.*;
import com.shawn.dubbo.dao.Override;
import com.shawn.dubbo.serMgrHelper.Tool;
import com.shawn.dubbo.service.ConsumerService;
import com.shawn.dubbo.service.OverrideService;
import com.shawn.dubbo.service.ProviderService;
import com.shawn.dubbo.service.RouteService;
import com.shawn.dubbo.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by 594829 on 2015/12/28.
 */
@Controller
@RequestMapping("/consumer")
public class ConsumerController {

    @Autowired
    private ProviderService providerService;
    
    @Autowired
    private ConsumerService consumerService;
    
    @Autowired
    private OverrideService overrideService;
    
    @Autowired
    private RouteService routeService;


    /**
     * 查找消费者，查找条件 service，application，Address
     * TODO 暂时不做分页，看前台如何处理搜索条件。
     * @param consumer
     * @return
     */
    @RequestMapping(value = "/findConsumer", method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<List<Consumer>> findConsumer(Consumer consumer) {
        JsonResult<List<Consumer>> jsonResult = new JsonResult<List<Consumer>>();
        Page<Consumer> pageConsumer = new Page<Consumer>();


        String service = consumer.getService();
        String application = consumer.getApplication();
        String address =  consumer.getAddress();
        List<Consumer> consumers;
        List<Override> overrides;
        List<Provider> providers = null;
    	List<Route> routes = null;
        // service
        if (service != null && service.length() > 0) {
            consumers = consumerService.findByService(service);
            overrides = overrideService.findByService(service);
            providers = providerService.findByService(service);
        	routes = routeService.findByService(service);
        }
        // address
        else if (address != null && address.length() > 0) {
            consumers = consumerService.findByAddress(address);
            overrides = overrideService.findByAddress(Tool.getIP(address));
        }
        // application
        else if (application != null && application.length() > 0) {
            consumers = consumerService.findByApplication(application);
            overrides = overrideService.findByApplication(application);
        }
        // all
        else {
            consumers = consumerService.findAll();
            overrides = overrideService.findAll();
        }
        if (consumers != null && consumers.size() > 0) {
            for (Consumer tconsumer : consumers) {
            	if (service == null || service.length() == 0) {
            		providers = providerService.findByService(tconsumer.getService());
            		routes = routeService.findByService(tconsumer.getService());
            	}
                List<Route> routed = new ArrayList<Route>();
                tconsumer.setProviders(RouteUtils.route(tconsumer.getService(), tconsumer.getAddress(), tconsumer.getParameters(), providers, overrides, routes, null, routed));
            	tconsumer.setRoutes(routed);
            	OverrideUtils.setConsumerOverrides(tconsumer, overrides);
            }
        }

        //分页查找
       /* List<Consumer> pageConsumers = new ArrayList<Consumer>();
        int pageSize = consumer.getPageSize();
        int curretPage = consumer.getCurrentPage();
        int begin = pageSize*(curretPage-1);
        int end = pageSize*curretPage;
        if(providers.size()>0){
            for(int i=begin;i<end;i++){
                pageConsumers.add(consumers.get(i));
            }
        }

        pageConsumer.setDatas(pageConsumers);
        pageConsumer.setTotalRecord(consumers.size());
        pageConsumer.setCurrentPage(curretPage);
        pageConsumer.setPageSize(pageSize);*/

        jsonResult = JsonResultUtils.getJsonResult(consumers, SystemConstants.RESPONSE_STATUS_SUCCESS,
                null, SystemConstants.RESPONSE_MESSAGE_SUCCESS);

        return jsonResult;
    }


    /**
     * 通过id获取消费者具体消息
     * @param id
     * @return 消费者，提供者，路由，
     */
    @RequestMapping(value="/findConsumerById",method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<Map<String,Object>>  findConsumerById(Long id) {
        JsonResult<Map<String,Object>> mapJsonResult = new JsonResult<Map<String, Object>>();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Consumer consumer = consumerService.findConsumer(id);
        List<Provider> providers = providerService.findByService(consumer.getService());
        List<Route> routes = routeService.findByService(consumer.getService());
        List<Override> overrides = overrideService.findByService(consumer.getService());
        List<Route> routed = new ArrayList<Route>();
        consumer.setProviders(RouteUtils.route(consumer.getService(), consumer.getAddress(), consumer.getParameters(), providers, overrides, routes, null, routed));
        consumer.setRoutes(routed);
        OverrideUtils.setConsumerOverrides(consumer, overrides);
        resultMap.put("consumer", consumer);
        resultMap.put("providers", consumer.getProviders());
        resultMap.put("routes", consumer.getRoutes());
        resultMap.put("overrides", consumer.getOverrides());
        mapJsonResult = JsonResultUtils.getJsonResult(resultMap,SystemConstants.RESPONSE_STATUS_SUCCESS,
                null, SystemConstants.RESPONSE_MESSAGE_SUCCESS);

        return mapJsonResult;
    }

    /**
     * 修改消费者
     * @param newConsumer
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value="/updateConsumer",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateConsumer(Consumer newConsumer, HttpServletResponse response, HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        User currentUser =(User) request.getSession().getAttribute(Constants.CURRENT_USER);
        String operator = currentUser.getUsername();
        String operatorAddress = NetUtils.getLocalHost();

    	Long id = newConsumer.getId();
    	String parameters = newConsumer.getParameters();
    	Consumer consumer = consumerService.findConsumer(id);
		if (consumer == null) {
            jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

            return jsonResult;
		}
        String service = consumer.getService();
        if (!currentUser.hasServicePrivilege(service)) {
            jsonResult = JsonResultUtils.getJsonResult(service,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR,SystemConstants.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR);

            return jsonResult;
        }
        Map<String, String> oldMap = StringUtils.parseQueryString(consumer.getParameters());
        Map<String, String> newMap = StringUtils.parseQueryString(parameters);
        for (Map.Entry<String, String> entry : oldMap.entrySet()) {
        	if (entry.getValue().equals(newMap.get(entry.getKey()))) {
        		newMap.remove(entry.getKey());
        	}
        }
        String address = consumer.getAddress();
        List<Override> overrides = overrideService.findByServiceAndAddress(consumer.getService(), consumer.getAddress());
        OverrideUtils.setConsumerOverrides(consumer, overrides);
        Override override = consumer.getOverride();
        if (override != null) {
            if (newMap.size() > 0) {
            	override.setParams(StringUtils.toQueryString(newMap));
                override.setEnabled(true);
                override.setOperator(operator);
                override.setOperatorAddress(operatorAddress);
                overrideService.updateOverride(override);
            } else {
            	overrideService.deleteOverride(override.getId());
            }
        } else {
            override = new Override();
            override.setService(service);
            override.setAddress(address);
            override.setParams(StringUtils.toQueryString(newMap));
            override.setEnabled(true);
            override.setOperator(operator);
            override.setOperatorAddress(operatorAddress);
            overrideService.saveOverride(override);
        }
        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,
                null, SystemConstants.RESPONSE_MESSAGE_SUCCESS);

        return jsonResult;
    }


    /**
     * 批量屏蔽
     * @param ids 消费者ID
     * @param response
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/shiedConsumer",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult shiedConsumer(Long[] ids, HttpServletResponse response, HttpServletRequest request) throws Exception {
    	return mock(ids, response,request,"force:return null");
    }

    /**
     * 批量容错
     * @param ids
     * @param response
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/tolerantConsumer",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult tolerantConsumer(Long[] ids, HttpServletResponse response, HttpServletRequest request) throws Exception {
    	return mock(ids, response,request, "fail:return null");
    }

    /**
     * 批量恢复
     * @param ids
     * @param response
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/recoverConsumer",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult recoverConsumer(Long[] ids, HttpServletResponse response, HttpServletRequest request) throws Exception {
    	return mock(ids, response,request, "");
    }


    /**
     * 屏蔽，容错，恢复操作等共同类
     * @param ids
     * @param response
     * @param request
     * @param mock
     * @return
     */
    private JsonResult mock(Long[] ids,  HttpServletResponse response, HttpServletRequest request, String mock){

        JsonResult jsonResult = new JsonResult();
        User currentUser = (User)request.getSession().getAttribute(Constants.CURRENT_USER);
        String operator = currentUser.getUsername();
        String operatorAddress = NetUtils.getLocalHost();
        if (ids == null || ids.length == 0){
            jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

            return jsonResult;
        }
        List<Consumer> consumers = new ArrayList<Consumer>();
        for (Long id : ids) {
            Consumer c = consumerService.findConsumer(id);
            if(c != null){
                consumers.add(c);
                if (!currentUser.hasServicePrivilege(c.getService())) {
                    jsonResult = JsonResultUtils.getJsonResult(c.getService(),SystemConstants.RESPONSE_STATUS_FAILURE,
                            SystemErrorCode.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR,SystemConstants.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR);

                    return jsonResult;
                }
            }
        }
        for(Consumer consumer : consumers) {
            String service = consumer.getService();
            String address = Tool.getIP(consumer.getAddress());
            List<Override> overrides = overrideService.findByServiceAndAddress(service, address);
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
                override.setAddress(address);
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
     * @param response
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/allshieldConsumer",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult allshieldConsumer( String service,HttpServletResponse response, HttpServletRequest request) throws Exception {
    	return allmock(service,  response, request, "force:return null");
    }

    /**
     * 缺省容忍
     * @param service
     * @param response
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/alltoleranConsumer",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult alltoleranConsumer(String service, HttpServletResponse response, HttpServletRequest request) throws Exception {
    	return allmock(service, response, request, "fail:return null");
    }

    /**
     * 缺省恢复
     * @param service
     * @param response
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/allrecoverConsumer",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult allrecoverConsumer(String service, HttpServletResponse response, HttpServletRequest request) throws Exception {
    	return allmock(service,  response, request, "");
    }

    /**
     * 缺省屏蔽，容错，恢复操作等共同类
     * @param service
     * @param response
     * @param request
     * @param mock
     * @return
     * @throws Exception
     */
    private JsonResult allmock(String service,HttpServletResponse response, HttpServletRequest request, String mock) throws Exception {
        JsonResult jsonResult = new JsonResult();
        User currentUser = (User)request.getSession().getAttribute(Constants.CURRENT_USER);
        String operator = currentUser.getUsername();
        String operatorAddress = NetUtils.getLocalHost();
    	/*String service = (String) context.get("service");*/
        if (service == null || service.length() == 0) {
            jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

            return jsonResult;
        }
        if (! currentUser.hasServicePrivilege(service)) {
            jsonResult = JsonResultUtils.getJsonResult(service,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR,SystemConstants.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR);

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


    /**
     * 批量允许
     * @param ids
     * @param response
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/allowConsumer",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult allowConsumer(Long[] ids, HttpServletResponse response, HttpServletRequest request) throws Exception {
    	return access(ids, response, request, true, false);
    }

    /**
     * 批量禁止
     * @param ids
     * @param response
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/forbidConsumer",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult forbidConsumer(Long[] ids, HttpServletResponse response, HttpServletRequest request) throws Exception {
    	return access(ids, response, request, false, false);
    }

    /**
     * 只允许
     * @param ids
     * @param response
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/onlyallowConsumer",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult onlyallowConsumer(Long[] ids, HttpServletResponse response, HttpServletRequest request) throws Exception {
    	return access(ids, response, request, true, true);
    }

    /**
     * 只禁止
     * @param ids
     * @param response
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/onlyforbidConsumer",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult onlyforbidConsumer(Long[] ids, HttpServletResponse response, HttpServletRequest request) throws Exception {
    	return access(ids, response, request, false, true);
    }

    /**
     * 允许，禁止操作共同类
     * @param ids
     * @param response
     * @param request
     * @param allow
     * @param only
     * @return
     * @throws Exception
     */
    private JsonResult access(Long[] ids, HttpServletResponse response, HttpServletRequest request, boolean allow, boolean only) throws Exception {

        JsonResult jsonResult = new JsonResult();
        User currentUser =(User) request.getSession().getAttribute(Constants.CURRENT_USER);
         String operator = currentUser.getUsername();

        if (ids == null || ids.length == 0){
            jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

            return jsonResult;
        }
        List<Consumer> consumers = new ArrayList<Consumer>();
        for (Long id : ids) {
            Consumer c = consumerService.findConsumer(id);
            if(c != null){
                consumers.add(c);
                if (!currentUser.hasServicePrivilege(c.getService())) {
                    jsonResult = JsonResultUtils.getJsonResult(c.getService(),SystemConstants.RESPONSE_STATUS_FAILURE,
                            SystemErrorCode.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR,SystemConstants.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR);

                    return jsonResult;
                }
            }
        }
        Map<String, Set<String>> serviceAddresses = new HashMap<String, Set<String>>();
        for(Consumer consumer : consumers) {
        	String service = consumer.getService();
        	String address = Tool.getIP(consumer.getAddress());
        	Set<String> addresses = serviceAddresses.get(service);
        	if (addresses == null) {
        		addresses = new HashSet<String>();
        		serviceAddresses.put(service, addresses);
        	}
        	addresses.add(address);
        }
        for(Map.Entry<String, Set<String>> entry : serviceAddresses.entrySet()) {
            String service = entry.getKey();
            boolean isFirst = false;
            List<Route> routes = routeService.findForceRouteByService(service);
            Route route = null;
            if(routes == null || routes.size() == 0){
                isFirst = true;
                route  = new Route();
                route.setService(service);
                route.setForce(true);
                route.setName(service+" blackwhitelist");
                route.setFilterRule("false");
                route.setEnabled(true);
            } else {
                route = routes.get(0);
            }
            Map<String, MatchPair> when = null;
            MatchPair matchPair = null;
            if(isFirst){
                when = new HashMap<String, MatchPair>();
                matchPair = new MatchPair(new HashSet<String>(),new HashSet<String>());
                when.put("consumer.host", matchPair);
            }else{
                when = RouteRule.parseRule(route.getMatchRule());
                matchPair = when.get("consumer.host");
            }
            if (only) {
            	matchPair.getUnmatches().clear();
            	matchPair.getMatches().clear();
            	if (allow) {
            		matchPair.getUnmatches().addAll(entry.getValue());
            	} else {
            		matchPair.getMatches().addAll(entry.getValue());
            	}
            } else {
            	for (String consumerAddress : entry.getValue()) {
                	if(matchPair.getUnmatches().size() > 0) { // 白名单优先
                		matchPair.getMatches().remove(consumerAddress); // 去掉黑名单中相同数据
                		if (allow) { // 如果允许访问
                			matchPair.getUnmatches().add(consumerAddress); // 加入白名单
                		} else { // 如果禁止访问
                			matchPair.getUnmatches().remove(consumerAddress); // 从白名单中去除
                		}
                    } else { // 黑名单生效
                    	if (allow) { // 如果允许访问
                    		matchPair.getMatches().remove(consumerAddress); // 从黑名单中去除
                    	} else { // 如果禁止访问
                    		matchPair.getMatches().add(consumerAddress); // 加入黑名单
                    	}
                    }
                }
            }
            StringBuilder sb = new StringBuilder();
            RouteRule.contidionToString(sb,when);
            route.setMatchRule(sb.toString());
            route.setUsername(operator);
            if (matchPair.getMatches().size() > 0 || matchPair.getUnmatches().size() > 0) {
            	if(isFirst) {
                	routeService.createRoute(route);
                } else {
                	routeService.updateRoute(route);
                }
            } else if (! isFirst) {
        		routeService.deleteRoute(route.getId());
        	}
        }
        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;
    }
}
