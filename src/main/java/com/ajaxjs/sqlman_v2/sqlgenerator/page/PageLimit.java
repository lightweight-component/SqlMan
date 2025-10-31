package com.ajaxjs.sqlman_v2.sqlgenerator.page;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * Do the pagination like MySQL start/limit
 */
@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PageLimit extends PageBase {
    private final int start = 0;

    private final int limit = DEFAULT_PAGE_SIZE;
}
