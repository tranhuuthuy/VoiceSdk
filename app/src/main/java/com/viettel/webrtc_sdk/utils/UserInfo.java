package com.viettel.webrtc_sdk.utils;

import java.util.HashMap;
import java.util.Map;

public class UserInfo {
    private Map<String, Object> properties;

    public UserInfo() {
        properties = new HashMap<>();
    }

    public UserInfo(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
