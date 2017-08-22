package com.easy.argparse;

import com.easy.argparse.util.Logger;
import com.easy.argparse.util.StringsUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class UsageExpressionExtractor {
    private static final Logger logger = Logger.getLogger(UsageExpressionExtractor.class);
    
    private final String usageExpression;
    private final List<IndexRange> optionalExpressionIndexRanges;

    UsageExpressionExtractor(String usageExpression) {
        this.usageExpression = usageExpression;
        this.optionalExpressionIndexRanges = Collections.unmodifiableList(getOptionalExpressionIndexRanges(usageExpression));
        
        // There is nothing more to do for object instantiation, so object is NOT in an inconsistent state
        // So, it is safe to invoke an instance method here
        if(hasIllegalBracket()){
            throw new IllegalArgumentException("Usage (" + usageExpression + ") has incorrect brackets");
        }
    }
    
    /*
    Look for index of '[', and then index of ']' starting after previously found '['. Create IndexRange object using indices of 
    '[' and ']' and add it to result list. Repeat this process with index starting after
    that of previously found ']' until no more '[' is left. If no ']' is found after a '[', raise
    an exception
    
    NOTE: The IndexRange object contains indices of '[' and ']' also, so take this into consideration while extracting the
    expression string
    */
    private List<IndexRange> getOptionalExpressionIndexRanges(String expression) {
        logger.debug("Obtaining optional expressions from: {}", expression);
        
        int squareBracketStartIndex = 0;
        int squareBracketEndIndex;
        
        List<IndexRange> indexRanges = new ArrayList<IndexRange>();

        while ((squareBracketStartIndex = expression.indexOf('[', squareBracketStartIndex)) != -1) {
            squareBracketEndIndex = expression.indexOf(']', squareBracketStartIndex);
            
            if (squareBracketEndIndex == -1) {
                throw new IllegalArgumentException("No matching square bracket at index = " + squareBracketEndIndex + " in (" + expression + ")");
            }
            
            // Check if there is another '[' between square brackets, i.e. something like [-a val_a [ -b val_b]
            if(expression.substring(squareBracketStartIndex + 1, squareBracketEndIndex).contains("[")){
                throw new IllegalArgumentException("Nested opening square bracket in (" + expression + ")");
            }
            
            logger.trace("Found optional expression between indices: {} and {}", squareBracketStartIndex, squareBracketEndIndex);
            indexRanges.add(new IndexRange(squareBracketStartIndex, squareBracketEndIndex));
            
            squareBracketStartIndex = squareBracketEndIndex;
        }
        
        return indexRanges;
    }
    
    /*
    Checks if the usage expression contains any bracket other than square bracket or any nested square bracket
    */
    private boolean hasIllegalBracket(){
        // Only square bracket is expected to be present in the expression
        if(usageExpression.contains("(") || usageExpression.contains(")") || usageExpression.contains("{") || usageExpression.contains("}")){
            return true;
        }
        
        boolean illegal = false;
        
        for(int index=0; index < usageExpression.length(); index++){
            char currentChar = usageExpression.charAt(index);
            boolean isSquareBracket = currentChar == '[' || currentChar == ']';
            if(isSquareBracket && !isPartOfOptionalExpression(index, optionalExpressionIndexRanges)){
                illegal = true;
                break;
            }
        }
        
        return illegal;
    }
    
    /*
    Return "false" immediately if these two conditions are met:
    1. If there are no optional expressions, all of the expressions must be mandatory
    2. if index in argument is lesser than first starting index this index in argument cannot be part of optional expression
    
    Now, simply search for the index in argument in given optional expression index ranges and return "true" if found
    It is to note here that optional expression indices consist of '[' and ']' indices
    
    And yes, linear searching is not efficient, but since we do not expect large expression, it should not be much of a concern
    */
    private boolean isPartOfOptionalExpression(int index, List<IndexRange> optionalExpressionIndexRanges){
        
        if(optionalExpressionIndexRanges.isEmpty()){
            return false;
        }else if(index < optionalExpressionIndexRanges.get(0).startIndex){
            return false;
        }
        
        boolean partOfOptionalExpression = false;
        
        for(IndexRange indexRange: optionalExpressionIndexRanges){
            if(index >= indexRange.startIndex && index <= indexRange.endIndex){
                partOfOptionalExpression = true;
                break;
            }
        }
        
        return partOfOptionalExpression;
    }
    
    /**
     * @return part of the specified usage expression representing mandatory arguments
     * For example, if usage expression is <b>-f file [-t time] -d directory</b>, then <b>-f file -d directory</b> will be returned
     */
    String getMandatoryExpression(){
        StringBuilder mandatoryExpression = new StringBuilder();
        for(int index=0; index < usageExpression.length(); index++){
            if(!isPartOfOptionalExpression(index, optionalExpressionIndexRanges)){
                mandatoryExpression.append(usageExpression.charAt(index));
            }
        }
        
        return StringsUtil.getWhitespaceNormalized(mandatoryExpression.toString());
    }
    
    /**
     * @return part of the specified usage expression representing optional arguments
     * For example, if usage expression is <b>-f file [-t time] -d directory</b>, then <b>-t time</b> will be returned
     */
    String getOptionalExpression(){
        StringBuilder optionalExpression = new StringBuilder();
        for(IndexRange indexRange : optionalExpressionIndexRanges){
            optionalExpression.append(getOptionalExpressionPart(usageExpression, indexRange)).append(" ");
        }
        
        return StringsUtil.getWhitespaceNormalized(optionalExpression.toString());
    }
    
    /*
    Simply extract the part of expression using start and end indices of index range object. Just take into consideration the
    fact that:
    1. start and end indices include that of '[' and ']', so these square brackets should be excluded from the expression
    2. "String.substring(startIndex, endIndex)" gives part of string from startIndex inclusive to endIndex exclusive, so
    use startIndex + 1 and endIndex (+1 since '[' is not to be included, ']' is already excluded since end index is exclusive)
    */
    private String getOptionalExpressionPart(String expression, IndexRange indexRange){
        return expression.substring(indexRange.startIndex + 1, indexRange.endIndex);
    }
    
    private class IndexRange{
        private final int startIndex;
        private final int endIndex;

        IndexRange(int startIndex, int endIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }
}