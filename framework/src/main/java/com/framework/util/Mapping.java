package com.framework.util;

public class Mapping {
    private Class<?> controllerClass;
    private java.lang.reflect.Method method;

    public Mapping(Class<?> controllerClass, java.lang.reflect.Method method) {
        this.controllerClass = controllerClass;
        this.method = method;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public java.lang.reflect.Method getMethod() {
        return method;
    }
}
