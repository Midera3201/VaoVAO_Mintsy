package com.framework.util;

import java.util.HashMap;
import java.util.Map;

public class Model {
    private Map<String, Object> data = new HashMap<>();

    public void setAttribute(String key, Object value) {
        data.put(key, value);
    }

    public Object getAttribute(String key) {
        return data.get(key);
    }

    public Map<String, Object> getData() {
        return data;
    }
}
