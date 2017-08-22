package com.easy.argparse;

import com.easy.argparse.ArgumentParser;
import static org.junit.Assert.*;
import org.junit.Test;

public class ArgumentParserTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentIsThrownIfEmptyUsageExpression(){
        ArgumentParser tester = new ArgumentParser("", ArgumentDataEmpty.class);
        tester.parse(new String[]{});
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentIsThrownIfMissingMandatoryOption(){
        String usage = "-i intVal [ -s stringVal ]";
        ArgumentParser tester = new ArgumentParser(usage, ArgumentDataWithString.class);
        ArgumentDataWithString data = (ArgumentDataWithString) tester.parse(new String[]{"-s", "path_to_file"});
        data.getClass();
    }
    
    @Test
    public void parsesDataIfMissingOptionalArgument(){
        String usage = "-i intVal [-s stringVal]";
        ArgumentParser tester = new ArgumentParser(usage, ArgumentDataWithString.class);
        ArgumentDataWithString data = (ArgumentDataWithString) tester.parse(new String[]{"-i", "81823"});
        assertEquals("Incorrect data parsed", 81823, data.getIntVal());
    }
    
    @Test
    public void parsesDataIfOnlyOptionalArguments(){
        String usage = "[-s stringVal]";
        ArgumentParser tester = new ArgumentParser(usage, ArgumentDataWithString.class);
        ArgumentDataWithString data = (ArgumentDataWithString) tester.parse(new String[]{"-s", "path_to_file"});
        assertEquals("Cannot parse data if only optional arguments are present", "path_to_file", data.getStringVal());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentIsThrownIfDataClassHasNoMatchingVariable(){
        String usage = "-i integerVal [-s stringVal]";
        ArgumentParser tester = new ArgumentParser(usage, ArgumentDataWithString.class);
        ArgumentDataWithString data = (ArgumentDataWithString) tester.parse(new String[]{"-i", "81823"});
        data.getClass();
    }
    
    @Test
    public void parsesDataIfOptionAliasIsUsed(){
        String usage = "--num | -n intVal [--str | -s stringVal]";
        ArgumentParser tester = new ArgumentParser(usage, ArgumentDataWithString.class);
        ArgumentDataWithString data = (ArgumentDataWithString) tester.parse(new String[]{"-n", "10", "--str", "path_to_file"});
        assertEquals("Cannot parse string with aliased arguments", "path_to_file", data.getStringVal());
        data = (ArgumentDataWithString) tester.parse(new String[]{"--num", "8818", "--str", "yet_another_path"});
        assertEquals("Cannot parse integer with aliased arguments", 8818, data.getIntVal());
        assertEquals("Cannot parse string with aliased arguments", "yet_another_path", data.getStringVal());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentIsThrownIfUnsupportedDataType(){
        String usage = "-m map";
        ArgumentParser tester = new ArgumentParser(usage, ArgumentDataWithComplexType.class);
        ArgumentDataWithComplexType data = (ArgumentDataWithComplexType) tester.parse(new String[]{"-m", "{a => apple}"});
        data.getClass();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentIsThrownIfIncorrectDataType(){
        String usage = "-i intVal [-s stringVal]";
        ArgumentParser tester = new ArgumentParser(usage, ArgumentDataWithString.class);
        ArgumentDataWithString data = (ArgumentDataWithString) tester.parse(new String[]{"-i", "89.7F"});
        data.getClass();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentIsThrownIfMissingValue(){
        String usage = "-i intVal -s stringVal";
        ArgumentParser tester = new ArgumentParser(usage, ArgumentDataWithString.class);
        ArgumentDataWithString data = (ArgumentDataWithString) tester.parse(new String[]{"-i", "-s", "some_string"});
        data.getClass();
    }
    
    @Test
    public void parsesDataIfPrimitivesOnly(){
        String usage = "-i intVal [-s shortVal -l longVal] -c charVal -f floatVal";
        ArgumentParser tester = new ArgumentParser(usage, ArgumentDataPrimitivesOnly.class);
        ArgumentDataPrimitivesOnly data = (ArgumentDataPrimitivesOnly) tester.parse("-i 189 -l 9877 -c T -f 89.23".split(" "));
        assertEquals("Integer not parsed correctly with primitives only data class", 189, data.getIntVal());
        assertEquals("Long not parsed correctly with primitives only data class", 9877, data.getLongVal());
        assertEquals("Character not parsed correctly with primitives only data class", 'T', data.getCharVal());
        assertEquals("Float not parsed correctly with primitives only data class", 89.23,  data.getFloatVal(), 0.05);
    }
    
    @Test
    public void parsesDataIfPrimitivesArray(){
        String usage = "--nums intVals -s stringVal";
        ArgumentParser tester = new ArgumentParser(usage, ArgumentDataWithPrimitiveArray.class);
        ArgumentDataWithPrimitiveArray data = (ArgumentDataWithPrimitiveArray) tester.parse("--nums 10,89,2,7,56 -s some_text".split(" "));
        assertArrayEquals("Cannot parse integer array in argument", new int[]{10, 89, 2, 7, 56}, data.getIntVals());
    }
    
    @Test
    public void parsesDataIfPrimitivesArrayWithSpecifiedDelimiter(){
        String usage = "-n intVals";
        ArgumentParser tester = new ArgumentParser(usage, ArgumentDataWithPrimitiveArray.class, ":");
        ArgumentDataWithPrimitiveArray data = (ArgumentDataWithPrimitiveArray) tester.parse("-n 10:89:2:7:56:88:102".split(" "));
        assertArrayEquals("Cannot parse integer array with : as delimiter in arguments", new int[]{10, 89, 2, 7, 56, 88, 102}, data.getIntVals());
    }
    
    @Test
    public void parsesDataIfNonPrimitivesArray(){
        String usage = "-s stringVals";
        ArgumentParser tester = new ArgumentParser(usage, ArgumentDataWithStringArray.class);
        ArgumentDataWithStringArray data = (ArgumentDataWithStringArray) tester.parse("-s hello,world,how,are,you".split(" "));
        assertArrayEquals("Cannot parse string array in argument", new String[]{"hello", "world", "how", "are", "you"}, data.getStringVals());
    }
    
    @Test
    public void parsesDataIfNonPrimitivesAarrayWithSpecifiedDelimiter(){
        String usage = "-s stringVals";
        ArgumentParser tester = new ArgumentParser(usage, ArgumentDataWithStringArray.class, ":");
        ArgumentDataWithStringArray data = (ArgumentDataWithStringArray) tester.parse("-s how:are:you:?".split(" "));
        assertArrayEquals("Cannot parse string array with delimiter in argument", new String[]{"how", "are", "you", "?"}, data.getStringVals());
    }
    
    @Test
    public void parsesDataIfArrayWithSpecialCharacterAsDelimiter(){
        String usage = "-n intVals";
        String[] delimiters = new String[]{"[", "\\", "^", "$", ".", "|", "?", "*", "+", "(", ")"};
        for(String delimiter : delimiters){
            ArgumentParser tester = new ArgumentParser(usage, ArgumentDataWithPrimitiveArray.class, delimiter);
            String valueString = "10" + delimiter + "89" + delimiter + "35" + delimiter;
            ArgumentDataWithPrimitiveArray data = (ArgumentDataWithPrimitiveArray) tester.parse(("-n " + valueString).split(" "));
            assertArrayEquals("Cannot parse integer array with special characters as delimiter", new int[]{10, 89, 35}, data.getIntVals());
        }
    }
    
    @Test
    public void parsesDataIfArrayDelimiterContainsSpecialCharacter(){
        String usage = "-n intVals";
        String delimiter = "<?>";
        ArgumentParser tester = new ArgumentParser(usage, ArgumentDataWithPrimitiveArray.class, delimiter);
        String valueString = "10" + delimiter + "89" + delimiter + "35" + delimiter;
        ArgumentDataWithPrimitiveArray data = (ArgumentDataWithPrimitiveArray) tester.parse(("-n " + valueString).split(" "));
        assertArrayEquals("Cannot parse integer array with special characters as delimiter", new int[]{10, 89, 35}, data.getIntVals());
    }
}
