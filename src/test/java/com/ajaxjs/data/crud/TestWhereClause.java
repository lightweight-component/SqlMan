package com.ajaxjs.data.crud;


import org.junit.Test;
import org.mockito.Mock;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestWhereClause {
    @Mock
    private HttpServletRequest request;

    private Map<String, String[]> parameterMap = new HashMap<>();

    @Test
    public void testGetWhereClauseWithSingleValue() {
        parameterMap.put("q_name", new String[]{"Alex"});
        setupRequest(parameterMap);

        String whereClause = FastCRUD_Service.getWhereClause(request);
        assertEquals(" AND name = 'Alex'", whereClause);
    }

    @Test
    public void testGetWhereClauseWithSingleValueSql() {
        parameterMap.put("q_name", new String[]{"SELECT dd"});
        setupRequest(parameterMap);

        String whereClause = FastCRUD_Service.getWhereClause(request);
        assertEquals(" AND name = ''", whereClause);
    }

    @Test
    public void testGetWhereClauseWithMultipleValues() {
        parameterMap.put("q_id", new String[]{"1", "2", "3"});
        setupRequest(parameterMap);

        String whereClause = FastCRUD_Service.getWhereClause(request);
        assertEquals(" AND id IN ('1','2','3')", whereClause);
    }

    @Test
    public void testGetWhereClauseWithNoMatchingParameters() {
        parameterMap.put("name", new String[]{"Alex"});
        setupRequest(parameterMap);

        String whereClause = FastCRUD_Service.getWhereClause(request);
        assertEquals("", whereClause);
    }

    @Test
    public void testGetWhereClauseWithEmptyValues() {
        parameterMap.put("q_name", new String[]{});
        setupRequest(parameterMap);

        String whereClause = FastCRUD_Service.getWhereClause(request);
        assertEquals(" AND name IN ()", whereClause);
    }

    @Test
    public void testGetWhereClauseWithNullValues() {
        parameterMap.put("q_name", new String[]{null});
        setupRequest(parameterMap);

        String whereClause = FastCRUD_Service.getWhereClause(request);
        assertEquals(" AND name = 'null'", whereClause);
    }

    private void setupRequest(Map<String, String[]> parameterMap) {
        request = mock(HttpServletRequest.class);
        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getParameterNames()).thenReturn(Collections.enumeration(parameterMap.keySet()));

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet())
            when(request.getParameterValues(entry.getKey())).thenReturn(entry.getValue());
    }
}
