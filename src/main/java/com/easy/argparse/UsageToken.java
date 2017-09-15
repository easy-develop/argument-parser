package com.easy.argparse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This thread-safe class represents a usage token present in the usage expression. For example, usage expression {@code -m minute [-s seconds]} has
 * two usage tokens: one corresponding to {@code -m minute} and another one corresponding to {@code -s seconds}
 * 
 * @author himanshu_shekhar
 */
public class UsageToken {
    private static final Logger logger = LoggerFactory.getLogger(UsageToken.class);
    
    private final String optionName;
    private final String optionAliasName;
    private final String dataVariableName;

    /**
     * 
     * @param optionName The option switch, e.g. {@code m} is an option name in usage expression {@code -m minute [-s seconds]}
     * @param optionAliasName The alias if any for the option, e.g. {@code min} is an alias in usage expression {@code -m|--min minute [-s seconds]}
     * @param dataVariableName The name corresponding to variable in data class, e.g. {@code minute} is data variable name in usage expression
     * {@code -m minute [-s seconds]}
     */
    public UsageToken(String optionName, String optionAliasName, String dataVariableName) {
        this.optionName = optionName;
        this.optionAliasName = optionAliasName;
        this.dataVariableName = dataVariableName;
    }
    
    /**
     * 
     * @return The name of option switch
     */
    public String getOptionName() {
        return optionName;
    }

    /**
     * @return The alias name for the option switch
     */
    public String getOptionAliasName() {
        return optionAliasName;
    }

    /**
     * 
     * @return The name of corresponding variable name in the data class
     */
    public String getDataVariableName() {
        return dataVariableName;
    }
    
    /**
     * 
     * @param dataClass The data class which will keep the values available in command line arguments
     * @return Reference to corresponding Field in the data class
     * @throws IllegalArgumentException If corresponding field could not be found
     */
    public Field getMappedField(Class<?> dataClass){
        try{
            return dataClass.getDeclaredField(dataVariableName);
        }catch(NoSuchFieldException e){
            logger.warn("Got exception while looking for field {} in the data class: {}", dataVariableName, e);
            throw new IllegalArgumentException("Cannot find variable " + dataClass.getName() + ":" + dataVariableName, e);
        }catch(SecurityException e){
            logger.warn("Got exception while accessing data class for fields: {}", e);
            throw new IllegalArgumentException("Restricted to find " + dataClass.getName() + ":" + dataVariableName, e);
        }
    }
    
    /**
     * 
     * @param dataClass The data class which will keep the values available in command line arguments
     * @return Reference to corresponding setter method in the data class
     * @throws IllegalArgumentException If corresponding setter method could not be found
     */
    public Method getSetterMethod(Class<?> dataClass){
        String setterMethodName = getSetterMethodName();
        Method setterMethod;
        try{
            setterMethod = dataClass.getDeclaredMethod(setterMethodName, getMappedField(dataClass).getType());
            if(!setterMethod.isAccessible()){
                setterMethod.setAccessible(true);
            }
        }catch(NoSuchMethodException  e){
            logger.warn("Got exception looking for method in the data class: {}", e);
            throw new IllegalArgumentException("Cannot find method " + dataClass.getName() + ":" + setterMethodName, e);
        }catch(SecurityException e){
            logger.warn("Got exception accessing method in data class: {}", e);
            throw new IllegalArgumentException("Restricted to access method " + dataClass.getName() + ":" + setterMethodName, e);
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
        hash = 59 * hash + (optionName != null ? optionName.hashCode() : 0);
        hash = 59 * hash + (optionAliasName != null ? optionAliasName.hashCode() : 0);
        hash = 59 * hash + (dataVariableName != null ? dataVariableName.hashCode() : 0);
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