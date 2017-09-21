package com.easy.argparse;

import com.easy.core.utils.RegexUtil;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This thread safe class parses the command line arguments (as an array of {@code String}) and updates the data class
 * with available values
 * 
 * @author himanshu_shekhar
 */
public class FieldValueManager {

    private static final Logger logger = LoggerFactory.getLogger(FieldValueManager.class);

    private final Class<?> dataClass;
    private final UsageTokenManager usageTokenManager;
    private final String arrayDelimiter;
    private final Map<UsageToken, String> valueMap;

    /**
     * 
     * @param dataClass The data class whose instance will keep the available values
     * @param usageTokenManager Instance which provides usage tokens corresponding to usage expression
     * @param arrayDelimiter The delimiter used to separate elements in array
     */
    public FieldValueManager(Class<?> dataClass, UsageTokenManager usageTokenManager, String arrayDelimiter) {
        this.dataClass = dataClass;
        this.usageTokenManager = usageTokenManager;
        this.arrayDelimiter = arrayDelimiter;
        this.valueMap = new ConcurrentHashMap<UsageToken, String>();
    }

    /**
     * Parse available values and keep a map of values available for corresponding option in usage
     * 
     * @param args Array of {@code String} corresponding to command line arguments
     */
    public void updateAvailableValues(String[] args) {
        logger.trace("Parsing the arguments for values");
        
        for (int index = 0; index < args.length; index++) {
            String option = args[index];
            if(option.startsWith("-")){
                UsageToken usageToken = usageTokenManager.findUsageToken(option.replaceAll("[-]{1,2}", ""));
                updateValueMap(usageToken, args, index);
            }
        }
    }
    
    /**
     * 
     * @return A set of usage tokens for which values are available in the command line argument
     */
    public Set<UsageToken> getAvailableUsageTokens(){
        return valueMap.keySet();
    }
    
    private void updateValueMap(UsageToken usageToken, String[] args, int currentIndex){
        String value = "";
        if(needsValue(usageToken)){
            value = getValue(args, currentIndex);
        }
        valueMap.put(usageToken, value);
    }
    
    private String getValue(String[] args, int currentIndex){
        String value = "";
        try{
            value = args[currentIndex + 1];
        }catch(ArrayIndexOutOfBoundsException e){
            logger.warn("Got exception trying to get value at index {}: {}", currentIndex, e);
            throw new IllegalArgumentException("Missing value for option at index: " +  currentIndex);
        }
        
        if(value.startsWith("-")){
            throw new IllegalArgumentException("Missing value for option at index: " +  currentIndex);
        }
        
        return value;
    }
    
    /**
     * 
     * @param usageToken The usage token corresponding to an option in the usage expression
     * @return The value available in command line argument
     */
    public Object getArgValueObject(UsageToken usageToken) {
        String value = valueMap.get(usageToken);
        Object argValue;
        if (needsValue(usageToken)) {
            Field field = usageToken.getMappedField(dataClass);
            Class<?> fieldType = field.getType();
            if (fieldType.isEnum()) {
                argValue = Enum.valueOf((Class<Enum>)fieldType, value);
            } else if (fieldType.isArray()) {
                argValue = getArrayArgValue(value, fieldType);
            } else {
                argValue = getNonArrayArgValue(value, fieldType);
            }
        } else {
            argValue = true;
        }

        return argValue;
    }

    private Object getArrayArgValue(String value, Class<?> fieldType) {
        Class<?> arrayComponentFieldType = fieldType.getComponentType();
        String delimiter = RegexUtil.containsSpecialCharacter(arrayDelimiter) ? 
                RegexUtil.getSpecialCharactersEscaped(arrayDelimiter) : arrayDelimiter;
        String[] vals = value.split(delimiter);

        Object arr = java.lang.reflect.Array.newInstance(arrayComponentFieldType, vals.length);

        for (int index = 0; index < vals.length; index++) {
            try {
                Array.set(arr, index, getNonArrayArgValue(vals[index].trim(), arrayComponentFieldType));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Incorrect data format (" + e.getMessage() + ")");
            }
        }

        return arr;
    }

    private Object getNonArrayArgValue(String value, Class<?> fieldType) {
        Object argValue;

        if (fieldType == String.class) {
            argValue = value;
        } else if (fieldType == Byte.class || fieldType == byte.class) {
            argValue = Boolean.parseBoolean(value);
        } else if (fieldType == Character.class || fieldType == char.class) {
            argValue = value.charAt(0);
        } else if (fieldType == Short.class || fieldType == short.class) {
            argValue = Short.parseShort(value);
        } else if (fieldType == Integer.class || fieldType == int.class) {
            argValue = Integer.parseInt(value);
        } else if (fieldType == Long.class || fieldType == long.class) {
            argValue = Long.parseLong(value);
        } else if (fieldType == Float.class || fieldType == float.class) {
            argValue = Float.parseFloat(value);
        } else if (fieldType == Double.class || fieldType == double.class) {
            argValue = Double.parseDouble(value);
        } else {
            throw new IllegalArgumentException("Cannot convert (" + value + ") to appropriate data");
        }

        return argValue;
    }

    /*
    return value indicating whether the option in arguments required a corresponding value or not
    for example, in "-file FILE -verbose", there must be a FILE value after "-file" but "-verbose" does not expect any corresponding value after it
    In short, everything beside booleans will need a value
     */
    private boolean needsValue(UsageToken usageToken) {
        Class<?> fieldType = usageToken.getMappedField(dataClass).getType();
        return !(fieldType == boolean.class || fieldType == Boolean.class);
    }
}
