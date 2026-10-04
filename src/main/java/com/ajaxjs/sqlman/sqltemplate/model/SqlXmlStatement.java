package com.ajaxjs.sqlman.sqltemplate.model;

import org.w3c.dom.Element;

public final class SqlXmlStatement {
    public final Element element;
    public final String source;
    public final Object renderLock = new Object();

    public SqlXmlStatement(Element element, String source) {
        this.element = element;
        this.source = source;
    }
}
