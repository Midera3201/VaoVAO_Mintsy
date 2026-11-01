package controller;

import com.framework.annotation.ApiJson;
import com.framework.annotation.UrlMapping;
import com.framework.annotation.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sprint 11 : demonstration de l'instance unique d'un controleur.
 *
 * Le constructeur ne doit etre appele qu'une seule fois pendant la duree de
 * vie de l'application. Les deux routes ci-dessous partagent donc le meme
 * objet, le meme instanceId et le meme compteur d'appels.
 */
@controller
public class SingletonController {

    private static final AtomicInteger NOMBRE_INSTANCES = new AtomicInteger();

    private final String instanceId = UUID.randomUUID().toString();
    private final AtomicInteger nombreAppels = new AtomicInteger();

    public SingletonController() {
        int total = NOMBRE_INSTANCES.incrementAndGet();
        System.out.println("[Test] SingletonController construit : instance "
                + instanceId + " (total=" + total + ")");
    }

    @ApiJson
    @UrlMapping(url = "/api/singleton", method = "GET")
    public Map<String, Object> etat() {
        return construireReponse("etat");
    }

    @ApiJson
    @UrlMapping(url = "/api/singleton/autre", method = "GET")
    public Map<String, Object> autreMethode() {
        return construireReponse("autre");
    }

    private Map<String, Object> construireReponse(String route) {
        Map<String, Object> reponse = new LinkedHashMap<>();
        reponse.put("classe", getClass().getSimpleName());
        reponse.put("instanceId", instanceId);
        reponse.put("nombreInstances", NOMBRE_INSTANCES.get());
        reponse.put("nombreAppels", nombreAppels.incrementAndGet());
        reponse.put("route", route);
        return reponse;
    }
}
