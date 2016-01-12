/*
 * Copyright 1999-2101 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shawn.dubbo.controller.serMgr;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.shawn.dubbo.service.ConsumerService;
import com.shawn.dubbo.service.OverrideService;
import com.shawn.dubbo.service.ProviderService;
import com.shawn.dubbo.dao.Override;
import com.shawn.dubbo.utils.JsonResult;
import com.shawn.dubbo.utils.JsonResultUtils;
import com.shawn.dubbo.utils.SystemConstants;
import com.shawn.dubbo.utils.SystemErrorCode;
import com.shawn.dubbo.vo.OverrideRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ding.lid
 */
@Controller
@RequestMapping("/override")
public class OverrideController {
    @Autowired
    private OverrideService overrideService;
    
    @Autowired
    private ProviderService providerService;
    
    @Autowired
    private ConsumerService consumerService;


    /**
     * 获得所有符合条件的动态配置
     * @param service 服务名
     * @param application 应用名
     * @param address 地址
     * @return
     */
    @RequestMapping(value="/findAllOverride",method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<List<Override>> findAllOverride(String service, String application, String address) {
        JsonResult<List<Override>> listJsonResult = new JsonResult<List<Override>>();

        List<Override> overrides = new ArrayList<Override>();
        if (StringUtils.isNotEmpty(service)) {
            overrides = overrideService.findByService(service);
        } else if(StringUtils.isNotEmpty(application)){
            overrides = overrideService.findByApplication(application);
        }else if(StringUtils.isNotEmpty(address)){
            overrides = overrideService.findByAddress(address);
        }
        else{
            overrides = overrideService.findAll();
        }

        listJsonResult = JsonResultUtils.getJsonResult(overrides, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);

        return listJsonResult;
    }


    /**
     * 装载新增动态配置界面
     * @param service
     * @param application
     * @return
     */
    @RequestMapping(value="/addOverride",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult<Map<String,Object>> addOverride(String service, String application){

        JsonResult<Map<String,Object>> mapJsonResult = new JsonResult<Map<String, Object>>();
        Map<String,Object> resultMap = new HashMap<String, Object>();

        List<String> serviceList = new ArrayList<String>();
        List<String> applicationList = new ArrayList<String>();

        if(StringUtils.isNotEmpty(application)){
            serviceList.addAll(providerService.findServicesByApplication(application));
            serviceList.addAll(consumerService.findServicesByApplication(application));
            resultMap.put("serviceList", serviceList);
        }else if(StringUtils.isNotEmpty(service)){
            applicationList.addAll(providerService.findApplicationsByServiceName(service));
            applicationList.addAll(consumerService.findApplicationsByServiceName(service));
            resultMap.put("applicationList", applicationList);
        }else{
            serviceList.addAll(providerService.findServices());
            serviceList.addAll(consumerService.findServices());
            providerService.findServicesByApplication(application);
            consumerService.findServicesByApplication(application);
        }
        resultMap.put("serviceList", serviceList);
        
        if (StringUtils.isNotEmpty(service) && !service.contains("*")) {
            resultMap.put("methods", CollectionUtils.sort(new ArrayList<String>(providerService.findMethodsByService(service))));
        }

        mapJsonResult = JsonResultUtils.getJsonResult(resultMap, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return mapJsonResult;
    }

    static final Pattern AND = Pattern.compile("\\&");
    static final Pattern EQUAL = Pattern.compile("([^=\\s]*)\\s*=\\s*(\\S*)");
    
    static Map<String, String> parseQueryString(String query) {
        HashMap<String, String> ret = new HashMap<String, String>();
        if(query == null || (query = query.trim()).length() == 0) return ret;
        
        String[] kvs = AND.split(query);
        for(String kv : kvs) {
            Matcher matcher = EQUAL.matcher(kv);
            if(!matcher.matches()) continue;
            String key = matcher.group(1);
            String value = matcher.group(2);
            ret.put(key, value);
        }
        
        return ret;
    }

    /**
     * 通过ID获取动态配置详细内容
     * @param id
     * @return
     */
    @RequestMapping(value = "/findOverrideById",method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<Map<String,Object>> findOverrideById(Long id) {

        JsonResult<Map<String,Object>> mapJsonResult = new JsonResult<Map<String, Object>>();
        Map<String,Object> resultMap = new HashMap<String, Object>();

        Override override = overrideService.findById(id);
        
        Map<String, String> parameters = parseQueryString(override.getParams());
        
        if(parameters.get(DEFAULT_MOCK_JSON_KEY)!=null){
            String mock = URL.decode(parameters.get(DEFAULT_MOCK_JSON_KEY));
            String[] tokens = parseMock(mock);
            resultMap.put(FORM_DEFAULT_MOCK_METHOD_FORCE, tokens[0]);
            resultMap.put(FORM_DEFAULT_MOCK_METHOD_JSON, tokens[1]);
            parameters.remove(DEFAULT_MOCK_JSON_KEY);
        }
        
        Map<String, String> method2Force = new LinkedHashMap<String, String>();
        Map<String, String> method2Json = new LinkedHashMap<String, String>();
        
        List<String> methods = CollectionUtils.sort(new ArrayList<String>(providerService.findMethodsByService(override.getService())));
        if(methods != null && methods.isEmpty()) {
            for(String m : methods) {
                parseMock(m, parameters.get(m + MOCK_JSON_KEY_POSTFIX), method2Force, method2Json);
                parameters.remove(m + MOCK_JSON_KEY_POSTFIX);
            }
        }
        for (Iterator<Map.Entry<String, String>> iterator = parameters.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, String> e =  iterator.next();
            String key = e.getKey();
            
            if(key.endsWith(MOCK_JSON_KEY_POSTFIX)) {
                String m = key.substring(0, key.length() - MOCK_JSON_KEY_POSTFIX.length());
                parseMock(m, e.getValue(), method2Force, method2Json);
                iterator.remove();
            }
        }

        resultMap.put("methods", methods);
        resultMap.put("methodForces", method2Force);
        resultMap.put("methodJsons", method2Json);
        resultMap.put("parameters", parameters);
        resultMap.put("override", override);

        mapJsonResult = JsonResultUtils.getJsonResult(resultMap, SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return mapJsonResult;
    }

    /**
     * 拼接Mock值
     * @param m
     * @param mock
     * @param method2Force
     * @param method2Json
     */
    private void parseMock(String m, String mock, Map<String, String> method2Force, Map<String, String> method2Json) {
        String[] tokens = parseMock(mock);
        method2Force.put(m, tokens[0]);
        method2Json.put(m, tokens[1]);
    }

    /**
     * 拼接Mock值
     * @param mock
     * @return
     */
    private String[] parseMock(String mock) {
        mock = URL.decode(mock);
        String force;
        if (mock.startsWith("force:")) {
            force = "force";
            mock = mock.substring("force:".length());
        } else if (mock.startsWith("fail:")) {
            force = "fail";
            mock = mock.substring("fail:".length());
        } else {
            force = "fail";
        }
        String[] tokens = new String[2];
        tokens[0] = force;
        tokens[1] = mock;
        return tokens;
    }
    
    static final String DEFAULT_MOCK_JSON_KEY = "mock";
    static final String MOCK_JSON_KEY_POSTFIX = ".mock";
    
    // FORM KEY
    
    static final String FORM_OVERRIDE_KEY = "overrideKey";
    static final String FORM_OVERRIDE_VALUE = "overrideValue";
    
    static final String FORM_DEFAULT_MOCK_METHOD_FORCE = "mockDefaultMethodForce";
    static final String FORM_DEFAULT_MOCK_METHOD_JSON = "mockDefaultMethodJson";
    
   /* static final String FORM_ORIGINAL_METHOD_FORCE_PREFIX = "mockMethodForce.";
    static final String FORM_ORIGINAL_METHOD_PREFIX = "mockMethod.";*/
    
    static final String FORM_DYNAMIC_METHOD_NAME_PREFIX = "mockMethodName";
    static final String FORM_DYNAMIC_METHOD_FORCE_PREFIX = "mockMethodForce";
    static final String FORM_DYNAMIC_METHOD_JSON_PREFIX = "mockMethodJson";


    /**
     * 新增 ，更新共同类
     * @param override
     * @param overrideRequestContext
     * @return
     */
    JsonResult catchParams(Override override, OverrideRequestContext overrideRequestContext) {

        JsonResult jsonResult = new JsonResult();
        Map<String,Object> context = overrideRequestContext.requestContextToMap();

        String service = override.getService();

        if(service == null || service.trim().length() == 0) {
            jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

            return jsonResult;
        }

        //默认mock值
        String defaultMockMethodForce = (String) context.get(FORM_DEFAULT_MOCK_METHOD_FORCE);
        String defaultMockMethodJson = (String) context.get(FORM_DEFAULT_MOCK_METHOD_JSON);


        Map<String, String> override2Value = new HashMap<String, String>();
        Map<String, String> method2Json = new HashMap<String, String>();
        
        for(Map.Entry<String, Object> param : context.entrySet()) {
            String key = param.getKey().trim();
            if(! (param.getValue() instanceof String) ) continue;
            
            String value = (String) param.getValue();

            //overridekey
            if(key.startsWith(FORM_OVERRIDE_KEY) && value != null && value.trim().length() > 0) {
                String index = key.substring(FORM_OVERRIDE_KEY.length());
                String overrideValue = (String) context.get(FORM_OVERRIDE_VALUE + index);
                if(overrideValue != null && overrideValue.trim().length() > 0) {
                    override2Value.put(value.trim(), overrideValue.trim());
                }
            }

            /**
             * 原修改的时候，选用old的方法
             */
          /* if(key.startsWith(FORM_ORIGINAL_METHOD_PREFIX) && value != null && value.trim().length() > 0) {
                String method = key.substring(FORM_ORIGINAL_METHOD_PREFIX.length());
                String force = (String) context.get(FORM_ORIGINAL_METHOD_FORCE_PREFIX + method);
                method2Json.put(method, force + ":" + value.trim());
            }*/
            
            if(key.startsWith(FORM_DYNAMIC_METHOD_NAME_PREFIX) && value != null && value.trim().length() > 0) {
                String index = key.substring(FORM_DYNAMIC_METHOD_NAME_PREFIX.length());
                String force = (String) context.get(FORM_DYNAMIC_METHOD_FORCE_PREFIX + index);
                String json = (String) context.get(FORM_DYNAMIC_METHOD_JSON_PREFIX + index);
                
                if(json != null && json.trim().length() > 0) {
                    method2Json.put(value.trim(), force + ":" + json.trim());
                }
            }
        }
        
        StringBuilder paramters = new StringBuilder();
        boolean isFirst = true;
        if(defaultMockMethodJson != null && defaultMockMethodJson.trim().length() > 0) {
            paramters.append("mock=").append(URL.encode(defaultMockMethodForce + ":" + defaultMockMethodJson.trim()));
            isFirst = false;
        }
        for(Map.Entry<String, String> e : method2Json.entrySet()) {
            if(isFirst) isFirst = false;
            else paramters.append("&");
            
            paramters.append(e.getKey()).append(MOCK_JSON_KEY_POSTFIX).append("=").append(URL.encode(e.getValue()));
        }
        for(Map.Entry<String, String> e : override2Value.entrySet()) {
            if(isFirst) isFirst = false;
            else paramters.append("&");
            
            paramters.append(e.getKey()).append("=").append(URL.encode(e.getValue()));
        }
        
        String p = paramters.toString();
        if(p.trim().length() == 0) {
            jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

            return jsonResult;
        }
        
        override.setParams(p);
        jsonResult = JsonResultUtils.getJsonResult(override, SystemConstants.RESPONSE_STATUS_SUCCESS,
                null, SystemConstants.RESPONSE_MESSAGE_SUCCESS);

        return jsonResult;
    }


    /**
     * 保存新增动态配置
     * @param override
     * @param overrideRequestContext
     * @return
     */
    @RequestMapping(value="/createOverride",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult createOverride(Override override, OverrideRequestContext overrideRequestContext) {

       JsonResult jsonResult = catchParams(override, overrideRequestContext);

        if(StringUtils.isEquals(jsonResult.getCode(),SystemConstants.RESPONSE_STATUS_SUCCESS)){
            Override newOverride = (Override) jsonResult.getData();
            overrideService.saveOverride(override);
        }

        return jsonResult;
    }


    /**
     * 修改更新
     * @param override
     * @param overrideRequestContext
     * @return
     */
    @RequestMapping(value="/updateOverride",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateOverride(Override override,OverrideRequestContext overrideRequestContext) {

        Override o = overrideService.findById(override.getId());
        override.setService(o.getService());
        override.setAddress(o.getAddress());
        override.setApplication(o.getApplication());

        JsonResult jsonResult = catchParams(override, overrideRequestContext);

        if(StringUtils.isEquals(jsonResult.getCode(),SystemConstants.RESPONSE_STATUS_SUCCESS)){
            Override newOverride = (Override) jsonResult.getData();
            overrideService.updateOverride(override);
        }

        return jsonResult;

    }

    /**
     * 删除动态配置
     * @param ids
     * @return
     */
    @RequestMapping(value="/deleteOverride" ,method = RequestMethod.POST)
    @ResponseBody
    public JsonResult deleteOverride(Long[] ids) {
        JsonResult jsonResult = new JsonResult();
        for (Long id : ids) {
            overrideService.deleteOverride(id);
        }
        jsonResult = JsonResultUtils.getJsonResult(null, SystemConstants.RESPONSE_STATUS_SUCCESS,
                null, SystemConstants.RESPONSE_MESSAGE_SUCCESS);

        return jsonResult;
    }

    /**
     * 批量启用
     * @param ids
     * @return
     */
    @RequestMapping(value="/enableOverride" ,method = RequestMethod.POST)
    @ResponseBody
    public JsonResult enableOverride(Long[] ids) {
       JsonResult jsonResult = new JsonResult();
        for(Long id : ids){
            Override override = overrideService.findById(id);
            if(override == null){
                jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                        SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

                return jsonResult;
            }
         }
        for (Long id : ids) {
            overrideService.enableOverride(id);
        }
       jsonResult = JsonResultUtils.getJsonResult(null, SystemConstants.RESPONSE_STATUS_SUCCESS,
               null, SystemConstants.RESPONSE_MESSAGE_SUCCESS);
       return jsonResult;
    }


    /**
     * 批量禁用
     * @param ids
     * @return
     */
    @RequestMapping(value="/disableOverride" ,method = RequestMethod.POST)
    @ResponseBody
    public JsonResult disableOverride(Long[] ids) {
        JsonResult jsonResult = new JsonResult();
        for(Long id : ids){
            Override override = overrideService.findById(id);
            if(override == null){
                jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                        SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

                return jsonResult;
            }
         }
        for (Long id : ids) {
            overrideService.disableOverride(id);
        }
        jsonResult = JsonResultUtils.getJsonResult(null, SystemConstants.RESPONSE_STATUS_SUCCESS,
                null, SystemConstants.RESPONSE_MESSAGE_SUCCESS);
        return jsonResult;
    }

}
