# argument-parser-easy

## Summary
This is simple and lightweight library for parsing an array of strings into specified class. For example, let's say we want to parse the command line arguments entered by end user and store the relevant values into a class, so that we can retrieve the values from the class and use them the way we need. Developers usually do it by checking the indices and convert values at certain index to certain variable, but this makes it a little bit cumbersome and fragile if the input pattern were to change. A java porting of GNU getopts is also available but that is not so straightforward to use. So, this project is for providing a simple to use API for parsing the command line arguments. 

## Requirements

 - Java 1.6 or above

## How To Use
Let's understand this through an example. Suppose, we are writing a utility which expects the end user to specify a set of directories and a time period. The end user can specify the `day`, `month`, year and `directoriesToSearch` as command line arguments, in which `year` and `directoriesToSearch` are optional parameters. First, we create a Java class for containing the values specified by end user, something like this:
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
		String usage = "--day|-d day --mon|-m month [--year|-y year][--dir|-ds directoriesToSearch]";
		ArgumentParser argParser = new ArgumentParser(usage, InputData.class);
		InputData inputData = (InputData) argParser.parse(args);
		showData(inputData);
		
		new StatsGenerator().generateStats(inputData);
	}
	
	private static void showData(InputData inputData) {
		System.out.println("Day = " + inputData.getDay() + ", month = " +
				inputData.getMonth() + ", year = " + inputData.getYear());
		
		String[] directoriesToSearch = inputData.getDirectoriesToSearch();
		if(directoriesToSearch == null) {
			System.out.println("no array obtained");
		}
		if(directoriesToSearch != null && directoriesToSearch.length > 0) {
			System.out.println("Target directories are >>>");
			for(String dir : directoriesToSearch){
				System.out.println(dir);
			}
		}
	}
}
```
That's all we need to do, just specify the input pattern and data class, and get a new instance of data class with fields updated as per values available in the array of strings. To use this library, add the JAR file available in [Releases](https://github.com/shekhar-himanshu/argument-parser-easy/releases) section to your classpath.

Here is the output of a sample run using this library:

    bash $ java com.demo.argParse.Main -d 20 -m 8 -ds "dir-A, dir-B" -y 2017
    Day = 20, month = 8, year = 2017
    Target directories are >>>
    dir-A
    dir-B
    bash $

Refer to javadoc in [Releases](https://github.com/shekhar-himanshu/argument-parser-easy/releases) section for details on the API.
