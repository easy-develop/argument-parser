package com.easy.argparse;

import com.easy.argparse.util.Logger;
import com.easy.argparse.util.RegexUtil;
import java.lang.reflect.Array;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class FieldValueManager {

    private static final Logger logger = Logger.getLogger(FieldValueManager.class);

    private final Class<?> dataClass;
    private final UsageTokenManager usageTokenManager;
    private final String arrayDelimiter;
    private final Map<UsageToken, String> valueMap;

    FieldValueManager(Class<?> dataClass, UsageTokenManager usageTokenManager, String arrayDelimiter) {
        this.dataClass = dataClass;
        this.usageTokenManager = usageTokenManager;
        this.arrayDelimiter = arrayDelimiter;
        this.valueMap = new ConcurrentHashMap<UsageToken, String>();
    }

    void updateAvailableValues(String[] args) {
        logger.trace("Parsing the arguments for values");
        
        for (int index = 0; index < args.length; index++) {
            String option = args[index];
            if (option.startsWith("-")) {
                option = option.replaceAll("[-]{1,2}", "");
                UsageToken usageToken = usageTokenManager.findUsageToken(option);
                String value = "";
                if (needsValue(usageToken)) {
                    try {
                        value = args[++index];
                        if (value.startsWith("-")) {
                            throw new IllegalArgumentException("No value specified for (" + usageToken.getDataVariableName() + ")");
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new IllegalArgumentException("Value for option (" + option + ") could not found (" + e.getMessage() + ")");
                    }
                }
                valueMap.put(usageToken, value);
            }
        }
    }
    
    Set<UsageToken> getAvailableUsageTokens(){
        return valueMap.keySet();
    }

    Map<UsageToken, String> getValueMap(String[] args) {
        logger.trace("Obtaining the argument value map");
        for (int index = 0; index < args.length; index++) {
            String option = args[index];
            if (option.startsWith("-")) {
                option = option.replaceAll("[-]{1,2}", "");
                UsageToken usageToken = usageTokenManager.findUsageToken(option);
                String value = "";
                if (needsValue(usageToken)) {
                    try {
                        value = args[++index];
                        if (value.startsWith("-")) {
                            throw new IllegalArgumentException("No value specified for (" + usageToken.getDataVariableName() + ")");
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new IllegalArgumentException("Value for option (" + option + ") could not found (" + e.getMessage() + ")");
                    }
                }
                valueMap.put(usageToken, value);
            }
        }

        return valueMap;
    }

    Object getArgValueObject(UsageToken usageToken) {
        String value = valueMap.get(usageToken);
        Object argValue;
        if (needsValue(usageToken)) {
            Class<?> fieldType = usageToken.getMappedField(dataClass).getType();
            if (fieldType.isArray()) {
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
                Array.set(arr, index, getNonArrayArgValue(vals[index], arrayComponentFieldType));
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
