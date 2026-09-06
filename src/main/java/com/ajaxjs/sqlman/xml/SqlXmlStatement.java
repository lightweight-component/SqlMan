package com.ajaxjs.sqlman.xml;

import org.w3c.dom.Element;

final class SqlXmlStatement {
    final Element element;
    final String source;
    final Object renderLock = new Object();

    SqlXmlStatement(Element element, String source) {
        this.element = element;
        this.source = source;
    }
}
