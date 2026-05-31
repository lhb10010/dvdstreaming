package com.burkhead.dvdstreaming.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class JavaScriptController {


    //TODO get list automatically from templates
    String[] scripts = {"uploadVideo.js"};


    @GetMapping("/javascript/{script}")
    public String uploadSeriesPage(@PathVariable String script){

        String targetFile = "";
        for(int i = 0; i < scripts.length; i++){
            if(script.equals(scripts[i])){
                targetFile = scripts[i];
            }
        }

        return targetFile;

    }
}
