# 动态 SQL XML 模板：DOM 与 `SqlTemplate` 设计备忘录

## 背景与目标

SqlMan 目前将 XML 中的 `<sql>` 内容读取为字符串，缓存为 `sqlId -> String`，随后通过字符串扫描处理 `<if>`。该方式适用于最简单的条件片段，但无法可靠地处理嵌套标签，也没有接入实际执行链路的 `<else>` 支持。

本次设计的目标是让 XML 中的动态 SQL 能正确支持嵌套的 `<if>`、`<else>` 和 `<forEach>`，并保持：

- 模板加载一次、运行时重复渲染；
- JDBC 值仍经 `?` 参数绑定，不把值直接拼进 SQL；
- 条件表达式引擎可替换；
- 并发渲染不修改缓存的模板。

## 现状与问题

`SmallMyBatis.loadXML()` 使用 `XmlHelper.parseXML()` 找到 `<sql>` 后调用 `XmlHelper.getNodeText()`，因此 XML 的层级结构在加载时被压平为字符串。

随后 `generateIfBlock()` 使用 `indexOf("<if")`、`indexOf("</if>")` 和 `substring()` 处理条件块。这个模型的限制是：

- 第一个 `</if>` 会被视为当前 `<if>` 的结束标签，嵌套 `<if>` 会错配；
- 正式执行链路只保留或删除 `<if>` 内容，不会选择 `<else>` 分支；
- 标签格式、空白和错误位置只能靠字符串猜测；
- `<forEach>` 无法以可靠的作用域规则处理 `item`、`index` 和嵌套循环。

## 备选方案

| 方案 | 优点 | 缺点 | 结论 |
| --- | --- | --- | --- |
| 继续字符串扫描 | 改动最少 | 必须自己匹配嵌套标签和分支，错误处理脆弱 | 不采用 |
| XSLT | XML 原生支持条件和选择 | 使用 XPath/XSLT 语义，和 JDBC 参数绑定、应用参数 Map 的模型不匹配 | 不采用 |
| 缓存 XML DOM | 可直接利用 XML 的父子关系；实现量适中；天然处理嵌套 | 保留通用 DOM 节点和整份 `Document` 的内存开销 | 第一阶段采用 |
| 编译为自定义 `SqlTemplate` 节点树 | 不可变、占用更少、可在加载期校验和预解析表达式 | 需要额外的 DOM 到模板节点转换层 | 第二阶段演进方向 |

## 第一阶段决定：缓存只读 XML DOM

加载期将 XML 字符串解析成 DOM，并按 `id` 缓存对应的 `<sql>` `Element`。运行期不修改 DOM，而是以本次参数、局部 `StringBuilder` 和局部循环状态递归渲染。

```text
XML 文件
  ↓ 解析一次
DOM Document / <sql> Element 缓存
  ↓ 每次调用 render(id, params)
递归选择动态节点并生成 SQL 模板
  ↓
prepareSql()
  ↓
JDBC SQL（?）+ 有序参数
```

当前原型实现位于 `src/main/java/com/ajaxjs/sqlman/xml/SqlXmlDomTemplate.java`。

### 渲染规则

1. 根 `<sql>` 的文本节点和 CDATA 节点原样追加到输出。
2. 遇到 `<if test="...">`，先由表达式求值器判断外层条件。
3. 条件为真，递归渲染 `<else>` 之前的直接子节点；条件为假，递归渲染直接 `<else>` 的子节点。
4. 内层 `<if>` 只有在所属外层分支被选中后才会被判断。这是递归调用自然得到的语义，不需要显式栈。
5. `<else>` 只能是 `<if>` 的直接子节点；独立出现、多个直接 `<else>`、缺少 `test` 都属于模板错误。
6. `<forEach>` 遍历 `Iterable`、数组或 `Map` 条目，支持 `collection`、`item`、`index`、`open`、`separator`、`close` 属性。空集合不输出 `open/close`。

示例：

```xml
<sql id="find">
    SELECT * FROM user WHERE 1 = 1
    <if test="enabled">
        AND enabled = #{enabled}
        <if test="tier > 1">
            AND tier = #{tier}
        <else>
            AND tier &lt;= 1
        </else>
        </if>
    <else>
        AND enabled = false
    </else>
    </if>
</sql>
```

## 条件表达式解耦

