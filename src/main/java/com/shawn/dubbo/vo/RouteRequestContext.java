package com.shawn.dubbo.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 接收前台数据类
 * Created by 594829 on 2015/12/30.
 */
public class RouteRequestContext implements Serializable{


    private static final long serialVersionUID =1L;
    private Object method;/** 匹配的    方法名 */
    private Object unmethod; /** 不匹配的    方法名 */
    private Object consumerApplication;/** 匹配的    消费者应用名 */
    private Object unconsumerApplication;/** 不匹配的    消费者应用名 */
    private Object consumerCluster;/** 匹配的    消费者集群 */
    private Object unconsumerCluster;/** 不匹配的    消费者集群 */

    private Object consumerHost;/** 匹配的    消费者IP地址 */
    private Object unconsumerHost;/** 不匹配的    消费者IP地址 */
    private Object consumerVersion;/** 匹配的    消费者版本号 */
    private Object unconsumerVersion;/** 不匹配的    消费者版本号 */
    private Object consumerGroup;/** 匹配的    消费者群组 */
    private Object unconsumerGroup;/** 不匹配的    消费者群组 */

    private Object   providerApplication;    /** 匹配的    提供者应用名 */
    private Object   unproviderApplication;  /** 不匹配的  提供者应用名*/
    private Object   providerCluster; /** 匹配的    提供者集群 */
    private Object   unproviderCluster; /** 不匹配的    提供者集群 */
    private Object   providerHost;/** 匹配的    提供者IP地址 */
    private Object   unproviderHost;/** 不匹配的    提供者IP地址 */
    private Object   providerProtocol; /** 匹配的    提供者协议 */
    private Object   unproviderProtocol;/** 不匹配的    提供者协议 */
    private Object   providerPort;/** 匹配的    提供者端口 */
    private Object   unproviderPort;/** 不匹配的    提供者端口 */
    private Object   providerVersion; /** 匹配的    提供者版本号 */
    private Object   unproviderVersion;/** 不匹配的    提供者版本号 */
    private Object   providerGroup;/** 匹配的    提供者群组 */
    private Object   unproviderGroup;/** 不匹配的    提供者群组 */

    private Object blacks; /** 黑名单 */
    private Object  priority; /** 优先级 */
    private Long routeId; /** 路由ID */
    private String routeName; /** 路由名称 */
    private String service; /** 服务名称 */
    private String address; /** 地址 */
    private Long consumerId; /** 消费者Id */


