[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/sqlman?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/sqlman)
![Java Version](https://img.shields.io/badge/Java-8-blue)
[![Javadoc](https://img.shields.io/badge/javadoc-1.8-brightgreen.svg?)](https://javadoc.io/doc/com.ajaxjs/sqlman)
![coverage](https://img.shields.io/badge/coverage-80%25-yellowgreen.svg?maxAge=2592000)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![询问 DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/lightweight-component/aj-mcp)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![QQ群](https://framework.ajaxjs.com/static/qq.svg)](https://shang.qq.com/wpa/qunwpa?idkey=3877893a4ed3a5f0be01e809e7ac120e346102bd550deb6692239bb42de38e22)

# SqlMan

SqlMan 是一个轻量级的 JDBC 封装工具。它**不是**一个 ORM 框架，而是采用 SQL 优先的策略。它允许你使用纯 SQL，并支持 IF/forEach 等逻辑控制，同时可以通过 Map 参数传递查询或执行语句。返回的结果可以是“Map”或 Java Bean 对象。SqlMan 依赖极少，代码紧凑精巧，统一的 API 简单易用。

## 源代码

[GitHub](https://github.com/lightweight-component/SqlMan) | [Gitcode](https://gitcode.com/lightweight-component/SqlMan)

## 链接

[官方网站](https://sqlman.ajaxjs.com) | [教程](https://sqlman.ajaxjs.com/docs/) | [Java 文档](https://javadoc.io/doc/com.ajaxjs/sqlman) | [Ask DeepWiki](https://deepwiki.com/lightweight-component/sqlman)

## 安装

运行环境：Java 8 及以上版本。

### Maven 依赖

在你的 `pom.xml` 文件中添加以下依赖项来引入 SqlMan：

```xml

<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>sqlman</artifactId>
    <version>1.8</version>
</dependency>
```