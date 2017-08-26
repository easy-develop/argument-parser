package com.easy.argparse.util;

public class StringsUtil {
    private StringsUtil(){}
    
    public static String getWhitespaceNormalized(String text){
        return text.replaceAll("\\s+", " ").trim();
    }
}