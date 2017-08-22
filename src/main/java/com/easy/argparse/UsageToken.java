package com.easy.argparse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

final class UsageToken {

    private final String optionName;
    private final String optionAliasName;
    private final String dataVariableName;

    UsageToken(String optionName, String optionAliasName, String dataVariableName) {
        this.optionName = optionName;
        this.optionAliasName = optionAliasName;
        this.dataVariableName = dataVariableName;
    }
    
    String getOptionName() {
        return optionName;
    }

    String getOptionAliasName() {
        return optionAliasName;
    }

    String getDataVariableName() {
        return dataVariableName;
    }
    
    Field getMappedField(Class<?> dataClass){
        try{
            return dataClass.getDeclaredField(dataVariableName);
        }catch(NoSuchFieldException e){
            throw new IllegalArgumentException("Cannot find variable " + dataVariableName + " in calss " + dataClass.getCanonicalName() + 
                    " (" + e.getMessage() + ")");
        }catch(SecurityException e){
            throw new IllegalArgumentException("Restricted to find variable " + dataVariableName + " in class " + dataClass.getCanonicalName() + 
                    " (" + e.getMessage() + ")");
        }
    }
    
    Method getSetterMethod(Class<?> dataClass){
        String setterMethodName = getSetterMethodName();
        Method setterMethod;
        try{
            setterMethod = dataClass.getDeclaredMethod(setterMethodName, getMappedField(dataClass).getType());
            if(!setterMethod.isAccessible()){
                setterMethod.setAccessible(true);
            }
        }catch(NoSuchMethodException  e){
            throw new IllegalArgumentException("Cannot find method " + setterMethodName + " in class " + dataClass.getCanonicalName() + 
                    " (" + e.getMessage() + ")");
        }catch(SecurityException e){
            throw new IllegalArgumentException("Restricted to access method " + setterMethodName + " in class " + dataClass.getCanonicalName() + 
                    " (" + e.getMessage() + ")");
        }
        
        return setterMethod;
    }
    
    private String getSetterMethodName() {
        StringBuilder setterMethodName = new StringBuilder();
        setterMethodName.append("set");
        setterMethodName.append(Character.toUpperCase(dataVariableName.charAt(0)));
        setterMethodName.append(dataVariableName.substring(1, dataVariableName.length()));

        return setterMethodName.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.optionName);
        hash = 59 * hash + Objects.hashCode(this.optionAliasName);
        hash = 59 * hash + Objects.hashCode(this.dataVariableName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UsageToken other = (UsageToken) obj;
        
        return other.getDataVariableName().equals(dataVariableName) &&
                (other.getOptionName().equals(optionName) || other.getOptionName().equals(optionAliasName));
    }
}