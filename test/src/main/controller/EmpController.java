package controller;

import com.framework.annotation.UrlMapping;
import com.framework.annotation.controller;

@controller
public class EmpController {
    @UrlMapping(url = "/emp/list/", method = "GET")
    public void empListe() {
    }

    @UrlMapping(url = "/emp/list/", method = "POST")
    public void empListePost() {
    }

    @UrlMapping(url = "/emp/new")
    public void empNew() {
    }
    @UrlMapping(url = "/andrana")
    public void andrana() {
    }
}