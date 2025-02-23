[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/sqlman?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/sqlman)
[![Javadoc](https://img.shields.io/badge/javadoc-1.0-brightgreen.svg?)](https://javadoc.io/doc/com.ajaxjs/sqlman)
![coverage](https://img.shields.io/badge/coverage-80%25-yellowgreen.svg?maxAge=2592000)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![QQ群](https://framework.ajaxjs.com/static/qq.svg)](https://shang.qq.com/wpa/qunwpa?idkey=3877893a4ed3a5f0be01e809e7ac120e346102bd550deb6692239bb42de38e22)

# SqlMan

SqlMan is a lightweight wrapper over JDBC. It is NOT an ORM but follows a SQL-first approach. It allows you to use pure SQL with IF/forEach and pass Map parameters for queries or executions. The results you receive are either 'isOk' indicators and 'Map' or Java Bean object(s). SqlMan enables fast CRUD database operations with ZERO dependencies, except for the JDK.

## Source code

[Github](https://github.com/core-lib/sqlman) | [Gitcode](https://gitcode.com/lightweight-component/SqlMan)

## Link

[Web Site](https://sqlman.ajaxjs.com) | [Tutorials](https://javadoc.io/doc/com.ajaxjs/sqlman) | [Java Documents](https://javadoc.io/doc/com.ajaxjs/sqlman) 

## Install

Runs on Java11+. Maven:

```xml

<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>sqlman</artifactId>
    <version>1.0</version>
</dependency>
```