/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.shawn.dubbo.controller.serMgr;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.shawn.dubbo.service.ConsumerService;
import com.shawn.dubbo.service.ProviderService;
import com.shawn.dubbo.utils.JsonResult;
import com.shawn.dubbo.utils.JsonResultUtils;
import com.shawn.dubbo.utils.SystemConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/address")
public class AddressController {
    
    @Autowired
    private ProviderService providerService;
    
    @Autowired
    private ConsumerService consumerService;

    /**
     * 根据条件获得所有的地址
     * @param service
     * @param application
     * @param address
     * @return
     */
    @RequestMapping(value="/findAllAddress",method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<Map<String,Object>> findAllAddress(String service, String application, String address) {

        JsonResult<Map<String,Object>> mapJsonResult = new JsonResult<Map<String, Object>>();
        Map<String,Object> resultMap = new HashMap<String, Object>();

        List<String> providerAddresses = null;
        List<String> consumerAddresses = null;
        
        if (application != null && application.length() > 0) {
            providerAddresses = providerService.findAddressesByApplication(application);
            consumerAddresses = consumerService.findAddressesByApplication(application);
        } else if (service != null && service.length() > 0) {
            providerAddresses = providerService.findAddressesByService(service);
            consumerAddresses = consumerService.findAddressesByService(service);
        }
        else {
            providerAddresses = providerService.findAddresses();
            consumerAddresses = consumerService.findAddresses();
        }
        
        Set<String> addresses = new TreeSet<String>();
        if (providerAddresses != null) {
            addresses.addAll(providerAddresses);
        }
        if (consumerAddresses != null) {
            addresses.addAll(consumerAddresses);
        }
        resultMap.put("providerAddresses", providerAddresses);
        resultMap.put("consumerAddresses", consumerAddresses);
        resultMap.put("addresses", addresses);


        if (StringUtils.isNotEmpty(address) && !"*".equals(address)) {
            address = address.toLowerCase();
            Set<String> newList = new HashSet<String>();
            Set<String> newProviders = new HashSet<String>();
            Set<String> newConsumers = new HashSet<String>();

            for (String o : addresses) {
                if (o.toLowerCase().indexOf(address) != -1) {
                    newList.add(o);
                }
            }
            for (String o : providerAddresses) {
                if (o.toLowerCase().indexOf(address) != -1) {
                    newProviders.add(o);
                }
            }
            for (String o : consumerAddresses) {
                if (o.toLowerCase().indexOf(address) != -1) {
                    newConsumers.add(o);
                }
            }
            resultMap.put("addresses", newList);
            resultMap.put("providerAddresses", newProviders);
            resultMap.put("consumerAddresses", newConsumers);
        }
        mapJsonResult = JsonResultUtils.getJsonResult(resultMap, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return mapJsonResult;
    }

}
