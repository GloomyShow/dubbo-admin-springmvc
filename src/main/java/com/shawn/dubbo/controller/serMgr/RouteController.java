/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.shawn.dubbo.controller.serMgr;

import com.alibaba.citrus.util.StringUtil;
import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.shawn.dubbo.common.route.ParseUtils;
import com.shawn.dubbo.common.route.RouteRule;
import com.shawn.dubbo.common.route.RouteUtils;
import com.shawn.dubbo.dao.Consumer;
import com.shawn.dubbo.dao.Provider;
import com.shawn.dubbo.dao.Route;
import com.shawn.dubbo.dao.User;
import com.shawn.dubbo.serMgrHelper.Tool;
import com.shawn.dubbo.service.ConsumerService;
import com.shawn.dubbo.service.OwnerService;
import com.shawn.dubbo.service.ProviderService;
import com.shawn.dubbo.service.RouteService;
import com.shawn.dubbo.utils.*;
import com.shawn.dubbo.vo.RouteRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.*;

/**
 * Providers.
 * URI: /services/$service/routes
 * 
 * @author ding.lid
 * @author william.liangf
 * @author tony.chenl
 */
@Controller
@RequestMapping("/route")
public class RouteController{
    
	private static final int MAX_RULE_LENGTH = 1000;
	
    @Autowired
    private RouteService routeService;
    
    @Autowired
    private ProviderService providerService;
    
    @Autowired
    private ConsumerService consumerService;
    
    static String[][] when_names = {
        {"method", "method", "unmethod"},
        {"consumer.application", "consumerApplication", "unconsumerApplication"},
        {"consumer.cluster", "consumerCluster", "unconsumerCluster"},
        {"consumer.host", "consumerHost", "unconsumerHost"},
        {"consumer.version", "consumerVersion", "unconsumerVersion"},
        {"consumer.group", "consumerGroup", "unconsumerGroup"},
    };

    static String[][] then_names = {
    	{"provider.application", "providerApplication", "unproviderApplication"},
        {"provider.cluster", "providerCluster", "unproviderCluster"}, // 要校验Cluster是否存在
        {"provider.host", "providerHost", "unproviderHost"},
        {"provider.protocol", "providerProtocol", "unproviderProtocol"},
        {"provider.port", "providerPort", "unproviderPort"},
        {"provider.version", "providerVersion", "unproviderVersion"},
        {"provider.group", "providerGroup", "unproviderGroup"}
    };


