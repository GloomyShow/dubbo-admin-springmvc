package com.shawn.dubbo.vo;

import java.util.HashMap;
import java.util.Map;

/**
 * 接收前台数据类
 * Created by 594829 on 2015/12/30.
 */
public class RouteRequestContext {

    private Object method;
    private Object unmethod;
    private Object consumerApplication;
    private Object unconsumerApplication;
    private Object consumerCluster;
    private Object unconsumerCluster;

    private Object consumerHost;
    private Object unconsumerHost;
    private Object consumerVersion;
    private Object unconsumerVersion;
    private Object consumerGroup;
    private Object unconsumerGroup;

    private Object   providerApplication;
    private Object   unproviderApplication;
    private Object   providerCluster;
    private Object   unproviderCluster;
    private Object   providerHost;
    private Object   unproviderHost;
    private Object   providerProtocol;
    private Object   unproviderProtocol;
    private Object   providerPort;
    private Object   unproviderPort;
    private Object   providerVersion;
    private Object   unproviderVersion;
    private Object   providerGroup;
    private Object   unproviderGroup;

    private Object blacks;
    private Object  priority;
    private Long routeId;
    private String routeName;
    private String service;
    private String address;
    private Long consumerId;


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

    public  Map<String,Object> requestContextToMap(RouteRequestContext routeRequestContext){

        Map<String,Object> requestMap = new HashMap<String, Object>();

        requestMap.put("method", routeRequestContext.getMethod());
        requestMap.put("unmethod", routeRequestContext.getUnmethod());
        requestMap.put("consumerApplication", routeRequestContext.getConsumerApplication());
        requestMap.put("unconsumerApplication", routeRequestContext.getUnconsumerApplication());
        requestMap.put("consumerCluster", routeRequestContext.getConsumerCluster());
        requestMap.put("unconsumerCluster", routeRequestContext.getUnconsumerCluster());
        requestMap.put("consumerHost", routeRequestContext.getConsumerHost());
        requestMap.put("unconsumerHost", routeRequestContext.getUnconsumerHost());
        requestMap.put("consumerVersion", routeRequestContext.getConsumerVersion());
        requestMap.put("unconsumerVersion", routeRequestContext.getUnconsumerVersion());
        requestMap.put("consumerGroup", routeRequestContext.getConsumerGroup());
        requestMap.put("unconsumerGroup", routeRequestContext.getUnconsumerGroup());

        requestMap.put("providerApplication", routeRequestContext.getProviderApplication());
        requestMap.put("unproviderApplication", routeRequestContext.getUnproviderApplication());
        requestMap.put("providerCluster", routeRequestContext.getProviderCluster());
        requestMap.put("unproviderCluster", routeRequestContext.getUnproviderCluster());
        requestMap.put("providerHost", routeRequestContext.getProviderHost());
        requestMap.put("unproviderHost", routeRequestContext.getUnproviderHost());
        requestMap.put("providerProtocol", routeRequestContext.getProviderProtocol());
        requestMap.put("unproviderProtocol", routeRequestContext.getUnproviderProtocol());
        requestMap.put("providerPort", routeRequestContext.getProviderPort());
        requestMap.put("unproviderPort", routeRequestContext.getUnproviderPort());
        requestMap.put("providerVersion", routeRequestContext.getProviderVersion());
        requestMap.put("unproviderVersion", routeRequestContext.getUnproviderVersion());
        requestMap.put("providerGroup", routeRequestContext.getProviderGroup());
        requestMap.put("unproviderGroup", routeRequestContext.getUnproviderGroup());


        requestMap.put("black", routeRequestContext.getBlacks());
        requestMap.put("priority", routeRequestContext.getPriority());
        requestMap.put("service", routeRequestContext.getService());
        requestMap.put("address", routeRequestContext.getAddress());
        requestMap.put("consumerId", routeRequestContext.getConsumerId());

        return requestMap;

    }
}
