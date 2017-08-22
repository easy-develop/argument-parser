package com.easy.argparse.util;

public class StringsUtil {
    public static String getWhitespaceNormalized(String text){
        return text.replaceAll("\\s+", " ").trim();
    }
}
