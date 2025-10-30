package com.ajaxjs.sqlman_v2.crud;

import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.sqlman_v2.Action;
import com.ajaxjs.util.BoxLogger;
import org.slf4j.MDC;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Update extends BaseAction {
    public Update(Action action) {
        super(action);
    }

    public UpdateResult update() {
        String resultText = null;

        try (PreparedStatement ps = action.getConn().prepareStatement(action.getSql())) {
            setParam2Ps(ps);

            int effectedRows = ps.executeUpdate();
            UpdateResult result = new UpdateResult();
            result.setOk(effectedRows > 0);
            result.setEffectedRows(effectedRows);
            resultText = result.toString();

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("SQL update error.", e);
        } finally {
            String _resultText = resultText;
            String traceId = MDC.get(BoxLogger.TRACE_KEY);
            String bizAction = MDC.get(BoxLogger.BIZ_ACTION);

//            CompletableFuture.runAsync(() -> PrettyLogger.printLog("Update", traceId, bizAction, sql, params, PrintRealSql.printRealSql(sql, params), this, _resultText, true));
        }
    }
}
