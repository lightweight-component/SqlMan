package com.ajaxjs.data;

import com.ajaxjs.data.jdbc_helper.JdbcConn;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;

@ContextConfiguration(classes = TestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public abstract class BaseTest {
    @Before
    public void initDb() {
        System.out.println("initDb");
//        DataBaseConnection.initDb();

    }

    @After
    public void closeDb() {
        JdbcConn.closeDb();

        // 调用删除方法
        if (deleteDirectory(getTestDir()))
            System.out.println("目录及其内容已删除。");
        else
            System.out.println("目录删除失败。");

        System.out.println("closeDb");
    }

    public static File getTestDir() {
        return new File(new File("."), "derby-test-db");
    }

    /**
     * 删除目录及其所有内容的递归方法。
     *
     * @param directory 要删除的目录对象
     * @return 删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(File directory) {
        // 检查目录是否存在
        if (!directory.exists())
            return false;

        // 删除目录中的所有文件和子目录
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory())
                    // 如果是子目录，则递归调用删除方法
                    deleteDirectory(file);
                else
                    // 如果是文件，直接删除
                    file.delete();
            }
        }

        // 删除空目录
        return directory.delete();
    }
}
