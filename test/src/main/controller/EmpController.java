package controller;

import com.framework.annotation.UrlMapping;
import com.framework.annotation.controller;

@controller
public class EmpController {
    @UrlMapping(url = "/emp/list/")
    public void empListe() {
    }

    @UrlMapping(url = "/emp/new")
    public void empNew() {
    }
    @UrlMapping(url = "/andrana")
    public void andrana() {
    }
}