[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/sqlman?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/sqlman)
![Java Version](https://img.shields.io/badge/Java-8-blue)
[![Javadoc](https://img.shields.io/badge/javadoc-1.6-brightgreen.svg?)](https://javadoc.io/doc/com.ajaxjs/sqlman)
![coverage](https://img.shields.io/badge/coverage-80%25-yellowgreen.svg?maxAge=2592000)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/lightweight-component/SqlMan)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![中文](https://img.shields.io/badge/lang-中文-red)](./README.zh-CN.md)

# 高可用分支、负载均衡

# SqlMan

SqlMan is a lightweight wrapper over JDBC. It is NOT an ORM but follows a SQL-first approach. It allows you to use pure SQL with IF/forEach and pass Map parameters for queries or executions. The results you receive are either 'isOk' indicators and 'Map' or Java Bean object(s). SqlMan enables fast CRUD database operations with ZERO dependencies, except for the JDK.

## Source code

[Github](https://github.com/lightweight-component/SqlMan) | [Gitcode](https://gitcode.com/lightweight-component/SqlMan)

## Link

[Web Site](https://sqlman.ajaxjs.com) | [Tutorials](https://sqlman.ajaxjs.com/docs/) | [Java Documents](https://javadoc.io/doc/com.ajaxjs/sqlman) | [Ask DeepWiki](https://deepwiki.com/lightweight-component/sqlman)

## Install

Runs on Java8+. Maven:

```xml

<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>sqlman</artifactId>
    <version>1.6</version>
</dependency>
```