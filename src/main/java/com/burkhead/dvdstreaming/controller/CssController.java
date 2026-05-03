package com.burkhead.dvdstreaming.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CssController {

    @GetMapping("/style.css")
    public String frontPage(){
        return "style.css";
    }
}