    public Long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(Long consumerId) {
        this.consumerId = consumerId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public Object getPriority() {
        return priority;
    }

    public void setPriority(Object priority) {
        this.priority = priority;
    }

    public Object getBlacks() {
        return blacks;
    }

    public void setBlacks(Object blacks) {
        this.blacks = blacks;
    }

    public Object getMethod() {
        return method;
    }

    public void setMethod(Object method) {
        this.method = method;
    }

    public Object getUnmethod() {
        return unmethod;
    }

    public void setUnmethod(Object unmethod) {
        this.unmethod = unmethod;
    }

    public Object getConsumerApplication() {
        return consumerApplication;
    }

    public void setConsumerApplication(Object consumerApplication) {
        this.consumerApplication = consumerApplication;
    }

    public Object getUnconsumerApplication() {
        return unconsumerApplication;
    }

    public void setUnconsumerApplication(Object unconsumerApplication) {
        this.unconsumerApplication = unconsumerApplication;
    }

    public Object getConsumerCluster() {
        return consumerCluster;
    }

    public void setConsumerCluster(Object consumerCluster) {
        this.consumerCluster = consumerCluster;
    }

    public Object getUnconsumerCluster() {
        return unconsumerCluster;
    }

    public void setUnconsumerCluster(Object unconsumerCluster) {
        this.unconsumerCluster = unconsumerCluster;
    }

    public Object getConsumerHost() {
        return consumerHost;
    }

    public void setConsumerHost(Object consumerHost) {
        this.consumerHost = consumerHost;
    }

    public Object getUnconsumerHost() {
        return unconsumerHost;
    }

    public void setUnconsumerHost(Object unconsumerHost) {
        this.unconsumerHost = unconsumerHost;
    }

    public Object getConsumerVersion() {
        return consumerVersion;
    }

    public void setConsumerVersion(Object consumerVersion) {
        this.consumerVersion = consumerVersion;
    }

    public Object getUnconsumerVersion() {
        return unconsumerVersion;
    }

    public void setUnconsumerVersion(Object unconsumerVersion) {
        this.unconsumerVersion = unconsumerVersion;
    }

    public Object getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(Object consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public Object getUnconsumerGroup() {
        return unconsumerGroup;
    }

    public void setUnconsumerGroup(Object unconsumerGroup) {
        this.unconsumerGroup = unconsumerGroup;
    }

    public Object getProviderApplication() {
        return providerApplication;
    }

    public void setProviderApplication(Object providerApplication) {
        this.providerApplication = providerApplication;
    }

    public Object getUnproviderApplication() {
        return unproviderApplication;
    }

    public void setUnproviderApplication(Object unproviderApplication) {
        this.unproviderApplication = unproviderApplication;
    }

    public Object getProviderCluster() {
        return providerCluster;
    }

    public void setProviderCluster(Object providerCluster) {
        this.providerCluster = providerCluster;
    }

    public Object getUnproviderCluster() {
        return unproviderCluster;
    }

    public void setUnproviderCluster(Object unproviderCluster) {
        this.unproviderCluster = unproviderCluster;
    }

    public Object getProviderHost() {
        return providerHost;
    }

    public void setProviderHost(Object providerHost) {
        this.providerHost = providerHost;
    }

    public Object getUnproviderHost() {
        return unproviderHost;
    }

    public void setUnproviderHost(Object unproviderHost) {
        this.unproviderHost = unproviderHost;
    }

    public Object getProviderProtocol() {
        return providerProtocol;
    }

    public void setProviderProtocol(Object providerProtocol) {
        this.providerProtocol = providerProtocol;
    }

    public Object getUnproviderProtocol() {
        return unproviderProtocol;
    }

    public void setUnproviderProtocol(Object unproviderProtocol) {
        this.unproviderProtocol = unproviderProtocol;
    }

    public Object getProviderPort() {
        return providerPort;
    }

    public void setProviderPort(Object providerPort) {
        this.providerPort = providerPort;
    }

    public Object getUnproviderPort() {
        return unproviderPort;
    }

    public void setUnproviderPort(Object unproviderPort) {
        this.unproviderPort = unproviderPort;
    }

    public Object getProviderVersion() {
        return providerVersion;
    }

    public void setProviderVersion(Object providerVersion) {
        this.providerVersion = providerVersion;
    }

    public Object getUnproviderVersion() {
        return unproviderVersion;
    }

    public void setUnproviderVersion(Object unproviderVersion) {
        this.unproviderVersion = unproviderVersion;
    }

    public Object getProviderGroup() {
        return providerGroup;
    }

    public void setProviderGroup(Object providerGroup) {
        this.providerGroup = providerGroup;
    }

    public Object getUnproviderGroup() {
        return unproviderGroup;
    }

    public void setUnproviderGroup(Object unproviderGroup) {
        this.unproviderGroup = unproviderGroup;
    }

    public  Map<String,Object> requestContextToMap(){

        Map<String,Object> requestMap = new HashMap<String, Object>();

        requestMap.put("method", method);
        requestMap.put("unmethod", unmethod);
        requestMap.put("consumerApplication", consumerApplication);
        requestMap.put("unconsumerApplication", unconsumerApplication);
        requestMap.put("consumerCluster", consumerCluster);
        requestMap.put("unconsumerCluster", unconsumerCluster);
        requestMap.put("consumerHost", consumerHost);
        requestMap.put("unconsumerHost", unconsumerHost);
        requestMap.put("consumerVersion", consumerVersion);
        requestMap.put("unconsumerVersion", unconsumerVersion);
        requestMap.put("consumerGroup", consumerGroup);
        requestMap.put("unconsumerGroup", unconsumerGroup);

        requestMap.put("providerApplication", providerApplication);
        requestMap.put("unproviderApplication", unproviderApplication);
        requestMap.put("providerCluster", providerCluster);
        requestMap.put("unproviderCluster", unproviderCluster);
        requestMap.put("providerHost", providerHost);
        requestMap.put("unproviderHost", unproviderHost);
        requestMap.put("providerProtocol", providerProtocol);
        requestMap.put("unproviderProtocol", unproviderProtocol);
        requestMap.put("providerPort", providerPort);
        requestMap.put("unproviderPort", unproviderPort);
        requestMap.put("providerVersion", providerVersion);
        requestMap.put("unproviderVersion", unproviderVersion);
        requestMap.put("providerGroup", providerGroup);
        requestMap.put("unproviderGroup", unproviderGroup);


        requestMap.put("black", blacks);
        requestMap.put("priority", priority);
        requestMap.put("service", service);
        requestMap.put("address", address);
        requestMap.put("consumerId", consumerId);

        return requestMap;

    }
}
