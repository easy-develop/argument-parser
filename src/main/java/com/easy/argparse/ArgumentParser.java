package com.easy.argparse;

import com.easy.argparse.util.Logger;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class can be used for parsing the command line arguments (or any array of {@code String}s) into specified class <br>
 * For example, suppose we have a usage like {@code -m minute [-s seconds]}, then this class can be used to parse {@code minute and seconds} from <br>
 * the given array of {@code String}s and update the specified class representing these values <br>
 * <br>
 * As input, this class expects usage expression (like {@code -m minute [-s seconds]}), data holding class and array delimiter (optional) <br>
 * As output, it provides a new instance of data holding class with available values set appropriately
 * 
 * @author himanshu_shekhar
 */
public class ArgumentParser {
    private static final Logger logger = Logger.getLogger(ArgumentParser.class);
    
    private final Class<?> dataClass;
    private final UsageTokenManager usageTokenManager;
    private final FieldValueManager fieldValueManager;
   
    /**
     * 
     * @param usageExpression The expression representing format in which command line arguments will be specified
     * For example: {@code --min|-m minute [--sec|-s seconds]}
     * 
     * @param dataHolderClass The class which will contain the values specified in command line arguments. This class must have variables specified in
     * the usageExpression, corresponding setterMethod and default constructor
     * For example: if usageExpression is {@code --min|-m minute [--sec|-s seconds]}, the dataHolderClass must have {@code minute} and {@code seconds}
     * as variables and {@code setMinute} and {@code setSeconds} methods and default constructor
     */
    public ArgumentParser(String usageExpression, Class<?> dataHolderClass){
        this(usageExpression, dataHolderClass, ",");
    }
    
    /**
     * 
     * @param usageExpression As described above
     * @param dataHolderClass As described above
     * @param arrayDelimiter The delimiter text which will be used to determine the array specified in command line argument
     */
    public ArgumentParser(String usageExpression, Class<?> dataHolderClass, String arrayDelimiter){
        this.dataClass = dataHolderClass;
        this.usageTokenManager = new UsageTokenManager(usageExpression, dataHolderClass);
        this.fieldValueManager = new FieldValueManager(dataHolderClass, usageTokenManager, arrayDelimiter);
    }
    
    /**
     * 
     * @param args The values representing command line arguments (or any String array for that sake)
     * @return A new instance of dataHolderClass specified in constructor with values available in {@code args} set appropriately
     */
    synchronized public Object parse(String[] args){
        initializeAndValidate();
        
        fieldValueManager.updateAvailableValues(args);
        
        if(usageTokenManager.isMissingMandatoryOption(fieldValueManager.getAvailableUsageTokens())){
            throw new IllegalArgumentException("Missing mandatory option from the arguments");
        }
        Object dataHolderObject;
        try {
            dataHolderObject = dataClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Cannot create instance of " + dataClass.getCanonicalName() + " (" + e.getMessage() + ")");
        }catch(IllegalAccessException e){
            throw new IllegalArgumentException("Cannot access " + dataClass.getCanonicalName() + " to create instance (" + e.getMessage() + ")");
        }
        for (UsageToken usageToken : fieldValueManager.getAvailableUsageTokens()) {
            Object argVal = fieldValueManager.getArgValueObject(usageToken);
            Method setterMethod = usageTokenManager.getSetterMethod(usageToken);
            logger.trace("Invoking setter method: {}", setterMethod.getName());
            try {
                setterMethod.invoke(dataHolderObject, argVal);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException("Cannot invoke method " + dataClass.getCanonicalName() + "." + setterMethod.getName() + 
                        " (" + e.getMessage() + ")");
            }catch(IllegalAccessException e){
                throw new IllegalArgumentException("Cannot access " + dataClass.getCanonicalName() + "." + setterMethod.getName() + 
                        " (" + e.getMessage() + ")");
            }
        }

        return dataHolderObject;
    }
    
    private void initializeAndValidate(){
        initialize();
        validate();
    }
    
    private void initialize(){
        usageTokenManager.initialize();
    }
    
    private void validate(){
        if(usageTokenManager.noTokensAvailable()){
            throw new IllegalArgumentException("No valid arguments found in usage expression");
        }
        
        usageTokenManager.validateVariableNames();
    }
}
