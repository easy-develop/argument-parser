# argument-parser-easy
This is simple and lightweight project for parsing an array of strings into specified class. For example, let's say we want to parse the command line arguments entered by end user and store the relevant values into a class, so that we can retrieve the values from the class and use them the way we need. This task is usually done by directly reading the arguments and storing different values based on indices, which makes it a little bit cumbersome and fragile if the input pattern were to change. A java porting of GNU getopts is also available but that is not so straightforward to use. So, this project is for providing a simple to use API for parsing the command line arguments. 

Let's understand this through an example. Suppose, we are writing a utility which reads available log files and generates a statistics report for specified period of time. The end user can specify the **day**, **month**, **year** and **directories to search for log files** as command line arguments, in which **year** and **directories** are optional parameters. First, we create a Java class for containing the values specified by end user, something like this:
```java
package com.demo.argParse;

public class InputData {
	private int day;
	private String month;
	private int year;
	private String[] directoriesToSearch;
	
	public int getDay() {
		return day;
	}
	
	public void setDay(int day) {
		this.day = day;
	}
	
	public String getMonth() {
		return month;
	}
	
	public void setMonth(String month) {
		this.month = month;
	}
	
	public int getYear() {
		return year;
	}
	
	public void setYear(int year) {
		this.year = year;
	}
	
	public String[] getDirectoriesToSearch() {
		return directoriesToSearch;
	}
	
	public void setDirectoriesToSearch(String[] directoriesToSearch) {
		this.directoriesToSearch = directoriesToSearch;
	}
}
```

Then, we can do something like this to parse and retrieve the values:
```java
package com.demo.argParse;

import com.easy.argparse.ArgumentParser;

public class Main {
	public static void main(String[] args) {
		String inputUsage = "--day|-d day --mon|-m month [--year|-y year] [--dir|-ds directoriesToSearch]";
		ArgumentParser argParser = new ArgumentParser(inputUsage, InputData.class);
		InputData inputData = (InputData) argParser.parse(args);
		
		new StatsGenerator().generateStats(inputData);
	}
}
```
That's all we need to do, just specify the input pattern and data class, and get a new instance of data class with fields updated as per values available in the array of strings.

There are some things which should be noted here:

 - List item
 - X
