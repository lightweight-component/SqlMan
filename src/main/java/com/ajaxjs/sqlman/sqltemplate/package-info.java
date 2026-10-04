/**
 * Dynamic SQL template compilation, rendering, and JDBC parameter binding.
 * <p>
 * A {@link com.ajaxjs.sqlman.sqltemplate.SqlXmlDomTemplate} compiles an inline
 * XML-compatible SQL fragment, or represents a {@code <sql>} node loaded from
 * a resource by {@link com.ajaxjs.sqlman.sqltemplate.xml.SqlXmlMgr}. Rendering
 * evaluates {@code <if>}, {@code <else>}, and {@code <forEach>} nodes and
 * returns a {@link com.ajaxjs.sqlman.sqltemplate.model.RenderedSql}. The DOM is
 * reusable while rendering state remains local to each invocation.
 * </p>
 * <p>
 * {@link com.ajaxjs.sqlman.sqltemplate.ParameterBinder} then converts
 * {@code #{name}} placeholders into JDBC {@code ?} placeholders and creates
 * ordered parameter values. {@code ${name}} is reserved for SQL identifiers
 * and is accepted only when it satisfies the identifier validation rule; it
 * must not receive untrusted user input. SQL data values should always use
 * {@code #{...}} or ordinary JDBC {@code ?} placeholders.
 * </p>
 * <p>
 * Inline templates must be valid XML fragments. SQL text containing XML
 * metacharacters such as {@code <} or {@code &} must use XML escaping or CDATA.
 * Conditions are evaluated through {@link com.ajaxjs.sqlman.sqltemplate.ExpressionEvaluator};
 * the default implementation uses a restricted JSqlParser expression subset.
 * </p>
 */
package com.ajaxjs.sqlman.sqltemplate;
