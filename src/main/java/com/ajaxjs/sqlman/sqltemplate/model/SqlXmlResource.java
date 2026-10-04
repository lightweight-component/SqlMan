package com.ajaxjs.sqlman.sqltemplate.model;

import lombok.AllArgsConstructor;

/**
 * Complete text content of one discovered SQL XML classpath resource.
 */
@AllArgsConstructor
public final class SqlXmlResource {
    /**
     * Stable classpath-relative resource path used for load ordering.
     */
    public final String logicalPath;

    /**
     * Human-readable physical resource location used in diagnostics.
     */
    public final String source;

    /**
     * Complete UTF-8 XML source text.
     */
    public final String xml;
}
