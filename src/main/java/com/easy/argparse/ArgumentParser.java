package com.easy.argparse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This thread-safe class parses the command line arguments provided in form of an array of Strings and instantiates data class such that parsed values can
 * be obtained from the created instance of data class <br>
 * For example, consider below: <br>
 * <b>Expected format: </b> {@code -m minute [-s seconds]} <br>
 * <b>Command line arguments: </b> {@code -s 45 -m 20} <br>
 * Then, created instance of data class can be used to obtain {@code 20} for minute and {@code 45} for seconds <br>
 * Follow below rules to use this class: <br>
 * <ul>
 *    <li> 
 *         Usage expression (like {@code -m minute [-s seconds]} can contain as many mandatory and optional expressions as required but optional
 *         expressions cannot be nested
 *    </li>
 *    <li>
 *         Any other bracket (curly braces or round brackets) must not be present in usage format expression
 *    </li>
 *    <li>
 *         A variable with name same as specified in usage expression must be present in the data class, along with corresponding setter method
 *    </li>
 *    <li>
 *         The data type of variable corresponding to name in usage expression must be either of below: <br>
 *         <ol>
 *            <li>String</li>
 *            <li>Primitive data type ({@code boolean, byte, char, short, int, long, float, double})</li>
 *            <li>Wrapper to primitive data type ({@code Boolean, Byte, Character, Short, Integer, Long, Float, Double})</li>
 *            <li>An array of any of the above types</li>
 *            <li>Enum class</li>
 *         </ol>
 *    </li>
 *    <li>
 *         An optional alias can be defined. For example, {@code -m|--min minute} is a valid usage expression and {@code -m} or {@code --min} can be
 *         used in corresponding command line arguments
 *    </li>
 *    <li>
 *         The delimiter to identify different elements of an array can be any valid {@code String}
 *    </li>
 *    <li>
 *         Usage expression is case sensitive
 *    </li>
 *    <li>
 *         All options except those corresponding to boolean type except a value to be specified in the command line arguments
 *    </li>
 *    <li>
 *         If anything is wrong, e.g. usage expression is not valid etc., then an IllegalArgumentException with appropriate message will be thrown
 *    </li>
 * </ul>
 * 
 * @author himanshu_shekhar
 */
public class ArgumentParser {
    private static final Logger logger = LoggerFactory.getLogger(ArgumentParser.class);
    
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
    public synchronized Object parse(String[] args){
        initializeAndValidate();
        
        fieldValueManager.updateAvailableValues(args);
        
        if(usageTokenManager.isMissingMandatoryOption(fieldValueManager.getAvailableUsageTokens())){
            throw new IllegalArgumentException("Missing mandatory option from the arguments");
        }
        Object dataHolderObject = getDataClassInstance();
        
        for (UsageToken usageToken : fieldValueManager.getAvailableUsageTokens()) {
            invokeSetterMethod(usageToken, dataHolderObject);
        }

        return dataHolderObject;
    }
    
    private Object getDataClassInstance(){
        Object dataHolderObject;
        try {
            dataHolderObject = dataClass.newInstance();
        } catch (InstantiationException e) {
            logger.warn("Got exception while creating instance of data class: {}", e);
            throw new IllegalArgumentException("Cannot create instance of " + dataClass.getCanonicalName() + " (" + e.getMessage() + ")");
        }catch(IllegalAccessException e){
            logger.warn("Got exception while accessing data class for instantiation: {}", e);
            throw new IllegalArgumentException("Cannot access " + dataClass.getCanonicalName() + " to create instance (" + e.getMessage() + ")");
        }
        
        return dataHolderObject;
    }
    
    private void invokeSetterMethod(UsageToken usageToken, Object dataClassInstance){
        Method setterMethod = usageTokenManager.getSetterMethod(usageToken);
        
        logger.trace("Invoking setter method: {}", setterMethod.getName());
        
        try {
            setterMethod.invoke(dataClassInstance, fieldValueManager.getArgValueObject(usageToken));
        } catch (InvocationTargetException e) {
            logger.warn("Got exception while invoking setter method: {}", e);
            throw new IllegalArgumentException("Cannot invoke method " + dataClass.getCanonicalName() + "." + setterMethod.getName()
                    + " (" + e.getMessage() + ")");
        } catch (IllegalAccessException e) {
            logger.warn("Got exception while accessing data class for instantiation: {}", e);
            throw new IllegalArgumentException("Cannot access " + dataClass.getCanonicalName() + "." + setterMethod.getName()
                    + " (" + e.getMessage() + ")");
        }
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
