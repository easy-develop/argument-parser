# Examples
Below given examples are for demonstrating how the [argument-parser](https://github.com/easy-develop/argument-parser) library can be used in various scenarios. Each of the examples will have these:

 1. Usage expression
 2. Data Class
 3. Demo Driver program
 4. Console output

**Note**: To run the demo driver program, dependency jars must be in the `classpath`. I used below command to include the jars in `classpath`:
```txt
bash$ export CLASSPATH=.:~/.m2/repository/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar:~/.m2/repository/com/github/easy-develop/core-utils/1.0.0/core-utils-1.0.0.jar:~/.m2/repository/org/slf4j/slf4j-simple/1.7.25/slf4j-simple-1.7.25.jar
bash$
```
You can include the jars in whatever way suits you.
<hr>
## List of examples:
- [A simple case](#simple_case)
- [With optional arguments](#optional_args)
- [Using alias for options](#option_alias)
- [Array in command line argument](#array_val)
- [Array with user defined delimiter](#array_val_with_delimiter)

<hr>
<a name="simple_case"></a>
## A simple case
The usage expression is:
`-m minute -s seconds`

Data class is:
```java
package com.easy.argparse.demo;

public class ArgumentData {
    private int minute;
    private int seconds;

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }
}
```

The demo driver program is:
```java
package com.easy.argparse.demo;

import com.easy.argparse.ArgumentParser;

public class ArgParserDemo {
    public static void main(String[] args) {
        String usageExpression = "-m minute -s seconds";
        ArgumentParser argParser = new ArgumentParser(usageExpression, ArgumentData.class);
        ArgumentData argData = (ArgumentData) argParser.parse(args);
        System.out.println("Minute: " + argData.getMinute() + ", Seconds: " + argData.getSeconds());
    }
}
```

And the console output is:
```txt
bash$ java com.easy.argparse.demo.ArgParserDemo -m 30 -s 45
Minute: 30, Seconds: 45
```
<hr>
<a name="optional_args"></a>
## With optional arguments
The optional arguments can be specified within square brackets in the usage expression. Here is the usage expression:
`-m minute [ -s seconds ]`
The `seconds` is an  optional argument in this usage expression

The data class is:
```java
package com.easy.argparse.demo;

public class ArgumentData {
    private int minute;
    private int seconds;

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }
}
```

The demo driver program is:
```java
package com.easy.argparse.demo;

import com.easy.argparse.ArgumentParser;

public class ArgParserDemo {
    public static void main(String[] args) {
        String usageExpression = "-m minute -s seconds";
        ArgumentParser argParser = new ArgumentParser(usageExpression, ArgumentData.class);
        ArgumentData argData = (ArgumentData) argParser.parse(args);
        System.out.println("Minute: " + argData.getMinute() + ", Seconds: " + argData.getSeconds());
    }
}
```

And the console output is:
```txt
bash$ java com.easy.argparse.demo.ArgParserDemo -m 30
Minute: 30, Seconds: 0
bash$ java com.easy.argparse.demo.ArgParserDemo -m 30 -s 20
Minute: 30, Seconds: 20
```
**Note**: The value of `seconds` takes default value for `int` (i.e. `0`) when not specified in command line arguments
<hr>
<a name="option_alias"></a>
## Using alias for options
The options to be specified in command line arguments can have aliases defined through the usage expression. Here is the usage expression:
`-m|--min minute -s seconds`
This means that `minute` can be specified through option `-m` or `--min`

The data class is:
```java
package com.easy.argparse.demo;

public class ArgumentData {
    private int minute;
    private int seconds;

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }
}
```

The demo driver program is:
```java
package com.easy.argparse.demo;

import com.easy.argparse.ArgumentParser;

public class ArgParserDemo {
    public static void main(String[] args) {
        String usageExpression = "-m minute [ -s seconds ]";
        ArgumentParser argParser = new ArgumentParser(usageExpression, ArgumentData.class);
        ArgumentData argData = (ArgumentData) argParser.parse(args);
        System.out.println("Minute: " + argData.getMinute() + ", Seconds: " + argData.getSeconds());
    }
}
```

And the console output is:
```txt
bash$ java com.easy.argparse.demo.ArgParserDemo -s 20 -m 45
Minute: 45, Seconds: 20
bash$ java com.easy.argparse.demo.ArgParserDemo --min 45 -s 20
Minute: 45, Seconds: 20
```
Note that output with `-m` is same as that with `--min`. It should also be noted that values in command line arguments need not be as per order given in usage expression
<hr>
<a name="array_val"></a>
## Array in command line argument
A value in the command line argument can be specified to be an array. Here is the usage expression:
`-u userIds`

The data class is:
```java
package com.easy.argparse.demo;

public class ArgumentData {
    private int[] userIds;

    public int[] getUserIds() {
        return userIds;
    }

    public void setUserIds(int[] userIds) {
        this.userIds = userIds;
    }
}
```

The demo driver program is:
```java
package com.easy.argparse.demo;

import com.easy.argparse.ArgumentParser;

public class ArgParserDemo {
    public static void main(String[] args) {
        String usageExpression = "-u userIds";
        ArgumentParser argParser = new ArgumentParser(usageExpression, ArgumentData.class);
        ArgumentData argData = (ArgumentData) argParser.parse(args);
        System.out.println("Specified users are given below:");
        for(int id : argData.getUserIds()){
            System.out.println("User ID = [" + id + "]");
        }
    }
}
```

And the console output is:
```txt
bash$ java com.easy.argparse.demo.ArgParserDemo -u "1818, 9856, 7812"
Specified users are given below:
User ID = [1818]
User ID = [9856]
User ID = [7812]
```
It is to note that if not specified otherwise, elements of the array are supposed to be separated by `,` and whitespace around the array elements are automatically removed
<hr>

<a name="array_val_with_delimiter"></a>
## Array with user defined delimiter
We can specify the array elements to be separated by some delimiter other than `,`. Here is the usage expression:
`-u userIds`

The data class is:
```java
package com.easy.argparse.demo;

public class ArgumentData {
    private int[] userIds;

    public int[] getUserIds() {
        return userIds;
    }

    public void setUserIds(int[] userIds) {
        this.userIds = userIds;
    }
}
```

The demo driver program  is:
```java
package com.easy.argparse.demo;

import com.easy.argparse.ArgumentParser;

public class ArgParserDemo {
    public static void main(String[] args) {
        String usageExpression = "-u userIds";
        ArgumentParser argParser = new ArgumentParser(usageExpression, ArgumentData.class, "|");
        ArgumentData argData = (ArgumentData) argParser.parse(args);
        System.out.println("Specified users are given below:");
        for(int id : argData.getUserIds()){
            System.out.println("User ID = [" + id + "]");
        }
    }
}
```

And the console output is:
```txt
bash$ java com.easy.argparse.demo.ArgParserDemo -u "1818 | 9856 | 7812"
Specified users are given below:
User ID = [1818]
User ID = [9856]
User ID = [7812]
```
<hr>