    /**
     * 获取所有路由
     * @param service 搜索条件
     * @param address
     * @return
     */
    @RequestMapping(value="/findAllRoute",method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<List<Route>> findAllRoute(String service, String address) {
        JsonResult<List<Route>> jsonResult = new JsonResult<List<Route>>();
        address = Tool.getIP(address);
        List<Route> routes;
        if (service != null && service.length() > 0
        		&& address != null && address.length() > 0) {
            routes = routeService.findByServiceAndAddress(service, address);
        } else if (service != null && service.length() > 0) {
            routes = routeService.findByService(service);
        } else if (address != null && address.length() > 0) {
            routes = routeService.findByAddress(address);
        } else {
            routes = routeService.findAll();
        }
        jsonResult = JsonResultUtils.getJsonResult(routes, SystemConstants.RESPONSE_STATUS_SUCCESS,
                null, SystemConstants.RESPONSE_MESSAGE_SUCCESS);

        return jsonResult;
    }

    /**
     * 载入新增路由页面需要的数据
     * @param service
     * @return
     */
    @RequestMapping(value = "/addRoute",method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<Map<String,Object>> addRoute(String service) {

        JsonResult<Map<String,Object>>  mapJsonResult = new JsonResult<Map<String, Object>>();
        Map<String,Object> resultMap = new HashMap<String, Object>();
        if (service != null && service.length() > 0 && !service.contains("*")) {
            resultMap.put("service", service);
            resultMap.put("methods", CollectionUtils.sort(new ArrayList<String>(providerService.findMethodsByService(service))));
        } else {
            List<String> serviceList = Tool.sortSimpleName(new ArrayList<String>(providerService.findServices()));
            resultMap.put("serviceList", serviceList);
        }

        if(resultMap.get("input") != null)
            resultMap.put("input", resultMap.get("input"));
        mapJsonResult = JsonResultUtils.getJsonResult(resultMap,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return mapJsonResult;
    }


    /**
     * 获取路由详细信息，用于编辑修改路由信息
     * @param routeId
     * @return
     */
    @RequestMapping(value = "/findRouteById",method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<Map<String,Object>> findRouteById(@RequestParam(value = "routeId") Long routeId) {
        JsonResult<Map<String,Object>>  mapJsonResult = new JsonResult<Map<String, Object>>();
        Map<String,Object> resultMap = new HashMap<String, Object>();
        try {
            Route route = routeService.findRoute(routeId);
            if (route == null) {
                mapJsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                        SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

                return mapJsonResult;
            }

            if(route.getService()!=null && !route.getService().isEmpty()){
                resultMap.put("service", route.getService());
            }

            RouteRule routeRule= RouteRule.parse(route);

            @SuppressWarnings("unchecked")
            Map<String, RouteRule.MatchPair>[] paramArray = new Map[] {
                    routeRule.getWhenCondition(), routeRule.getThenCondition()};
            String[][][] namesArray = new String[][][] {when_names, then_names };

            for(int i=0; i<paramArray.length; ++i) {
                Map<String, RouteRule.MatchPair> param = paramArray[i];
                String[][] names = namesArray[i];
                for(String[] name : names) {
                    RouteRule.MatchPair matchPair = param.get(name[0]);
                    if(matchPair == null) {
                        continue;
                    }

                    if(!matchPair.getMatches().isEmpty()) {
                        String m = RouteRule.join(matchPair.getMatches());
                        resultMap.put(name[1], m);
                    }
                    if(!matchPair.getUnmatches().isEmpty()) {
                        String u = RouteRule.join(matchPair.getUnmatches());
                        resultMap.put(name[2], u);
                    }
                }
            }
            resultMap.put("route", route);
            resultMap.put("methods", CollectionUtils.sort(new ArrayList<String>(providerService.findMethodsByService(route.getService()))));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mapJsonResult = JsonResultUtils.getJsonResult(resultMap,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return mapJsonResult;
    }


    /**
     * 检验service合法性
     * @param service
     */
    static void checkService(String service) {
        if(service.contains(","))
            throw new IllegalStateException("service(" + service + ") contain illegale ','");
        String interfaceName = service;
        int gi = interfaceName.indexOf("/");
        if(gi != -1) interfaceName = interfaceName.substring(gi + 1);
        int vi = interfaceName.indexOf(':');
        if(vi != -1) interfaceName = interfaceName.substring(0, vi);
        
        if(interfaceName.indexOf('*') != -1 && interfaceName.indexOf('*') != interfaceName.length() -1) {
            throw new IllegalStateException("service(" + service + ") only allow 1 *, and must be last char!");
        }
    }
    

    /**
     * 新增路由信息
     * @param routeRequestContext
     * @param request
     * @return
     */
    @RequestMapping(value="/createRoute",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult createRoute(@RequestBody RouteRequestContext routeRequestContext,
                               HttpServletRequest request) {

        JsonResult jsonResult = new JsonResult();
        String name = routeRequestContext.getRouteName();
        String service = routeRequestContext.getService();

        if(StringUtils.isBlank(name) ||  StringUtils.isBlank(service) ){
            jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

            return jsonResult;
        }


        Map<String,Object> context = routeRequestContext.requestContextToMap(routeRequestContext);
        User currentUser = (User)request.getSession().getAttribute(Constants.CURRENT_USER);
        String operator = currentUser.getUsername();
        String operatorAddress = NetUtils.getLocalHost();



        if (StringUtils.isNotEmpty(service)
                && StringUtils.isNotEmpty(name)) {

            checkService(service);//检验service合法性
        	
            Map<String, String> when_name2valueList = new HashMap<String, String>();
            Map<String, String> notWhen_name2valueList = new HashMap<String, String>();
            for(String[] names : when_names) {
                when_name2valueList.put(names[0], (String)context.get(names[1]));
                notWhen_name2valueList.put(names[0], (String)context.get(names[2])); // value不为null的情况，这里处理，后面会保证
            }
            
            Map<String, String> then_name2valueList = new HashMap<String, String>();
            Map<String, String> notThen_name2valueList = new HashMap<String, String>();
            for(String[] names : then_names) {
                then_name2valueList.put(names[0], (String)context.get(names[1]));
                notThen_name2valueList.put(names[0], (String)context.get(names[2]));
            }

            RouteRule routeRule = RouteRule.createFromNameAndValueListString(
                    when_name2valueList, notWhen_name2valueList,
                    then_name2valueList, notThen_name2valueList);
            
            if (routeRule.getThenCondition().isEmpty()) {

                jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                        SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

                return jsonResult;
            }

            String matchRule = routeRule.getWhenConditionString();
            String filterRule = routeRule.getThenConditionString();
            
            //限制表达式的长度
            if (matchRule.length() > MAX_RULE_LENGTH) {
                jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                        SystemErrorCode.PARAMETER_TOO_LONG,SystemConstants.PARAMETER_TOO_LONG);

                return jsonResult;
            }
            if (filterRule.length() > MAX_RULE_LENGTH) {
                jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                        SystemErrorCode.PARAMETER_TOO_LONG,SystemConstants.PARAMETER_TOO_LONG);

                return jsonResult;
            }
            
            Route route = new Route();
            route.setService(service);
            route.setName(name);
            route.setUsername(operator);
            route.setOperator(operatorAddress);
            route.setRule(routeRule.toString());
            if (StringUtils.isNotEmpty((String)context.get("priority"))) {
                route.setPriority(Integer.parseInt((String)context.get("priority")));
            }
            routeService.createRoute(route);
            
        }

        jsonResult = JsonResultUtils.getJsonResult(null, SystemConstants.RESPONSE_STATUS_SUCCESS,
                null, SystemConstants.RESPONSE_MESSAGE_SUCCESS);

        return jsonResult;
    }



    /**
     * 保存更新数据
     * @param requestContext
     * @param request
     * @return
     */
    @RequestMapping(value = "/updateRoute",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateRoute(@RequestBody RouteRequestContext requestContext,
                                    HttpServletRequest request) {

        JsonResult jsonResult = new JsonResult();
        Long routeId = requestContext.getRouteId();
        String name = requestContext.getRouteName();
        if(StringUtils.isBlank(name) ||  routeId == null ){
            jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

            return jsonResult;
        }


        Map<String,Object> context = requestContext.requestContextToMap(requestContext);
        User currentUser = (User)request.getSession().getAttribute(Constants.CURRENT_USER);
        String operator = currentUser.getUsername();
        String operatorAddress = NetUtils.getLocalHost();
        String idStr = routeId.toString();
        if (idStr != null && idStr.length() > 0 ) {
            String[] blacks =(String[]) context.get("blacks");
            boolean black = false;
            if(blacks != null && blacks.length > 0){
                black = true;
            }

            Route oldRoute = routeService.findRoute(routeId);
            if(null == oldRoute) {
                jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                        SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

                return jsonResult;
            }
            //判断参数，拼凑rule
            if (StringUtils.isNotEmpty(name)) {
            	String service = oldRoute.getService();

                Map<String, String> when_name2valueList = new HashMap<String, String>();
                Map<String, String> notWhen_name2valueList = new HashMap<String, String>();
                for(String[] names : when_names) {
                    when_name2valueList.put(names[0], (String)context.get(names[1]));
                    notWhen_name2valueList.put(names[0], (String)context.get(names[2])); // value不为null的情况，这里处理，后面会保证
                }

                Map<String, String> then_name2valueList = new HashMap<String, String>();
                Map<String, String> notThen_name2valueList = new HashMap<String, String>();
                for(String[] names : then_names) {
                    then_name2valueList.put(names[0], (String)context.get(names[1]));
                    notThen_name2valueList.put(names[0], (String)context.get(names[2]));
                }

                RouteRule routeRule = RouteRule.createFromNameAndValueListString(
                        when_name2valueList, notWhen_name2valueList,
                        then_name2valueList, notThen_name2valueList);
                
                RouteRule result = null;
                if(black){
                    RouteRule.MatchPair matchPair = routeRule.getThenCondition().get("black");
                    Map<String, RouteRule.MatchPair> then = null;
                    if(null == matchPair) {
                        matchPair = new RouteRule.MatchPair();
                        then = new HashMap<String, RouteRule.MatchPair>();
                        then.put("black", matchPair);
                    }else{
                        matchPair.getMatches().clear();
                    }
                    matchPair.getMatches().add(String.valueOf(black));
                    result =  RouteRule.copyWithReplace(routeRule, null, then);
                }
                
                if(result == null){
                    result = routeRule;
                }
                
                if (result.getThenCondition().isEmpty()) {
                    jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                            SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

                    return jsonResult;
                }
                
                String matchRule = result.getWhenConditionString();
                String filterRule = result.getThenConditionString();
                
                //限制表达式的长度
                if (matchRule.length() > MAX_RULE_LENGTH) {
                        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                        SystemErrorCode.PARAMETER_TOO_LONG,SystemConstants.PARAMETER_TOO_LONG);

                    return jsonResult;
                }
                if (filterRule.length() > MAX_RULE_LENGTH) {
                    jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                            SystemErrorCode.PARAMETER_TOO_LONG,SystemConstants.PARAMETER_TOO_LONG);

                    return jsonResult;
                }
                
                int priority = 0;
                if (StringUtils.isNotEmpty((String) context.get("priority"))) {
                    priority = Integer.parseInt((String)context.get("priority"));
                }
                
                Route route = new Route();
                route.setRule(result.toString());
                route.setService(service);
                route.setPriority(priority);
                route.setName(name);
                route.setUsername(operator);
                route.setOperator(operatorAddress);
                route.setId(Long.valueOf(idStr));
                route.setPriority(priority);
                route.setEnabled(oldRoute.isEnabled());
                routeService.updateRoute(route);
                
                Set<String> usernames = new HashSet<String>();
                usernames.add(operator);
                usernames.add(route.getUsername());

                
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("action", "update");
                params.put("route", route);
                
            }
        }

        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;
    }

    /**
     * 批量删除指定ID的route规则
     * @param ids
     * @return
     */
    @RequestMapping(value="/deleteRoute",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult deleteRoute(Long[] ids) {
    	JsonResult jsonResult = new JsonResult();
    	  for (Long id : ids) {
    		 routeService.deleteRoute(id);
         }

        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;
    }
    
    /**
     * 启用指定ID的route规则（可以批量处理）
     * @param ids
     * @return
     */
    @RequestMapping(value="/enableRoute",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult enableRoute(Long[] ids) {
        JsonResult jsonResult = new JsonResult();
        for(Long id : ids){
            routeService.enableRoute(id);
        }
        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;
    }
    
    /**
     * 禁用指定ID的route规则（可以批量处理）
     * @param ids
     * @return
     */
    @RequestMapping(value="/disableRoute",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult disableRoute(Long[] ids) {
        JsonResult jsonResult = new JsonResult();
        for(Long id : ids){
            routeService.disableRoute(id);
        }
        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;
    }
    

    /**
     * 选择路由，预览相关路由信息
     * @param routeId
     * @return
     */
    @RequestMapping(value="/routeselect",method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<Map<String,Object>> routeselect(Long routeId){

        JsonResult<Map<String,Object>> mapJsonResult = new JsonResult<Map<String, Object>>();
        Map<String,Object> resultMap = new HashMap<String, Object>();

        resultMap.put("id", routeId);
        
        Route route = routeService.findRoute(routeId);
        if (route == null) {
            mapJsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

            return mapJsonResult;
        }

        //放入route对象
        resultMap.put("route", route);
        // 获取数据
        List<Consumer> consumers = consumerService.findByService(route.getService());
        resultMap.put("consumers", consumers);
        
        Map<String, Boolean> matchRoute = new HashMap<String, Boolean>();
        for(Consumer c : consumers) {
            matchRoute.put(c.getAddress(), RouteUtils.matchRoute(c.getAddress(), null, route, null));
        }
        resultMap.put("matchRoute", matchRoute);

        mapJsonResult = JsonResultUtils.getJsonResult(resultMap,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return mapJsonResult;
    }


    /**
     * 通过消费者地址预览
     * @param requestContext routeId,consumerId,address,service
     * @return
     */
    @RequestMapping(value="/preview")
    @ResponseBody
    public JsonResult<Map<String,Object>> preview(@RequestBody RouteRequestContext requestContext){

        JsonResult<Map<String,Object>> mapJsonResult = new JsonResult<Map<String, Object>>();
        Map<String,Object> resultMap = new HashMap<String, Object>();

        Map<String,Object> context = requestContext.requestContextToMap(requestContext);


        String rid = (String)context.get("routeId");
        String consumerid = (String)context.get("consumerId");
        
        
        if(StringUtils.isEmpty(rid)) {
            mapJsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

            return mapJsonResult;
        }
        
        Map<String, String> serviceUrls = new HashMap<String, String>();
        Route route = routeService.findRoute(Long.valueOf(rid));
        if(null == route) {
            mapJsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

            return mapJsonResult;
        }
        List<Provider> providers = providerService.findByService(route.getService());
        if (providers != null) {
            for (Provider p : providers) {
                serviceUrls.put(p.getUrl(), p.getParameters());
            }
        }
        if(StringUtils.isNotEmpty(consumerid)) {
            Consumer consumer = consumerService.findConsumer(Long.valueOf(consumerid));
            if(null == consumer) {
                mapJsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                        SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

                return mapJsonResult;
            }
            Map<String, String> result = RouteUtils.previewRoute(consumer.getService(), consumer.getAddress(), consumer.getParameters(), serviceUrls,
                    route, null, null);
            resultMap.put("route", route);
            resultMap.put("consumer", consumer);
            resultMap.put("result", result);
        }
        else {
            String address = (String)context.get("address");
            String service = (String)context.get("service");
            
            Map<String, String> result = RouteUtils.previewRoute(service, address, null, serviceUrls,
                    route, null, null);
            context.put("route", route);
            
            Consumer consumer = new Consumer();
            consumer.setService(service);
            consumer.setAddress(address);
            resultMap.put("consumer", consumer);
            resultMap.put("result", result);
        }

        mapJsonResult = JsonResultUtils.getJsonResult(resultMap,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return mapJsonResult;

    }

    /**
     * 添加与服务相关的Owner
     * 
     * @param usernames 用于添加的用户名
     * @param serviceName 不含通配符
     */
    public static void addOwnersOfService(Set<String> usernames, String serviceName,
                                          OwnerService ownerDAO) {
        List<String> serviceNamePatterns = ownerDAO.findAllServiceNames();
        for (String p : serviceNamePatterns) {
            if (ParseUtils.isMatchGlobPattern(p, serviceName)) {
                List<String> list = ownerDAO.findUsernamesByServiceName(p);
                usernames.addAll(list);
            }
        }
    }

    /**
     * 添加与服务模式相关的Owner
     * 
     * @param usernames 用于添加的用户名
     * @param serviceNamePattern 服务模式，Glob模式
     */
    public static void addOwnersOfServicePattern(Set<String> usernames, String serviceNamePattern,
                                                OwnerService ownerDAO) {
        List<String> serviceNamePatterns = ownerDAO.findAllServiceNames();
        for (String p : serviceNamePatterns) {
            if (ParseUtils.hasIntersection(p, serviceNamePattern)) {
                List<String> list = ownerDAO.findUsernamesByServiceName(p);
                usernames.addAll(list);
            }
        }
    }
}
