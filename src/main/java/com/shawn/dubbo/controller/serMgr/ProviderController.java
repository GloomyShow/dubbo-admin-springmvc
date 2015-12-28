package com.shawn.dubbo.controller.serMgr;


import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.shawn.dubbo.common.route.OverrideUtils;
import com.shawn.dubbo.dao.Provider;
import com.shawn.dubbo.dao.Override;
import com.shawn.dubbo.dao.User;
import com.shawn.dubbo.serMgrHelper.Tool;
import com.shawn.dubbo.service.OverrideService;
import com.shawn.dubbo.service.ProviderService;
import com.shawn.dubbo.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * /providers 全部提供者列表<br> Fin
 * GET /providers/add 新增提供者表单<br> Fin
 * POST /providers 创建提供者<br> Fin
 * GET /providers/$id 查看提供者详细<br>
 * GET /providers/$id/edit 编辑提供者表单<br> Fin
 * POST /providers/$id 更新提供者<br>
 * GET /providers/$id/delete 删除提供者<br> Fin
 * GET /providers/$id/tostatic 转为静态<br>
 * GET /providers/$id/todynamic 转为动态<br>
 * GET /providers/$id/enable 启用<br>
 * GET /providers/$id/disable 禁用<br>
 * GET /providers/$id/reconnect 重连<br>
 * GET /providers/$id/recover 恢复<br
 */

/**
 * Created by 594829 on 2015/12/24.
 */
@Controller
@RequestMapping("/provider")
public class ProviderController {


    @Autowired
    private ProviderService providerService;

    @Autowired
    private OverrideService overrideService;

    /**
     * 分页查询提供者
     * @author 594829 on 2015/12/25
     * @param provider 有搜索条件时，在service，application，或者address传值
     * @param response
     * @param request
     * @return 分页提供者
     */
    @RequestMapping(value = "/findPageProvider", method = RequestMethod.GET)
    @ResponseBody
    public JsonResult<Page<Provider>> findPageProvider(Provider provider, HttpServletResponse response, HttpServletRequest request) {

        JsonResult<Page<Provider>> jr = new JsonResult<Page<Provider>>();
        Page<Provider> providerPage = new Page<Provider>();
        String service = provider.getService();
        String application =  provider.getApplication();
        String address = provider.getAddress();

        String value = "";
        String separators = "....";
        List<Provider> providers = null;

        // service
        if (service != null && service.length() > 0) {
            providers = providerService.findByService(service);
            value = service + separators + request.getRequestURI();
        }
        // address
        else if (address != null && address.length() > 0) {
            providers = providerService.findByAddress(address);
            value = address + separators + request.getRequestURI();
        }
        // application
        else if (application != null && application.length() > 0) {
            providers = providerService.findByApplication(application);
            value = application + separators + request.getRequestURI();
        }
        // all
        else {
            providers = providerService.findAll();
        }

       /* context.put("providers", providers);*/

        // 设置搜索结果到cookie中
        setSearchHistroy( response,request,value);

        //分页查找
        List<Provider> pageProviders = new ArrayList<Provider>();
        int pageSize = provider.getPageSize();
        int curretPage = provider.getCurrentPage();
        int begin = provider.getStartRecord();
        int end = pageSize*curretPage;
        if(providers.size()>0){
            for(int i=begin;i<end;i++){
                pageProviders.add(providers.get(i));
            }
        }

        providerPage.setDatas(pageProviders);
        providerPage.setTotalRecord(providers.size());
        providerPage.setCurrentPage(curretPage);
        providerPage.setPageSize(pageSize);

        jr = JsonResultUtils.getJsonResult(providerPage, SystemConstants.RESPONSE_STATUS_SUCCESS,
                null, SystemConstants.RESPONSE_MESSAGE_SUCCESS);

        return jr;
    }


