/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.shawn.dubbo.controller.serMgr;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.shawn.dubbo.dao.LoadBalance;
import com.shawn.dubbo.dao.Provider;
import com.shawn.dubbo.dao.User;
import com.shawn.dubbo.serMgrHelper.OverrideUtils;
import com.shawn.dubbo.serMgrHelper.Tool;
import com.shawn.dubbo.service.OverrideService;
import com.shawn.dubbo.service.ProviderService;
import com.shawn.dubbo.utils.Constants;
import com.shawn.dubbo.utils.JsonResult;
import com.shawn.dubbo.utils.JsonResultUtils;
import com.shawn.dubbo.utils.SystemConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/loadbalance")
public class LoadbalanceController{
    
    @Autowired
    private OverrideService overrideService;
    
    @Autowired
    private ProviderService providerService;

    /**
     * 得到所有符合条件的负载均衡
     * @param service
     * @return
     */
    @RequestMapping(value = "/findAllLoadBalance",method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<List<LoadBalance>> findAllLoadBalance(String service) {

        JsonResult<List<LoadBalance>> listJsonResult = new JsonResult<List<LoadBalance>>();

        List<LoadBalance> loadbalances;
        if (service != null && service.length() > 0) {
            loadbalances = OverrideUtils.overridesToLoadBalances(overrideService.findByService(service));
        } else {
            loadbalances = OverrideUtils.overridesToLoadBalances(overrideService.findAll());
        }
        listJsonResult = JsonResultUtils.getJsonResult(loadbalances, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return listJsonResult;
    }

    /**
     * 装载负载均衡相关数据，用于新增和修改
     * @param service
     * @return
     */
    @RequestMapping(value = "/loadLoadBalance",method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<Map<String,Object>> loadLoadBalance(String service) {
        JsonResult<Map<String,Object>> mapJsonResult= new JsonResult<Map<String, Object>>();
        Map<String,Object> resultMap = new HashMap<String, Object>();

        if (service != null && service.length() > 0 && !service.contains("*")) {
            List<Provider> providerList = providerService.findByService(service);
            List<String> addressList = new ArrayList<String>();
            for(Provider provider : providerList){
                addressList.add(provider.getUrl().split("://")[1].split("/")[0]);
            }
            resultMap.put("addressList", addressList);
            resultMap.put("service", service);
            resultMap.put("methods", CollectionUtils.sort(providerService.findMethodsByService(service)));
        } else {
            List<String> serviceList = Tool.sortSimpleName(providerService.findServices());
            resultMap.put("serviceList", serviceList);
        }
       /* if(resultMap.get("input") != null)
            resultMap.put("input", resultMap.get("input"));
*/
        mapJsonResult = JsonResultUtils.getJsonResult(resultMap, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return mapJsonResult;
    }

    /**
     * 根据ID获得负载均衡详细内容
     * @param id
     * @return
     */
    @RequestMapping(value = "/findLoadBalanceById",method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<LoadBalance> findLoadBalanceById(Long id) {
        JsonResult<LoadBalance> jsonResult = new JsonResult<LoadBalance>();
        LoadBalance loadbalance = OverrideUtils.overrideToLoadBalance(overrideService.findById(id));
        jsonResult = JsonResultUtils.getJsonResult(loadbalance, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;

    }

    /**
     * 新增负载均衡
     * @param loadBalance
     * @return
     */
    @RequestMapping(value = "/createLoadBalance",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult createLoadBalance(LoadBalance loadBalance,HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        User user = (User) request.getSession().getAttribute(Constants.CURRENT_USER);
        String operator = user.getUsername();
    	loadBalance.setUsername(operator);
    	overrideService.saveOverride(OverrideUtils.loadBalanceToOverride(loadBalance));

        jsonResult = JsonResultUtils.getJsonResult(null, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;
    }

    /**
     * 更新负载均衡信息
     * @param loadBalance
     * @return
     */
    @RequestMapping(value = "/updateLoadBalance",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateLoadBalance(LoadBalance loadBalance) {
        JsonResult jsonResult = new JsonResult();
    	overrideService.updateOverride(OverrideUtils.loadBalanceToOverride(loadBalance));
        jsonResult = JsonResultUtils.getJsonResult(null, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @RequestMapping(value = "/deleteLoadBalance",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult deleteLoadBalance(Long[] ids) {
        JsonResult jsonResult = new JsonResult();
        for (Long id : ids) {
        	overrideService.deleteOverride(id);
        }
        jsonResult = JsonResultUtils.getJsonResult(null, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;
    }

}
