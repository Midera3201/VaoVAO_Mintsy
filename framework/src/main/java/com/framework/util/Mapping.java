package com.framework.util;

import java.lang.reflect.Method;

public class Mapping {

    private Class<?> controllerClass;
    private Method method;
    private boolean json;

    public Mapping(Class<?> controllerClass, Method method) {
        this(controllerClass, method, false);
    }

    public Mapping(Class<?> controllerClass, Method method, boolean json) {
        this.controllerClass = controllerClass;
        this.method = method;
        this.json = json;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public Method getMethod() {
        return method;
    }

    /**
     * true si la methode est annotee @ApiJson : le resultat sera
     * serialise en JSON au lieu d'etre traite comme un nom de vue.
     */
    public boolean isJson() {
        return json;
    }
}
