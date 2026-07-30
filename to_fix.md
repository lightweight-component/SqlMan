# SqlMan 待修复问题

更新时间：2026-07-30

## 验证结论

用户重新安装了修正后的 `ajaxjs-util 1.3.5`，其中已经包含源码所需的 `Methods.execute(...)` 重载。该依赖兼容问题已经解决。

项目没有设置或提高 Maven 版本。当前使用系统已有的 Maven 3.9.16，并显式指定 JDK 17。项目 `pom.xml` 只覆盖父 POM 的 `skipTests=false`，没有指定新的 Surefire 版本。

工作区实际执行结果：

```text
Tests run: 56, Failures: 6, Errors: 0, Skipped: 0
```

其中 50 个测试通过，6 个测试失败。

## P0：构建和测试配置

### 1. 系统 Maven 默认绑定 JDK 26，Lombok 1.18.34 无法正常处理

当前 `mvn -version` 显示 Maven 使用 JDK 26。默认编译时 Lombok getter、setter 和 `@Slf4j` 字段均未生成；显式开启注解处理后又出现：

```text
ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

JDK 17 下 Lombok 可以正常处理，随后只剩 `Methods.execute(...)` 编译错误。

建议方案：

1. 项目构建先固定为 JDK 17。
2. Maven Compiler Plugin 使用 `<release>8</release>`，替代 `source/target=8`。
3. 如果必须支持 JDK 26 构建，升级到明确支持该 JDK 的 Lombok，并配置 `annotationProcessorPaths` 和 `proc=full` 后重新验证。

这里的问题是 Maven 进程使用的 **JDK 版本**，不是 Maven 自身版本。当前测试已通过 `JAVA_HOME` 固定使用 JDK 17。

### 2. Surefire 从父 POM 继承了固定的 `skipTests=true`

effective POM 中 `maven-surefire-plugin 3.5.2` 的 execution 和全局 configuration 都是：

```xml
<skipTests>true</skipTests>
```

即使执行 `mvn -DskipTests=false test`，仍然输出 `Tests are skipped`。这会造成构建成功但测试从未运行。

已在本项目 `pom.xml` 覆盖 Surefire 配置，并沿用父 POM 管理的插件版本：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <configuration>
                <skipTests>false</skipTests>
            </configuration>
        </plugin>
    </plugins>
</build>
```

修复后应以 Surefire 的实际测试数量作为验证依据。

## P1：新增回归测试发现的源码问题

### 3. 复用 `Action` 时，空参数不会清除旧参数

位置：`src/main/java/com/ajaxjs/sqlman/Action.java:100`

失败测试：

```text
TestActionState.emptyParametersClearParametersFromPreviousUse
```

`setParams()` 仅在参数非空时赋值。先设置参数再调用 `setParams()`，旧数组仍保留，后续 SQL 可能绑定上一次操作的参数。

建议方案：每次调用都先清除 `this.params`；有参数时再处理模板 Map 和剩余参数。应明确空参数最终保存为 `null` 还是空数组，并保持一致。

### 4. 关闭连接失败时 ThreadLocal 没有移除

位置：`src/main/java/com/ajaxjs/sqlman/JdbcConnection.java:184`

失败测试：

```text
TestJdbcConnectionLifecycle.removesThreadLocalEvenWhenCloseFails
```

`closeDb(getConnection())` 抛异常后不会执行 `CONNECTION.remove()`，线程池线程会继续持有失效连接。

建议方案：

```java
Connection conn = getConnection();
try {
    closeDb(conn);
} finally {
    CONNECTION.remove();
}
```

保留关闭异常，同时保证线程状态被清理。

### 5. 总记录数为零时，分页结果的 `list` 仍为 null

位置：`src/main/java/com/ajaxjs/sqlman/crud/page/PageQuery.java:36`

失败测试：

```text
TestPageQuery.returnsEmptyListWhenQueryHasNoRows
```

