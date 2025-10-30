package com.ajaxjs.sqlman.v2temp;

import com.ajaxjs.sqlman.model.CreateResult;
import com.ajaxjs.sqlman.model.UpdateResult;

import java.io.Serializable;

public class AutoSqlAction {
    String sql;

    public UpdateResult update(String where) {
        sql += " WHERE " + where;

        return null;
    }

    public <T extends Serializable> CreateResult<T> create(boolean isAutoIns, Class<T> idTypeClz) {
        return null;
    }

    public <T extends Serializable> CreateResult<T> create() {
        return null;
    }
}