    /**
     * 设置search记录到cookie中，操作步骤：
     * 检查加入的记录是否已经存在cookie中，如果存在，则更新列表次序；如果不存在，则插入到最前面
     * @author 594829 on 2015/12/28
     * @param response
     * @param request
     * @param value
     */
    private void setSearchHistroy(HttpServletResponse response, HttpServletRequest request, String value) {
        //分析已有的cookie
        String separatorsB = "\\.\\.\\.\\.\\.\\.";
        String newCookiev = value;
        Cookie[] cookies = request.getCookies();
        for(Cookie c:cookies){
            if(c.getName().equals("HISTORY")){
                String cookiev = c.getValue();
                String[] values = cookiev.split(separatorsB);
                int count = 1;
                for(String v : values){
                    if(count<=10){
                        if(!value.equals(v)){
                            newCookiev = newCookiev + separatorsB + v;
                        }
                    }
                    count ++;
                }
                break;
            }
        }
        Cookie _cookie=new Cookie("HISTORY", newCookiev);
        _cookie.setMaxAge(60*60*24*7); // 设置Cookie的存活时间为30分钟
        _cookie.setPath("/");
        response.addCookie(_cookie); // 写入客户端硬盘
    }


    /**
     * 装载新增服务页面，获取所有的服务名称
     * @author 594829 on 2015/12/28
     * @param id
     * @param provider
     * @return
     */
    @RequestMapping(value="/addProvider",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult<Map<String,Object>> addProvider(Long id,Provider provider) {

        JsonResult jsonResult = new JsonResult();
        Map<String,Object> resultMap = new HashMap<String, Object>();

        if (provider.getService() == null) {
            List<String> serviceList = Tool.sortSimpleName(new ArrayList<String>(providerService.findServices()));
            resultMap.put("serviceList", serviceList);
        }
        if (id != null) {
            Provider p = providerService.findProvider(id);
            if (p != null) {
                resultMap.put("provider", p);
                String parameters = p.getParameters();
                if (parameters != null && parameters.length() > 0) {
                    Map<String, String> map = StringUtils.parseQueryString(parameters);
                    map.put("timestamp", String.valueOf(System.currentTimeMillis()));
                    map.remove("pid");
                    p.setParameters(StringUtils.toQueryString(map));
                }
            }
        }
        jsonResult = JsonResultUtils.getJsonResult(resultMap,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);
        return jsonResult;
    }


    /**
     * 创建提供者
     * @author 594829 on 2015/12/28
     * @param provider
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value="/createProvider",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult createProvider(Provider provider,HttpServletResponse response, HttpServletRequest request) {

        JsonResult jsonResult = new JsonResult();
        User currentUser = (User)request.getSession().getAttribute(Constants.CURRENT_USER);
        String service = provider.getService();
        if (!currentUser.hasServicePrivilege(service)) {
            jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR,SystemConstants.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR);

            return jsonResult;
        }
        if (provider.getParameters() == null) {
            String url = provider.getUrl();
            if (url != null) {
                int i = url.indexOf('?');
                if (i > 0) {
                    provider.setUrl(url.substring(0, i));
                    provider.setParameters(url.substring(i + 1));
                }
            }
        }
        provider.setDynamic(false); // 页面上添加的一定是静态的Provider
        providerService.create(provider);
        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;

        return jsonResult;
    }


    /**
     * 装载修改Provider界面
     * @author 594829 on 2015/12/28
     * @param id
     * @return
     */
    @RequestMapping(value="/editProvider",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult<Provider> editProvider(Long id) {
        JsonResult<Provider> jsonResult = new JsonResult<Provider>();
        Provider provider = providerService.findProvider(id);
            if (provider != null && provider.isDynamic()) {
                List<Override> overrides = overrideService.findByServiceAndAddress(provider.getService(), provider.getAddress());
                OverrideUtils.setProviderOverrides(provider, overrides);
            }
            jsonResult = JsonResultUtils.getJsonResult(provider,SystemConstants.RESPONSE_STATUS_SUCCESS,null,
                    SystemConstants.RESPONSE_MESSAGE_SUCCESS);
           return jsonResult;
    }


    /**
     * 更新提供者信息
     * @param newProvider
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value="/updateProvider",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateProvider(Provider newProvider, HttpServletResponse response, HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        User currentUser =(User) request.getSession().getAttribute(Constants.CURRENT_USER);
        String operator = currentUser.getUsername();
        String operatorAddress = NetUtils.getLocalHost();
        Long id = newProvider.getId();
        String parameters = newProvider.getParameters();
        Provider provider = providerService.findProvider(id);
        if (provider == null) {
            jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

            return jsonResult;
        }
        String service = provider.getService();
        if (!currentUser.hasServicePrivilege(service)) {
            jsonResult = JsonResultUtils.getJsonResult(provider.getService(),SystemConstants.RESPONSE_STATUS_FAILURE,
                    SystemErrorCode.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR,SystemConstants.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR);

            return jsonResult;
        }
        Map<String, String> oldMap = StringUtils.parseQueryString(provider.getParameters());
        Map<String, String> newMap = StringUtils.parseQueryString(parameters);
        for (Map.Entry<String, String> entry : oldMap.entrySet()) {
            if (entry.getValue().equals(newMap.get(entry.getKey()))) {
                newMap.remove(entry.getKey());
            }
        }
        if (provider.isDynamic()) {
            String address = provider.getAddress();
            List<Override> overrides = overrideService.findByServiceAndAddress(provider.getService(), provider.getAddress());
            OverrideUtils.setProviderOverrides(provider, overrides);
            Override override = provider.getOverride();
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
        } else {
            provider.setParameters(parameters);
            providerService.updateProvider(provider);
        }
        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;

        return jsonResult;
    }


    /**
     * 删除提供者
     * @param ids
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value="/deleteProvider",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult<Object> deleteProvider(Long[] ids, HttpServletResponse response, HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        User currentUser =(User) request.getSession().getAttribute(Constants.CURRENT_USER);
        for (Long id : ids) {
            Provider provider = providerService.findProvider(id);
            if (provider == null) {
                jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                        SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

                return jsonResult;
            } else if (provider.isDynamic()) {
                jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                        SystemErrorCode.CAN_NOT_DELETE_DYNAMICDATA_ERROR,SystemConstants.CAN_NOT_DELETE_DYNAMICDATA_ERROR);

                return jsonResult;
            } else if (! currentUser.hasServicePrivilege(provider.getService())) {
                jsonResult = JsonResultUtils.getJsonResult(provider.getService(),SystemConstants.RESPONSE_STATUS_FAILURE,
                        SystemErrorCode.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR,SystemConstants.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR);

                return jsonResult;
            }
        }
        for (Long id : ids) {
            providerService.deleteStaticProvider(id);
        }
        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;

        return jsonResult;
    }


    /**
     * 启用提供者
     * @param ids
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value="/enableProvider",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult<Object> enableProvider(Long[] ids, HttpServletResponse response, HttpServletRequest request) {
        JsonResult jsonResult = new JsonResult();
        User currentUser = (User)request.getSession().getAttribute(Constants.CURRENT_USER);
        Map<Long, Provider> id2Provider = new HashMap<Long, Provider>();
        for (Long id : ids) {
            Provider provider = providerService.findProvider(id);
            if (provider == null) {
                jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                        SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

                return jsonResult;
            } else if (! currentUser.hasServicePrivilege(provider.getService())) {
                jsonResult = JsonResultUtils.getJsonResult(provider.getService(),SystemConstants.RESPONSE_STATUS_FAILURE,
                        SystemErrorCode.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR,SystemConstants.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR);

                return jsonResult;
            }
            id2Provider.put(id, provider);
        }
        for (Long id : ids) {
            providerService.enableProvider(id);
        }
        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;
    }


    /**
     * 禁用提供者
     * @param ids
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value="/disableProvider",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult<Object> disableProvider(Long[] ids, HttpServletResponse response, HttpServletRequest request) {
       JsonResult jsonResult = new JsonResult();
        User currentUser = (User)request.getSession().getAttribute(Constants.CURRENT_USER);
        for (Long id : ids) {
            Provider provider = providerService.findProvider(id);
            if (provider == null) {
                jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_FAILURE,
                        SystemErrorCode.PARAMETER_HAS_NULLPOINTER,SystemConstants.PARAMETER_HAS_NULLPOINTER);

                return jsonResult;
            } else if (! currentUser.hasServicePrivilege(provider.getService())) {
                jsonResult = JsonResultUtils.getJsonResult(provider.getService(),SystemConstants.RESPONSE_STATUS_FAILURE,
                        SystemErrorCode.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR,SystemConstants.BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR);

                return jsonResult;
            }
        }
        for (Long id : ids) {
            providerService.disableProvider(id);
        }
        jsonResult = JsonResultUtils.getJsonResult(null,SystemConstants.RESPONSE_STATUS_SUCCESS,null,SystemConstants.RESPONSE_MESSAGE_SUCCESS);;
        return jsonResult;
    }

}
