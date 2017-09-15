## Table of contents
- [Overview](#overview)
- [Feature Highlights](#feature_highlights)
- [Short Description](#short_description)
- [Prerequisites](#prerequisites)
- [Getting started](#getting_started)
- [Support](#support)
- [License](#license)

<a name="overview"></a>
## Overview
An easy to use and lightweight library for parsing the command line arguments. Supports specifying optional arguments. Values available in command line argument are used to create an instance for given class, which can further be used to retrieve the values.

<a name="feature_highlights"></a>
## Feature Highlights
- Can specify optional arguments
- Instance of given class is created with available values which can be used as needed
- Alias to argument options can be specified
- A value in command line argument can an array of elements separated by used defined delimiter
- Values in the command line argument are directly converted to supported data types instead of treating everything as `String`. See [API](https://easy-develop.github.io/argument-parser/1.0.0/apidocs/) for list of supported data types
- Is thread safe

<a name="short_description"></a>
## Short Description
Often, the developers find themselves rolling their own logic for handling the command line arguments and read the values in command line arguments directly, based on indices. This approach makes the code fragile and cumbersome since playing directly with the array indices is risky at best. And, if the format in which arguments are expected is to change, correcting this short in-house logic becomes yet more difficult to handle. There are some libraries which provide features for parsing command line arguments, but they are a bit difficult to use. So, here is a library which provides very simple APIs to perform this task in an efficient way. For details on API, see the [Javadoc](https://easy-develop.github.io/argument-parser/1.0.0/apidocs/)

<a name="prerequisites"></a>
## Prerequisites
- Java 1.5 or above

<a name="getting_started"></a>
## Getting started
First of all, include the jar for this library in your project. If you are using maven, add below dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>com.github.easy-develop</groupId>
    <artifactId>argument-parser</artifactId>
    <version>1.0.0</version>
</dependency>
```
If you are using any other build tool, have a look at [Maven Repository](http://mvnrepository.com/artifact/com.github.easy-develop/argument-parser/1.0.0) page on how to include the dependency

On how to use the APIs, have a look at the [examples](https://easy-develop.github.io/argument-parser/1.0.0/examples)

<a name="support"></a>
## Support
Please [open an issue](https://github.com/easy-develop/argument-parser/issues) if you have any suggestion or need an assistance

<a name="license"></a>
## License
This project is licensed under [MIT](https://github.com/easy-develop/properties/blob/master/LICENSE)