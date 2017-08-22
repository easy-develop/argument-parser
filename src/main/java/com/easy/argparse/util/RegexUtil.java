package com.easy.argparse.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegexUtil {
    
    private static final List<Character> REGEX_SPECIAL_CHARACTERS = new ArrayList<Character>(Arrays.asList(new Character[]
    {
        '[', 
        '\\', 
        '^', 
        '$', 
        '.', 
        '|', 
        '?', 
        '*', 
        '+', 
        '(', 
        ')'
    }));
    
    public static boolean containsSpecialCharacter(String text) {
        boolean hasSpecialChar = false;

        for (char specialCharacter : REGEX_SPECIAL_CHARACTERS) {
            if (text.contains("" + specialCharacter)) {
                hasSpecialChar = true;
                break;
            }
        }

        return hasSpecialChar;
    }
    
    public static String getSpecialCharactersEscaped(String delimiter){
        StringBuilder escapedDelimiter = new StringBuilder();
        for(char currentChar : delimiter.toCharArray()){
            if(REGEX_SPECIAL_CHARACTERS.contains(currentChar)){
                escapedDelimiter.append("\\");
            }
            escapedDelimiter.append(currentChar);
        }
        
        return escapedDelimiter.toString();
    }
}
