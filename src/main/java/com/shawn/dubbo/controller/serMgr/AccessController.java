/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.shawn.dubbo.controller.serMgr;


import com.shawn.dubbo.common.route.RouteRule;
import com.shawn.dubbo.common.route.RouteRule.MatchPair;
import com.shawn.dubbo.dao.Access;
import com.shawn.dubbo.dao.Route;
import com.shawn.dubbo.dao.User;
import com.shawn.dubbo.serMgrHelper.Tool;
import com.shawn.dubbo.service.ProviderService;
import com.shawn.dubbo.service.RouteService;

import com.shawn.dubbo.utils.Constants;
import com.shawn.dubbo.utils.JsonResult;
import com.shawn.dubbo.utils.JsonResultUtils;
import com.shawn.dubbo.utils.SystemConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;



@Controller
@RequestMapping("/access")
public class AccessController {

    @Autowired
    private RouteService routeService;

    @Autowired
    private ProviderService providerService;


    /**
     * 根据条件获得所有满足条件的访问控制
     * @param service
     * @param address
     * @return
     */
    @RequestMapping(value="/findAllAccess",method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<List<Access>> findAllAccess(String service, String address){

        JsonResult<List<Access>> listJsonResult = new JsonResult<List<Access>>();

        address = Tool.getIP(address);
        List<Route> routes;
        if (service != null && service.length() > 0) {
            routes = routeService.findForceRouteByService(service);
        } else if (address != null && address.length() > 0) {
            routes = routeService.findForceRouteByAddress(address);
        } else {
            routes = routeService.findAllForceRoute();
        }
        List<Access> accesses = new ArrayList<Access>();
        if(routes == null){
            listJsonResult = JsonResultUtils.getJsonResult(accesses, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
            return listJsonResult;
        }

        try {
            for(Route route :routes){
                Map<String, MatchPair> rule = null;
                rule = RouteRule.parseRule(route.getMatchRule());
                RouteRule.MatchPair pair = rule.get("consumer.host");
                if(pair != null){
                    for(String host : pair.getMatches()){
                        Access access = new Access();
                        access.setAddress(host);
                        access.setService(route.getService());
                        access.setAllow(false);
                        accesses.add(access);
                    }
                    for(String host : pair.getUnmatches()){
                        Access access = new Access();
                        access.setAddress(host);
                        access.setService(route.getService());
                        access.setAllow(true);
                        accesses.add(access);
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        listJsonResult = JsonResultUtils.getJsonResult(accesses, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return listJsonResult;
    }

    /**
     * 获得所有的服务列表
     * @return
     */
    @RequestMapping(value="/getServiceList" ,method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<Map<String,Object>> getServiceList() {
        JsonResult<Map<String,Object>> mapJsonResult = new JsonResult<Map<String, Object>>();
        Map<String,Object> resultMap = new HashMap<String, Object>();
        List<String> serviceList = Tool.sortSimpleName(providerService.findServices());
        resultMap.put("serviceList", serviceList);
        mapJsonResult = JsonResultUtils.getJsonResult(resultMap, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return mapJsonResult;
    }

    private static final Pattern IP_PATTERN       = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}$");

    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");
    private static final Pattern ALL_IP_PATTERN   = Pattern.compile("0{1,3}(\\.0{1,3}){3}$");


    /**
     * 新增访问控制
     * @param access
     * @param request
     * @return
     */
    @RequestMapping(value="/createAccess",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult createAccess(Access access, HttpServletRequest request) {

        JsonResult jsonResult = new JsonResult();

        String addr =  access.getAddress();
        String services = access.getService();

        User currentUser = (User)request.getSession().getAttribute(Constants.CURRENT_USER);
        String operator = currentUser.getUsername();
        try {
            Set<String> consumerAddresses = toAddr(addr);
            Set<String> aimServices = toService(services);

            for(String aimService : aimServices) {
                boolean isFirst = false;
                List<Route> routes = routeService.findForceRouteByService(aimService);
                Route route = null;
                if(routes==null||routes.size()==0){
                    isFirst = true;
                    route  = new Route();
                    route.setService(aimService);
                    route.setForce(true);
                    route.setName(aimService+" blackwhitelist");
                    route.setFilterRule("false");
                    route.setEnabled(true);
                }else{
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
                for (String consumerAddress : consumerAddresses) {
                    if(access.isAllow()){
                        matchPair.getUnmatches().add(Tool.getIP(consumerAddress));

                    }else{
                        matchPair.getMatches().add(Tool.getIP(consumerAddress));
                    }
                }
                StringBuilder sb = new StringBuilder();
                RouteRule.contidionToString(sb,when);
                route.setMatchRule(sb.toString());
                route.setUsername(operator);
                if(isFirst){
                    routeService.createRoute(route);
                }else{
                    routeService.updateRoute(route);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        jsonResult = JsonResultUtils.getJsonResult(null, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;
    }

    /**
     * 转换IP地址
     * @param addr
     * @return
     * @throws IOException
     */
    private Set<String> toAddr(String addr) throws IOException{
        Set<String> consumerAddresses = new HashSet<String>();
        BufferedReader reader = new BufferedReader(new StringReader(addr));
        while (true) {
            String line = reader.readLine();
            if (null == line)
                break;
            
            String[] split = line.split("[\\s,;]+");
            for (String s : split) {
                if (s.length() == 0)
                    continue;
                if (!IP_PATTERN.matcher(s).matches()) {
                    throw new IllegalStateException("illegal IP: " + s);
                }
                if (LOCAL_IP_PATTERN.matcher(s).matches() || ALL_IP_PATTERN.matcher(s).matches()) {
                    throw new IllegalStateException("local IP or any host ip is illegal: " + s);
                }

                consumerAddresses.add(s);
            }
        }
        return consumerAddresses;
    }

    /**
     * 转换Service
     * @param services
     * @return
     * @throws IOException
     */
    private Set<String> toService(String services) throws IOException{
        Set<String> aimServices  = new HashSet<String>();
        BufferedReader reader = new BufferedReader(new StringReader(services));
        while (true) {
            String line = reader.readLine();
            if (null == line)
                break;
            
            String[] split = line.split("[\\s,;]+");
            for (String s : split) {
                if (s.length() == 0)
                    continue;
                aimServices.add(s);
            }
        }
        return aimServices;
    }

    /**
     * 批量删除
     * @param accesses
     * @return
     */
    @RequestMapping(value="/deleteAccess",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult deleteAccess(String[] accesses) {

        JsonResult jsonResult = new JsonResult();
        Map<String,Set<String>> prepareToDeleate = new HashMap<String,Set<String>>();
        for(String s : accesses){
        	String service = s.split("=")[0];
            String address = s.split("=")[1];
            Set<String> addresses = prepareToDeleate.get(service);
            if(addresses == null){
            	prepareToDeleate.put(service, new HashSet<String>());
            	addresses = prepareToDeleate.get(service);
            }
            addresses.add(address);
        }

        for(Entry<String, Set<String>> entry : prepareToDeleate.entrySet()){
            
            String service = entry.getKey();
            List<Route> routes = routeService.findForceRouteByService(service);
            if(routes == null || routes.size() == 0){
                continue;
            }
            try {
                for(Route blackwhitelist : routes){
                    MatchPair pairs = null;

                        pairs = RouteRule.parseRule(blackwhitelist.getMatchRule()).get("consumer.host");

                    Set<String> matches = new HashSet<String>();
                    matches.addAll(pairs.getMatches());
                    Set<String> unmatches = new HashSet<String>();
                    unmatches.addAll(pairs.getUnmatches());
                    for(String pair : pairs.getMatches()){
                        for(String address : entry.getValue()){
                            if(pair.equals(address)){
                                matches.remove(pair);
                                break;
                            }
                        }
                    }
                    for(String pair : pairs.getUnmatches()){
                        for(String address : entry.getValue()){
                             if(pair.equals(address)){
                                 unmatches.remove(pair);
                                 break;
                             }
                        }
                    }
                    if(matches.size()==0 && unmatches.size()==0){
                        routeService.deleteRoute(blackwhitelist.getId());
                    }else{
                        Map<String, MatchPair> condition = new HashMap<String, MatchPair>();
                        condition.put("consumer.host", new MatchPair(matches,unmatches));
                        StringBuilder sb = new StringBuilder();
                        RouteRule.contidionToString(sb,condition);
                        blackwhitelist.setMatchRule(sb.toString());
                        routeService.updateRoute(blackwhitelist);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        jsonResult = JsonResultUtils.getJsonResult(null, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;
    }

}
