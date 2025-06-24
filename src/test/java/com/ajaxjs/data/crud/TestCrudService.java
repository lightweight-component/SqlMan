package com.ajaxjs.data.crud;

import com.ajaxjs.data.BaseTest;
import com.ajaxjs.data.crud.CRUD_Service;
import lombok.Data;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestCrudService extends BaseTest {
    @Autowired
    CRUD_Service crud;

    @Test
    public void testQueryOne() {
        int total = crud.queryOne(int.class, "SELECT count(*) FROM Employees");
        System.out.println(total);
        assertTrue(total > 0);
    }

    @Test
    public void testInfoMap() {
        Map<String, Object> map = crud.infoMap("SELECT * FROM Employees WHERE id = ?", 1);
        System.out.println(map);
        System.out.println(crud.hashCode());
        assertNotNull(map);
    }

    @Data
    public static class Employee {
        private Integer id;

        private String name;

        private Date birthday;

        private Date hireDate;

        private String department;
    }

    @Test
    public void testInfoBean() {
        Employee employee = crud.info(Employee.class, "SELECT * FROM Employees WHERE id = ?", 1);
        System.out.println(employee);

        System.out.println(crud.hashCode());

        assertNotNull(employee);
    }
}
