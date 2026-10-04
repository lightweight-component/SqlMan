package com.ajaxjs.sqlman.sqltemplate.model;

import org.w3c.dom.Element;

/**
 * One cached {@code <sql>} statement DOM element and its source location.
 */
public final class SqlXmlStatement {
    /**
     * Read-only statement DOM element.
     */
    public final Element element;

    /**
     * Resource location used in diagnostics.
     */
    public final String source;

    /**
     * Serializes DOM traversal because DOM implementations need not be thread-safe.
     */
    public final Object renderLock = new Object();

    /**
     * Creates a cached statement descriptor.
     *
     * @param element statement DOM element
     * @param source  resource location used in diagnostics
     */
    public SqlXmlStatement(Element element, String source) {
        this.element = element;
        this.source = source;
    }
}
