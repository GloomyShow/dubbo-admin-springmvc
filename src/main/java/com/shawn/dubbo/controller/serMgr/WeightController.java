/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.shawn.dubbo.controller.serMgr;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.shawn.dubbo.dao.Access;
import com.shawn.dubbo.dao.Provider;
import com.shawn.dubbo.dao.User;
import com.shawn.dubbo.dao.Weight;
import com.shawn.dubbo.serMgrHelper.OverrideUtils;
import com.shawn.dubbo.serMgrHelper.Tool;
import com.shawn.dubbo.service.OverrideService;
import com.shawn.dubbo.service.ProviderService;
import com.shawn.dubbo.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;


@Controller
@RequestMapping("/weight")
public class WeightController {
    
    @Autowired
    private OverrideService overrideService;
    
    @Autowired
    private ProviderService providerService;

    /**
     * 获取所有符合条件的权重
     * @param service
     * @param address
     * @return
     */
    @RequestMapping(value = "/findAllWeight",method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<List<Weight>> findAllWeight(String service, String address) {

        JsonResult<List<Weight>> listJsonResult = new JsonResult<List<Weight>>();
        address = Tool.getIP(address);
        List<Weight> weights = new ArrayList<Weight>();
        if (service != null && service.length() > 0) {
            weights = OverrideUtils.overridesToWeights(overrideService.findByService(service));
        } else if (address != null && address.length() > 0) {
            weights = OverrideUtils.overridesToWeights(overrideService.findByAddress(address));
        } else {
            weights = OverrideUtils.overridesToWeights(overrideService.findAll());
        }
        listJsonResult = JsonResultUtils.getJsonResult(weights, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return listJsonResult;
    }
  

    /**
     * load页面相关数据，用于新增修改。
     * @param service
     * @return
     */
    @RequestMapping(value="/loadWeight" ,method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<Map<String,Object>> loadWeight(String service) {
        JsonResult<Map<String,Object>> mapJsonResult = new JsonResult<Map<String, Object>>();
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
            List<String> serviceList = Tool.sortSimpleName(providerService.findServices());//增加服务列表
            resultMap.put("serviceList", serviceList);
        }
      /*  if(resultMap.get("input") != null)
            resultMap.put("input", resultMap.get("input"));*/

        mapJsonResult = JsonResultUtils.getJsonResult(resultMap, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return mapJsonResult;
    }

    
    private static final Pattern IP_PATTERN       = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}$");
    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");
    private static final Pattern ALL_IP_PATTERN   = Pattern.compile("0{1,3}(\\.0{1,3}){3}$");



    /**
     * 新增权重
     * @param weight
     * @param request
     * @return
     */
    @RequestMapping(value = "/createWeight",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult createWeight(Weight weight, HttpServletRequest request){
        JsonResult jsonResult = new JsonResult();
        User currentUser = (User) request.getSession().getAttribute(Constants.CURRENT_USER);
        String operator = currentUser.getUsername();

        String addr = (String) weight.getAddress();
        String services = (String) weight.getService();
        if(services == null || services.trim().length() == 0) {
            jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

            return jsonResult;
        }

        int w = weight.getWeight();
        
        Set<String> addresses = new HashSet<String>();
        BufferedReader reader = new BufferedReader(new StringReader(addr));

        try {
            while (true) {
                String line = null;

                    line = reader.readLine();

                if (null == line)
                    break;

                String[] split = line.split("[\\s,;]+");
                    for (String s : split) {
                        if (s.length() == 0)
                            continue;

                    String ip = s;
                    String port = null;
                    if(s.indexOf(":") != -1) {
                        ip = s.substring(0, s.indexOf(":"));
                        port = s.substring(s.indexOf(":") + 1, s.length());
                        if(port.trim().length() == 0) port = null;
                    }
                    if (!IP_PATTERN.matcher(ip).matches()) {
                        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                                SystemErrorCode.PARAMETER_ILLEGAL_IP,SystemConstants.PARAMETER_ILLEGAL_IP+s);

                        return jsonResult;
                    }
                    if (LOCAL_IP_PATTERN.matcher(ip).matches() || ALL_IP_PATTERN.matcher(ip).matches()) {
                        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                                SystemErrorCode.PARAMETER_ILLEGAL_LOCALIP_ANYHOSTIP,SystemConstants.PARAMETER_ILLEGAL_LOCALIP_ANYHOSTIP+s);

                        return jsonResult;
                    }
                    if(port != null) {
                        if(!NumberUtils.isDigits(port)) {
                            jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                                    SystemErrorCode.PARAMETER_ILLEGAL_IP,SystemConstants.PARAMETER_ILLEGAL_IP+s);

                            return jsonResult;
                        }
                    }
                    addresses.add(s);
                }
            }
        
            Set<String> aimServices  = new HashSet<String>();
            reader = new BufferedReader(new StringReader(services));
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

        for(String aimService : aimServices) {
            for (String a : addresses) {
                Weight wt = new Weight();
                wt.setUsername(operator);
                wt.setAddress(Tool.getIP(a));
                wt.setService(aimService);
                wt.setWeight(w);
                overrideService.saveOverride(OverrideUtils.weightToOverride(wt));
            }
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;

        return jsonResult;
    }


    /**
     * 根据ID查找权重信息
     * @param id
     * @return
     */
    @RequestMapping(value ="/findWeightById",method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<Map<String,Object>> findWeightById(Long id) {
        JsonResult<Map<String,Object>> mapJsonResult = new JsonResult<Map<String, Object>>();

        Map<String, Object> resultMap = new HashMap<String, Object>();
        Weight weight = OverrideUtils.overrideToWeight(overrideService.findById(id));
        resultMap.put("weight", weight);
        resultMap.put("service", overrideService.findById(id).getService());

        mapJsonResult = JsonResultUtils.getJsonResult(resultMap, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return mapJsonResult;
    }
    


    /**
     * 更新权重设置
     * @param weight
     * @return
     */
    @RequestMapping(value = "/updateWeight",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateWeight(Weight weight) {
        JsonResult jsonResult = new JsonResult();
        weight.setAddress(Tool.getIP(weight.getAddress()));
    	overrideService.updateOverride(OverrideUtils.weightToOverride(weight));
        jsonResult = JsonResultUtils.getJsonResult(null, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @RequestMapping(value = "/deleteWeight",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult deleteWeight(Long[] ids) {

        JsonResult jsonResult = new JsonResult();
        for (Long id : ids) {
            Weight w = OverrideUtils.overrideToWeight(overrideService.findById(id));
        }
        
        for (Long id : ids) {
        	overrideService.deleteOverride(id);
        }
        jsonResult = JsonResultUtils.getJsonResult(null, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;
    }

}
