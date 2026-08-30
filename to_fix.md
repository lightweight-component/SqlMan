# SqlMan 待修复问题

更新时间：2026-08-30


## P1：新增回归测试发现的源码问题

### 1. SQL 注入分析器放过子查询和 UNION

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

