package com.ajaxjs.sqlman.sqlgenerator;

import java.io.Serializable;

/**
 * Auto query business logic automatically.
 */
public interface AutoQueryBusiness {
    /**
     * When querying the list, whether to automatically add sorting by date.
     *
     * @return Whether to automatically add sorting by date.
     */
    boolean isListOrderByDate();

    /**
     * Whether to add tenant data isolation.
     *
     * @return Whether to add tenant data isolation.
     */
    boolean isTenantIsolation();

    /**
     * Restrict the query results to include only data belonging to the current user.
     *
     * @return Whether to restrict the query results to include only data belonging to the current user.
     */
    boolean isCurrentUserOnly();

    /**
     * Whether to filter deleted data.
     *
     * @return Whether to filter deleted data.
     */
    boolean isFilterDeleted();

    /**
     * How to get current user id
     *
     * @return The id of current user, available types are int/long/string.
     */
    Serializable getCurrentUserId();

    /**
     * How to get tenant id
     *
     * @return The id of tenant, available types are int/long/string.
     */
    Serializable getTenantId();

    /**
     * Override this function if you want to join tables.
     *
     * @return How to join tables.
     */
    default TableJoin getTableJoin() {
        return null;
    }

    /**
     * Code 8421, can be:
     * <pre>
     * 1=creator_id
     * 2=creator
     * 4=user_id
     * 8=user_name
     * </pre>
     * Common cases:
     * <pre>
     * 3=creator_id+creator
     * 7=creator_id+creator+user_id
     * 15=creator_id+creator+user_id+user_name
     * </pre>
     */
    default Integer getSaveUserOnCreate() {
        return null;
    }
}
