# JSqlParser 表达式解析器设计备忘录

## 背景

动态 SQL 的 `<if test="...">` 需要根据参数 Map 判断是否输出一个 XML 分支。旧实现直接使用 Spring EL：表达式能力很强，但语法与 SQL 模板不一致，并且条件语言与 Spring 运行时耦合。

本次目标不是执行 SQL，而是为模板条件提供一个小而明确的求值器：

- 使用 SQL 风格的逻辑、比较和算术语法；
- 支持括号和必要的函数扩展；
- 只读取调用方提供的参数；
- 不允许模板任意调用 Java 方法、访问类或执行子查询；
- 可安全地被同一个 XML 模板并发使用。

与 XML DOM 的整体方案和 `<if>/<else>/<forEach>` 渲染规则，参见[动态 SQL XML 模板：DOM 与 `SqlTemplate` 设计备忘录](动态%20SQL%20XML%20模板：DOM%20与%20`SqlTemplate`%20设计备忘录.md)。

## 备选方案

| 方案 | 优点 | 缺点 | 结论 |
| --- | --- | --- | --- |
| 保留 Spring EL | 功能完整，已有实现 | 与 Spring 绑定；语法不是 SQL；能力范围过大 | 不作为新模板的默认实现 |
| 手写字符串解析 | 依赖少，初期代码少 | 优先级、括号、错误定位、扩展函数都需自行实现 | 不采用 |
| 直接使用 JavaCC | 可自定义完整语法 | 需维护词法、语法、AST 与错误处理；学习和维护成本高 | 不采用 |
| JSqlParser + 自定义 Visitor | 已有成熟 SQL 表达式 AST；支持括号与运算符；只需实现受限求值 | JSqlParser 只解析，仍需自行定义运行语义 | 采用 |

JSqlParser 的职责仅是把文本转换为 AST；它不会读取参数 Map，也不会返回布尔结果。因此不能仅靠升级依赖替代 Spring EL，必须实现 Visitor。

## 最终方案

入口类为 `JSqlParserExpressionEvaluator`，实现 `SqlTemplateExpressionEvaluator`。它负责：

1. 用 `CCJSqlParserUtil.parseCondExpression()` 解析 `test`；
2. 按原始表达式字符串缓存只读 AST；
3. 为每次求值创建新的 `JSqlParserExpressionVisitor`；
4. 要求根节点最终返回 `Boolean`。

```text
<if test="enabled = TRUE AND tier + bonus >= 2">
                 │
                 ▼
JSqlParserExpressionEvaluator
  ├─ AST 缓存（ConcurrentHashMap）
  └─ parseCondExpression()
                 │
                 ▼
JSqlParserExpressionVisitor + 当前参数 Map
                 │
                 ▼
Boolean ──► DOM 渲染器选择 if / else 分支
```

Visitor 是同包外部类，而非 `JSqlParserExpressionEvaluator` 的内部类。解析、缓存、公开 API 与 AST 求值职责由此分离，代码更易阅读和测试。

## 支持的语法与语义

| 类别 | 支持内容 | 示例 |
| --- | --- | --- |
| 布尔逻辑 | `AND`、`OR`、`NOT`、括号 | `(enabled = TRUE AND tier >= 2) OR admin = TRUE` |
| 比较 | `=`、`<>`、`>`、`>=`、`<`、`<=` | `status = 'OPEN'` |
| 空值 | `IS NULL`、`IS NOT NULL` | `deletedAt IS NULL` |
| 集合范围 | `IN`、`NOT IN`、`BETWEEN`、`NOT BETWEEN` | `tier BETWEEN 1 AND 3` |
| 算术 | `+`、`-`、`*`、`/`、`DIV`、`%`、正负号 | `(tier + bonus) >= 2` |
| 自定义函数 | 显式注册的函数 | `isBlank(name)` |

表达式中的普通标识符对应参数 Map 的键。例如 `tier` 读取 `params.get("tier")`。`TRUE` 和 `FALSE` 是布尔字面量；带表前缀的标识符，例如 `user.tier`，不支持。

在 XML 属性中，比较符号需要遵守 XML 转义：

```xml
<if test="tier &gt;= 2 AND score &lt; 100">
    ...
</if>
```

### 数值

所有 `Number` 在计算或数值比较时转换为 `BigDecimal`。这不是为了扩大模板表达式能力，而是为了让小数、除法和跨数值类型比较具有确定且精确的语义；模板作者无需直接使用 `BigDecimal`。

除数为零、非数值参与算术、或不可比较的两种类型都会抛出 `IllegalArgumentException`。

### 空值和错误

- `IS NULL` / `IS NOT NULL` 是空值判断的唯一明确写法；
- 普通比较、`IN`、`BETWEEN` 遇到 `null` 时结果为 `false`；
- 引用不存在的参数是模板错误，会抛出异常，而不是静默当作 `null`；
- 顶层表达式必须是布尔值，例如单独写 `tier + 1` 会被拒绝。

这些规则避免了 Spring EL 中“缺失属性、隐式类型转换或异常被吞掉”造成的不可预测分支。

## 自定义函数

函数通过 `Map<String, SqlTemplateFunction>` 注入。函数名大小写不敏感，构造时会统一为小写；没有注册的函数立即报错。

```java
Map<String, SqlTemplateFunction> functions = new HashMap<>();
functions.put("isBlank", (arguments, params) -> {
    String value = (String) arguments.get(0);
    return value == null || value.trim().isEmpty();
});

SqlTemplateExpressionEvaluator evaluator =
        new JSqlParserExpressionEvaluator(functions);
```

函数接收已求值的参数列表和当前参数 Map，可以返回布尔值、数值或字符串，再参与外层表达式。这里使用显式 Java 函数对象，而不按模板给出的类名或方法名反射调用；这样 API 清晰，也避免模板文本获得任意 Java 调用能力。

## 明确不支持的 AST

Visitor 只覆写允许节点。其他 JSqlParser AST 节点不会被默认放行，而会以 `Unsupported template expression` 失败，包括：

- 子查询、`EXISTS`、`SELECT`；
- `CASE WHEN`、窗口函数、聚合函数和数据库专有语法；
- JDBC `?` 参数、命名 JDBC 参数；
- 属性链、表字段引用、赋值和变量；
- 未注册函数。

这是模板条件引擎，而不是 SQL 执行器。表达式解析绝不会连接数据库或把表达式拼入待执行 SQL。

## 并发与缓存

`JSqlParserExpressionEvaluator` 用 `ConcurrentHashMap` 缓存解析后的 AST。AST 在求值期间只读；每一次 `evaluate()` 都新建 Visitor，Visitor 内保存的中间 `result` 不会跨线程或跨请求共享。

参数 Map 由调用方传入，求值器只读取它；循环局部变量由 XML DOM 渲染器提供独立作用域。函数实现自身如持有可变状态，则由注册方负责线程安全。

## 测试范围

当前测试覆盖：

- 多层逻辑、算术与括号；
- `IN`、`BETWEEN`、`IS NULL`；
- 自定义函数和大小写无关的函数名；
- 缺失参数、未注册函数和不支持的 AST；
- 通过 `SqlXmlDomTemplate` 的 `<if>` 集成。

后续扩展新的 AST 节点前，应先明确其参数类型、空值、短路和错误语义，再增加 Visitor 分支与对应的正反向测试；不要仅因 JSqlParser 能解析某个节点就默认支持它。