DOM 模板不应决定使用 Spring EL、JSqlParser Visitor 或其他条件语言。`SqlXmlDomTemplate` 通过函数式接口 `SqlTemplateExpressionEvaluator` 注入条件求值器：

```java
SqlXmlDomTemplate template = new SqlXmlDomTemplate(xml,
        (expression, params) -> conditionVisitor.evaluate(expression, params));
```

接口接收 `<if>` 的 `test` 字符串和当前作用域参数 Map，返回是否渲染 true 分支。foreach 中的 `item` 和 `index` 也进入当前作用域。

当前接口边界使后续迁移成为可能：

- 短期可由 Spring EL 适配器维持旧表达式语法；
- 若改用 JSqlParser，可用 `parseCondExpression()` 获取 SQL 风格条件 AST，再由自定义 Visitor 对参数 Map 求值；
- 引擎应只开放比较、逻辑、空值、算术和明确注册的函数，不应让模板任意反射调用 Java 方法。

## `forEach` 参数绑定待决策

循环体中的 `#{item}` 必须最终变成多个彼此独立的 JDBC 参数。例如集合 `[4, 7, 9]` 需要三个绑定值，而原始参数 Map 通常只包含 `ids`。

当前原型为兼容现有 `prepareSql()`，会在渲染期生成内部唯一名称，并通过 `RenderedSql` 一并返回扩展后的参数 Map。这不是 XML 作者需要书写的语法，XML 中仍使用 `#{item}`。

该行为尚未确定为最终公开 API。后续需要在以下方向中明确选择：

1. 保留 `RenderedSql`，将 SQL 与扩展后的不可变参数快照一起返回；
2. `render()` 只返回字符串，但写入调用方传入的可修改 Map；
3. 扩展参数绑定器，使其能直接解析集合索引或循环绑定，而不暴露内部名称。

选择时应优先保证参数绑定正确、避免修改调用方 Map 和避免让内部占位符成为模板作者的约束。

## DOM 与自定义 `SqlTemplate` 的比较

| 维度 | 缓存 DOM | 自定义 `SqlTemplate` 节点树 |
| --- | --- | --- |
| 首期实现 | 低，直接遍历 `Node` | 中，需要编译 DOM |
| 嵌套动态标签 | 递归天然支持 | 递归天然支持 |
| 内存 | 保留 Element、属性、文本、父子关系和整份 Document | 只保留 Text/If/ForEach 等运行期需要的数据 |
| 模板校验 | 运行期或加载期遍历 DOM 时完成 | 可在编译期集中完成 |
| 并发 | DOM 必须严格只读 | 不可变节点天然适合 |
| 表达式缓存 | 可额外缓存 | 可直接放在 IfNode 中 |
| 配置来源 | 绑定 XML | 可由 XML、注解、数据库或代码构造 |

自定义节点树的最小模型可以是：

```java
interface SqlTemplateNode {}

final class TextNode implements SqlTemplateNode {
    final String text;
}

final class IfNode implements SqlTemplateNode {
    final String test;
    final List<SqlTemplateNode> thenNodes;
    final List<SqlTemplateNode> elseNodes;
}
```

随后再按需增加 `ForEachNode`、`ChooseNode` 等。

## 第二阶段演进条件

当满足以下任一条件时，将 DOM 在加载期编译为不可变 `SqlTemplate` 节点树：

- 正式支持 `choose/when/otherwise`、复杂 `forEach` 或更多动态标签；
- 需要缓存已解析的条件表达式 AST；
- XML 模板数量、文件规模或长期驻留内存成为实际问题；
- 模板来源不再只有 XML；
- 需要更精确的模板错误定位和加载期校验。

在此之前，缓存只读 DOM 是正确性、实现量和可维护性的平衡点。

## 测试与安全要求

- 使用 `src/test/resources` 中的真实 XML 文件测试，避免只覆盖内嵌字符串；
- 覆盖 true/false 分支、两层以上嵌套、foreach、空集合、非法 else、缺少属性和并发渲染；
- XML 解析器应禁用 DTD 与外部实体，避免 XXE；
- 值始终通过 `#{...}` 转换为 JDBC `?` 绑定；`${...}` 仅能用于经过严格校验的标识符；
- 缓存模板不可变或只读，不能在渲染时改写共享 DOM/节点树。
