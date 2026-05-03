package com.burkhead.dvdstreaming.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

public class ConfigValues {

    private static HashMap<String, String> configValues = new HashMap<>();

    public static boolean getAllConfigValues(){
        try{
            File file = new File("src/main/resources/application.properties");
            Scanner inp = new Scanner(file);

            while(inp.hasNext()){

                //get next line
                String line = inp.nextLine();

                //remove white space
                line = line.replaceAll("\\s", "");

                //skip comment lines and lines without "="
                if(line.isEmpty() || line.charAt(0) == '#' || !line.contains("=")){
                    continue;
                }

                //split string on "=" and ensure theres only 2 halves
                String[] keyValue = line.split("=");
                if(keyValue.length > 2 || keyValue[0].isEmpty() || keyValue[1].isEmpty()){
                    continue;
                }

                //add new key value pair
                configValues.put(keyValue[0], keyValue[1]);

            }
        }
        catch (Exception e){ //TODO more specific error handling
            return false;
        }

        return true;
    }


    public static String getValue(String key){
        return configValues.get(key);
    }

    public static String getValueAsPath(String key){

        String temp = configValues.get(key);
        if(temp.charAt(temp.length() - 1) != '/')
            temp += "/";
        return temp;
    }


}
