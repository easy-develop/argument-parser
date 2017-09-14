package com.easy.argparse;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataClassValidator {
    private static final Logger logger = LoggerFactory.getLogger(DataClassValidator.class);
    
    private static final Set<Class<?>> ALLOWED_NON_PRIMITIVE_FIELD_TYPES = new HashSet<Class<?>>(Arrays.asList(new Class<?>[]
    {
        String.class,
        Boolean.class,
        Byte.class,
        Character.class,
        Short.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class
    }));
    
    private final Class<?> dataClass;

    public DataClassValidator(Class<?> dataClass) {
        this.dataClass = dataClass;
    }
    
    void validateVariableNames(List<UsageToken> usageTokens){
        for(UsageToken usageToken : usageTokens){
            String variableName = usageToken.getDataVariableName();
            logger.trace("Checking data class for variable: {}", variableName);
            try{
                Field field = dataClass.getDeclaredField(variableName);
                logger.trace("Found variable {} in the data class", variableName);
                validateFieldType(field.getType());
            }catch(NoSuchFieldException e){
                logger.warn("Got exception while looking for field in data class: {}", e.getMessage());
                throw new IllegalArgumentException("Field " + variableName + " not present in class " + dataClass.getCanonicalName() + 
                        " (" + e.getMessage() + ")");
            }catch(SecurityException e){
                logger.warn("Got exception while accessing data class for fields: {}", e.getMessage());
                throw new IllegalArgumentException("Restricted to access field " + variableName + " in class " + dataClass.getCanonicalName() + 
                        " (" + e.getMessage() + ")");
            }
        }
    }
    
    private void validateFieldType(Class<?> fieldType){
        logger.trace("Checking validity of field type: {}", fieldType.getCanonicalName());
        if(fieldType.isArray()){
            validateFieldType(fieldType.getComponentType());
        }else if(!fieldType.isPrimitive() && !ALLOWED_NON_PRIMITIVE_FIELD_TYPES.contains(fieldType)){
            throw new IllegalArgumentException("Field type " + fieldType.getCanonicalName() + " is not allowed");
        }
    }
}
