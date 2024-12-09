# SqlMan
A lightweight Data Access Library for Java

https://tawn33y.github.io/EasySQL/#demo
https://github.com/fast-sql/FastSQL
https://framework.ajaxjs.com/docs/aj/
https://github.com/lightweight-component/aj-data

https://sqlman.ajaxjs.com/


https://www.baeldung.com/jdbi
https://www.kancloud.cn/apachecn/zetcode-zh/1949996
https://www.jianshu.com/p/2d8e9550f650
https://blog.csdn.net/wjw465150/article/details/124778277
https://jdbi.org/#_introduction_to_jdbi_3

https://jueee.github.io/2020/10/2020-10-12-Apache%E6%95%B0%E6%8D%AE%E5%BA%93%E5%B7%A5%E5%85%B7commons-dbutils%E7%9A%84%E4%BD%BF%E7%94%A8/
https://github.com/apache/commons-dbutils/tree/master

Apache Commons DbUtils 库是一个相当小的一组类，它们被设计用来在没有资源泄漏的情况下简化 JDBC 调用处理，并且具有更简洁的代码。

由于 JDBC 资源清理非常繁琐且容易出错，因此 DBUtils 类有助于抽取出重复代码，以便开发人员只专注于与数据库相关的操作。
使用优点

    无资源泄漏 - DBUtils 类确保不会发生资源泄漏。
    清理和清除代码 - DBUtils 类提供干净清晰的代码来执行数据库操作，而无需编写任何清理或资源泄漏防护代码。
    Bean 映射 - DBUtils 类支持从结果集中自动填充 javabeans。

设计原则

    小 - DBUtils 库的体积很小，只有较少的类，因此易于理解和使用。
    透明 - DBUtils 库在后台没有做太多工作，它只需查询并执行。
    快速 - DBUtils 库类不会创建许多背景对象，并且在数据库操作执行中速度非常快。

