package com.shawn.dubbo.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 594829 on 2016/1/5.
 */
public class OverrideRequestContext implements Serializable {

    private static final long serialVersionUID =1L;
    private  String[] overrideKey;//动态配置参数名
    private  String[]  overrideValue ;//动态配置参数值

    private String  mockDefaultMethodForce ;//默认降级选项值
    private String mockDefaultMethodJson ;//默认降级JSON传值

    private  String[]   mockMethodName ;//降级方法值
    private String[]   mockMethodForce ;//降级选项
    private String[]   mockMethodJson ;//降级JSON值


    public String[] getOverrideKey() {
        return overrideKey;
    }

    public void setOverrideKey(String[] overrideKey) {
        this.overrideKey = overrideKey;
    }

    public String[] getOverrideValue() {
        return overrideValue;
    }

    public void setOverrideValue(String[] overrideValue) {
        this.overrideValue = overrideValue;
    }

    public String getMockDefaultMethodForce() {
        return mockDefaultMethodForce;
    }

    public void setMockDefaultMethodForce(String mockDefaultMethodForce) {
        this.mockDefaultMethodForce = mockDefaultMethodForce;
    }

    public String getMockDefaultMethodJson() {
        return mockDefaultMethodJson;
    }

    public void setMockDefaultMethodJson(String mockDefaultMethodJson) {
        this.mockDefaultMethodJson = mockDefaultMethodJson;
    }

    public String[] getMockMethodName() {
        return mockMethodName;
    }

    public void setMockMethodName(String[] mockMethodName) {
        this.mockMethodName = mockMethodName;
    }

    public String[] getMockMethodForce() {
        return mockMethodForce;
    }

    public void setMockMethodForce(String[] mockMethodForce) {
        this.mockMethodForce = mockMethodForce;
    }

    public String[] getMockMethodJson() {
        return mockMethodJson;
    }

    public void setMockMethodJson(String[] mockMethodJson) {
        this.mockMethodJson = mockMethodJson;
    }



    public  Map<String,Object> requestContextToMap(){

        Map<String,Object> requestMap = new HashMap<String, Object>();

        for (int i=0;i<overrideKey.length;i++){
            String key = "overrideKey"+i;
            requestMap.put(key,overrideKey[i]);

            String value = "overrideValue"+i;
            requestMap.put(value,overrideValue[i]);
        }

        requestMap.put("mockDefaultMethodForce",mockDefaultMethodForce );
        requestMap.put("mockDefaultMethodJson", mockDefaultMethodJson);

        for (int i=0;i<mockMethodName.length;i++){
            String key1 = "mockMethodName"+i;
            requestMap.put(key1,mockMethodName[i]);

            String key2 = "mockMethodForce"+i;
            requestMap.put(key2,mockMethodForce[i]);

            String key3 = "mockMethodJson"+i;
            requestMap.put(key3,mockMethodJson[i]);
        }

        return requestMap;

    }
}
