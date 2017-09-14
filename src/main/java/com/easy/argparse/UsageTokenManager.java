package com.easy.argparse;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This thread-safe class extracts and manages the mandatory and optional usage tokens from given usage expression
 * 
 * @author himanshu_shekhar
 */
public class UsageTokenManager {

    private static final Logger logger = LoggerFactory.getLogger(UsageTokenManager.class);

    private static List<UsageToken> getUsageTokens(String usageExpression) {
        // example: --day|-d DAY -time | -t TIME -f FILE
        logger.trace("Parsing ({}) for usage tokens", usageExpression);
        String regex = "[-]{1,2}([a-zA-Z0-9_]+)([ ]?\\|[ ]?[-]{1,2}([a-zA-Z0-9_]+))? ([a-zA-Z$_][a-zA-Z$_0-9]*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(usageExpression);

        List<UsageToken> usageTokens = new ArrayList<UsageToken>();

        while (matcher.find()) {
            String optionName = matcher.group(1);
            String optionAliasName = matcher.group(3);
            String dataVariableName = matcher.group(4);
            usageTokens.add(new UsageToken(optionName, optionAliasName, dataVariableName));
            logger.trace("Found usage token: option = {}, alias = {}, variable name = {}", optionName, optionAliasName, dataVariableName);
        }

        return usageTokens;
    }

    private final String usageExpression;
    private final Class<?> dataClass;
    private final Map<UsageToken, Method> setterMethodMap;

    private List<UsageToken> mandatoryUsageTokens;
    private List<UsageToken> optionalUsageTokens;

    /**
     * 
     * @param usageExpression The usage expression for the input format of command line arguments
     * @param dataClass The data class which will keep the values available in command line arguments
     */
    public UsageTokenManager(String usageExpression, Class<?> dataClass) {
        this.usageExpression = usageExpression;
        this.dataClass = dataClass;
        this.setterMethodMap = new ConcurrentHashMap<UsageToken, Method>();
        this.mandatoryUsageTokens = new ArrayList<UsageToken>();
        this.optionalUsageTokens = new ArrayList<UsageToken>();
    }

    /**
     * Initialize and update the data structures representing mandatory and optional usage tokens
     */
    public synchronized void initialize() {
        reset();
        
        UsageExpressionExtractor usageExpressionExtractor = new UsageExpressionExtractor(usageExpression);
        mandatoryUsageTokens = getUsageTokens(usageExpressionExtractor.getMandatoryExpression());
        optionalUsageTokens = getUsageTokens(usageExpressionExtractor.getOptionalExpression());
        updateSetterMethodMap(dataClass);
    }
    
    private void reset(){
        mandatoryUsageTokens.clear();
        optionalUsageTokens.clear();
        setterMethodMap.clear();
    }

    private void updateSetterMethodMap(Class<?> dataClass) {
        updateSetterMethodMap(dataClass, mandatoryUsageTokens);
        updateSetterMethodMap(dataClass, optionalUsageTokens);
    }

    private void updateSetterMethodMap(Class<?> dataClass, List<UsageToken> usageTokens) {
        for (UsageToken usageToken : usageTokens) {
            setterMethodMap.put(usageToken, usageToken.getSetterMethod(dataClass));
        }
    }

    /**
     * Validate the data class for mandatory and optional usage tokens
     * @throws IllegalArgumentException If data class is not valid
     */
    public synchronized void validateVariableNames() {
        DataClassValidator dataClassValidator = new DataClassValidator(dataClass);
        dataClassValidator.validateVariableNames(mandatoryUsageTokens);
        dataClassValidator.validateVariableNames(optionalUsageTokens);
    }

    /**
     * 
     * @param availableUsageTokens Set of usage options as available in the command line arguments
     * @return If any of the mandatory options is missing
     */
    public synchronized boolean isMissingMandatoryOption(Set<UsageToken> availableUsageTokens) {
        boolean missing = false;
        for (UsageToken usageToken : mandatoryUsageTokens) {
            if (!availableUsageTokens.contains(usageToken)) {
                missing = true;
                break;
            }
        }

        return missing;
    }

    /**
     * 
     * @param option The option as present in the usage expression, e.g. {@code m} in usage expression {@code -m minute [-s seconds]} is an option
     * @return Corresponding usage token
     * @throws IllegalArgumentException If corresponding usage token could not be found
     */
    public synchronized UsageToken findUsageToken(String option) {
        UsageToken foundUsageToken = findUsageToken(option, mandatoryUsageTokens);
        if (foundUsageToken == null) {
            foundUsageToken = findUsageToken(option, optionalUsageTokens);
        }
        if (foundUsageToken == null) {
            throw new IllegalArgumentException("No usage definition could be found for option (" + option + ")");
        }
        return foundUsageToken;
    }

    /**
     * 
     * @return If no valid usage tokens could be found in the given usage expression
     */
    public synchronized boolean noTokensAvailable() {
        return mandatoryUsageTokens.isEmpty() && optionalUsageTokens.isEmpty();
    }

    private UsageToken findUsageToken(String option, List<UsageToken> usageTokens) {
        UsageToken foundUsageToken = null;
        for (UsageToken usageToken : usageTokens) {
            if (option.equals(usageToken.getOptionName()) || option.equals(usageToken.getOptionAliasName())) {
                foundUsageToken = usageToken;
                break;
            }
        }

        return foundUsageToken;
    }

    /**
     * 
     * @param usageToken The usage token corresponding to the option
     * @return The setter method in specified data class corresponding to given usage token
     */
    public Method getSetterMethod(UsageToken usageToken) {
        return setterMethodMap.get(usageToken);
    }
}
