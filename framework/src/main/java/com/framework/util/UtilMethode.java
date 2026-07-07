package com.framework.util;

import java.util.Objects;

public class UtilMethode {
    private String url;
    private String httpMethod;

    public UtilMethode(String url, String httpMethod) {
        this.url = url;
        this.httpMethod = httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UtilMethode that = (UtilMethode) o;
        return Objects.equals(url, that.url) && Objects.equals(httpMethod, that.httpMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, httpMethod);
    }
}
