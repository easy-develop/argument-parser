package com.easy.argparse;

import com.easy.argparse.util.Logger;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class can be used for parsing the command line arguments (or any array of {@code String}s) into specified class <br>
 * For example, suppose we have a usage like {@code -m minute [-s seconds]}, then this class can be used to parse {@code minute and seconds} from <br>
 * the given array of {@code String}s and update the specified class representing these values <br>
 * <br>
 * As input, this class expects usage expression (like {@code -m minute [-s seconds]}), data class and array delimiter (optional) <br>
 * As output, it provides a new instance of data holding class with available values set appropriately <br><br>
 * 
 * Consider below things before using this library: <br>
 * <b><tt>Usage Expression Format</tt></b>
 * <ul>
 *    <li>
 *        Can contain mandatory as well as optional expressions
 *    </li>
 *    <li>
 *        Optional expressions must be within square bracket, example: {@code [-s seconds]}
 *    </li>
 *    <li>
 *        Nested square brackets are not allowed, example: {@code [-s seconds [-S milliseconds]]} is NOT allowed
 *    </li>
 *    <li>
 *        No other bracket other than square bracket is allowed, example: {@code (-m minute)} is NOT allowed
 *    </li>
 *    <li>
 *        An unmatched square bracket is illegal, example: {@code -m minute ] [-s seconds]} is NOT allowed
 *    </li>
 *    <li>
 *       An alias to the argument switch can be specified separated by {@code |} character, example: {@code --sec|-s seconds}
 *    </li>
 *    <li>
 *       Order in which values are provided is not relevant, example: {@code -m 20 -s 45} and {@code -s 45 -m 20} will give same results
 *    </li>
 *    <li>
 *       If an invalid usage expression is specified, {@code IllegalArgumentException} with appropriate message will be thrown
 *    </li>
 * </ul>
 * 
 * <br>
 * 
 * <b><tt>Data Class</tt></b>
 * <ul>
 *    <li>
 *       Must contain the variables specified in usage expression format, example: if usage is {@code -m minute [-s seconds]}, then data class
 *       must contain {@code minute} and {@code seconds} fields
 *    </li>
 *    <li>
 *       Allowed data types for the variables are:
 *       <ol>
 *          <li>
 *             Primitive data types: {@code boolean, byte, char, short, int, long, float, double}
 *          </li>
 *          <li>
 *             Wrapper to the primitive data types: {@code Boolean, Byte, Character, Short, Integer, Long, Float, Double}
 *          </li>
 *          <li>
 *             String
 *          </li>
 *          <li>
 *             Array of any of the above data types
 *          </li>
 *       </ol>
 *    <li>
 *       Must contain setter method for variables specified in usage expression with appropriate argument, example: if we have {@code int minute} as
 *       field, then we must have {@code setMinute(int)} method in the data class
 *    </li>
 *    <li>
 *       The default constructor must be accessible
 *    </li>
 * </ul>
 * 
 * <br>
 * 
 * <b><tt>Array Delimiter</tt></b>
 * <ul>
 *    <li>
 *       Any string
 *    </li>
 * </ul>
 * 
 * <br>
 * <b><tt>Input data</tt></b>
 * <ul>
 *    <li>
 *       The input data must be an array of {@code String} values conforming to the specified usage expression, example: for usage expression
 *       {@code --min|-m minute [--verbose|-v verbose]} with data class having fields {@code int minute, int seconds,
 *       boolean verbose}, below input data values are valid: <br>
 *       {@code --min 23} <br>
 *       {@code -m 23 --verbose} <br>
 *       {@code -m 23 -v} <br>
 *       It is to note that every option except that for boolean (verbose in our example is boolean) needs to have a corresponding value
 *    </li>
 *    <li>
 *       All the mandatory option (and corresponding value, if applicable) must be present
 *    </li>
 *    <li>
 *       Value for an option must be in proper format
 *    </li>
 *    <li>
 *       An {@code IllegalArgumentException} with appropriate error message is thrown if anything wrong is found with the input data
 *    </li>
 * </ul>
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
            logger.warn("Got exception while creating instance of data class: {}", e.getMessage());
            throw new IllegalArgumentException("Cannot create instance of " + dataClass.getCanonicalName() + " (" + e.getMessage() + ")");
        }catch(IllegalAccessException e){
            logger.warn("Got exception while accessing data class for instantiation: {}", e.getMessage());
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
            logger.warn("Got exception while invoking setter method: {}", e.getMessage());
            throw new IllegalArgumentException("Cannot invoke method " + dataClass.getCanonicalName() + "." + setterMethod.getName()
                    + " (" + e.getMessage() + ")");
        } catch (IllegalAccessException e) {
            logger.warn("Got exception while accessing data class for instantiation: {}", e.getMessage());
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
