package com.easy.argparse;

import static org.junit.Assert.*;
import org.junit.Test;

public class UsageExpressionExtractorTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void invalidExpressionIsThrownIfUnmatchedOpeningSquareBracket(){
        String expression = "-a val_a [-b val_b [ -c val_c ]";
        UsageExpressionExtractor tester = new UsageExpressionExtractor(expression);
        tester.getClass();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void invalidExpressionIsThrwonIfStraySquareBracket(){
        String expression = "[-a val_a] ] -b val_b";
        UsageExpressionExtractor tester = new UsageExpressionExtractor(expression);
        tester.getClass();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void invalidExpressionIsThrownIfNonSquareBracket(){
        String expression = "-a val_a (-b val_b)";
        UsageExpressionExtractor tester = new UsageExpressionExtractor(expression);
        tester.getClass();
    }
    
    @Test
    public void extractsMandatoryExpressionIfOptionalExpressionPresent(){
        String expression = "-a val_a [-b val_b] -c val_c";
        UsageExpressionExtractor tester = new UsageExpressionExtractor(expression);
        assertEquals("Mandatory expression could not be extracted when optional is present", "-a val_a -c val_c", tester.getMandatoryExpression());
    }
    
    @Test
    public void extractsMandatoryExpressionIfOptionalExpressionAbsent(){
        String expression = "-a val_a -b val_b";
        UsageExpressionExtractor tester = new UsageExpressionExtractor(expression);
        assertEquals("Mandatory expression could not be extracted if optional is absent", "-a val_a -b val_b", tester.getMandatoryExpression());
    }
    
    @Test
    public void extractsOptionalExpressionIfMandatoryExpressionPresent(){
        String expression = "[-a val_a] -b val_b [-c val_c]";
        UsageExpressionExtractor tester = new UsageExpressionExtractor(expression);
        assertEquals("Optional expression could not be extracted if mandatory is present", "-a val_a -c val_c", tester.getOptionalExpression());
    }
    
    @Test
    public void extractsOptionalExpressionIfMandatoryExpressionAbsent(){
        String expression = "[-a val_a -b val_b]";
        UsageExpressionExtractor tester = new UsageExpressionExtractor(expression);
        assertEquals("Optional expression could not be extracted if mandatory is absent", "-a val_a -b val_b", tester.getOptionalExpression());
    }
    
    @Test
    public void extractOptionalExpressionWithWhitespace(){
        String expression = "-a val_a [ -b val_b     ] -c val_c";
        UsageExpressionExtractor tester = new UsageExpressionExtractor(expression);
        assertEquals("Optional expression with whitespace could not be extracted", "-b val_b", tester.getOptionalExpression());
    }
}