越过最后一页已经返回空 List，但总记录数为零时没有设置 `list`，两种空结果语义不一致。

建议方案：创建 `PageResult` 后立即把 `list` 初始化为 `Collections.emptyList()`，有记录时再替换为查询结果。

### 6. 空 INSERT 实体没有被拒绝

位置：`src/main/java/com/ajaxjs/sqlman/sqlgenerator/Entity2WriteSql.java:69`

失败测试：

```text
TestEntity2WriteSql.rejectsInsertWithoutWritableValues
```

空 Map、全部为 null 的 Bean、全部被 `@Transient` 排除的 Bean 都没有可写字段。当前代码仍执行 `deleteCharAt()` 并产生损坏 SQL，而不是给出明确异常。

建议方案：先收集字段和值；字段为空时抛出 `IllegalArgumentException`，消息包含操作类型、表名和实体类型。

### 7. UPDATE 实体只有 ID 时没有被拒绝

位置：`src/main/java/com/ajaxjs/sqlman/sqlgenerator/Entity2WriteSql.java:98`

失败测试：

```text
TestEntity2WriteSql.rejectsUpdateWithoutValuesOtherThanId
```

排除 ID 后没有 SET 字段，当前仍删除 SQL 构造器最后一个字符并继续拼接 WHERE，生成损坏 SQL。

建议方案：在追加 WHERE 之前确认至少存在一个更新字段；为空时抛出 `IllegalArgumentException`。

### 8. SQL 注入分析器放过子查询和 UNION

位置：

- `src/main/java/com/ajaxjs/sqlman/util/sqlinjectionanalyzer/SqlInjectionAnalyzer.java:76`
- `src/main/java/com/ajaxjs/sqlman/util/sqlinjectionanalyzer/SqlInjectionAnalyzer.java:87`

失败用例：

```sql
select * from dc_device where id in (select id from other)
select * from dc_device UNION select name from other
```

`visit(SubSelect)` 的检测逻辑被完全注释，UNION/SetOperationList 也没有相应限制。

建议方案：

1. 先确定安全策略：是否一律禁止子查询和 UNION，还是只允许受控结构。
2. 若保持现有测试契约，应在 AST visitor 中显式拒绝顶层 UNION 和 WHERE 子查询。
3. 不要在 `check()` 中调用 `printStackTrace()`；正常的拒绝结果不应污染测试和应用日志。
4. 当前 Javadoc 写“true 表示攻击”，但实现和测试实际是 `true=允许、false=拒绝`。为兼容现有调用，建议保留返回语义并修正文档；如需反转语义，应新增命名明确的方法，避免直接破坏 API。

## 已完成的测试模块修正

- `BaseTest` 改为每个测试方法独立创建、重置和关闭 H2 数据库，消除测试顺序和跨类污染。
- 恢复原本整类注释的 `TestXml`，覆盖 XML 加载、SQL ID、动态 `<if>` 和占位符。
- `TestPrintRealSql`、`TestSnowflakeId` 和 SQL 注入测试由打印输出改为真实断言。
- 修正越过最后一页时错误期待 `totalCount=0` 的旧断言；总记录数应保持真实值。
- 修正测试辅助类对旧版 `ajaxjs-util Methods` API 的调用。
- SQL 注入测试改用 `assertAll`，保证一次运行能报告所有漏检用例。
- 新增 Action 参数状态、连接关闭异常、零记录分页和空实体 SQL 回归测试。

## 建议修复顺序

1. 保持测试使用 JDK 17；如需 JDK 26 构建，再单独升级并验证 Lombok。
2. 修复 Action 参数残留和 ThreadLocal 清理。
3. 统一分页空列表语义。
4. 拒绝没有可写字段的 INSERT/UPDATE。
5. 明确 SQL 注入分析器的返回契约和禁止规则，再修复子查询/UNION 漏检。
6. 在工作区重新执行完整测试，目标为 `Failures: 0, Errors: 0, Skipped: 0`。
