package com.ajaxjs.util.io;

import com.ajaxjs.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Slf4j
public class Resources {
    /**
     * 获取 Classpath 根目录下的资源文件的路径
     *
     * @param resource 文件名称，输入空字符串这返回 Classpath 根目录
     * @param isDecode 是否解码
     * @return 所在工程路径+资源路径，找不到文件则返回 null
     */
    public static String getResourcesFromClasspath(String resource, boolean isDecode) {
        URL url = Resources.class.getClassLoader().getResource(resource);

        if (url == null)
            throw new RuntimeException("The resource " + resource + " not found");

        return url2path(url, isDecode);
    }

    /**
     * 获取当前类目录下的资源文件
     * 测试时候，源码目录没有，要手动复制
     *
     * @param clz      类引用
     * @param resource 资源文件名
     * @return 当前类的绝对路径，找不到文件则返回 null
     */
    public static String getResourcesFromClass(Class<?> clz, String resource) {
        return getResourcesFromClass(clz, resource, true);
    }

    /**
     * 获取当前类目录下的资源文件
     *
     * @param clz      类引用
     * @param resource 资源文件名
     * @param isDecode 是否解码
     * @return 当前类的绝对路径，找不到文件则返回 null
     */
    public static String getResourcesFromClass(Class<?> clz, String resource, boolean isDecode) {
        return url2path(clz.getResource(resource), isDecode);
    }

    /**
     * 获取 Classpath 根目录下的资源文件
     *
     * @param resource 文件名称，输入空字符串这返回 Classpath 根目录。可以支持包目录，例如  com\\foo\\new-file.txt
     * @return 所在工程路径+资源路径，找不到文件则返回 null
     */
    public static String getResourcesFromClasspath(String resource) {
        return getResourcesFromClasspath(resource, true);
    }

    /**
     * url.getPath() 返回 /D:/project/a，需要转换一下
     *
     * @param url      URL 对象
     * @param isDecode 是否解码
     * @return 转换路径
     */
    private static String url2path(URL url, boolean isDecode) {
        if (url == null) return null;

        String path;

        if (isDecode)
            path = StringUtils.uriDecode(new File(url.getPath()).toString(), StandardCharsets.UTF_8);
        else {
            path = url.getPath();
            path = path.startsWith("/") ? path.substring(1) : path;
        }

        // path = path.replaceAll("file:\\", "");
        return path;
    }

    /**
     * Java 类文件 去掉后面的 .class 只留下类名
     *
     * @param file        Java 类文件
     * @param packageName 包名称
     * @return 类名
     */
    public static String getClassName(File file, String packageName) {
        String clzName = file.getName().substring(0, file.getName().length() - 6);

        return packageName + '.' + clzName;
    }

    /**
     * 从 classpath 获取资源文件的内容
     *
     * @param path 资源文件路径，例如 application.yml
     * @return 资源文件的内容
     */
    public static String getResourceText(String path) {
        try (InputStream in = getResource(path)) {
            if (in == null) {
                log.warn("[{}] 下没有 [{}] 资源文件", getResourcesFromClasspath(StrUtil.EMPTY_STRING), path);
                return null;
            }

            return StreamHelper.copyToString(in);
        } catch (IOException e) {
            log.warn("ERROR>>", e);
            return null;
        }
    }

    /**
     * 可以在 JAR 中获取资源文件
     * <a href="https://www.cnblogs.com/coderxx/p/13566423.html">...</a>
     * <p>
     * 根据路径获取资源的 InputStream
     * 此方法用于从类路径中加载资源，返回一个输入流，以便读取该资源
     * 主要用于简化资源文件的读取过程，避免直接操作文件系统或处理类路径的问题
     *
     * @param path 资源的路径。可以是类路径上的相对路径或文件系统中的绝对路径
     * @return 找到的资源的输入流，如果找不到则返回 null
     */
    public static InputStream getResource(String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }

    /**
     * 获取正在运行的 JAR 文件的目录
     * 如果您在 IDE 中运行代码，则该代码可能会返回项目的根目录
     *
     * @return JAR 文件的目录
     */
    public static String getJarDir() {
        String jarDir = null;

        try {
            jarDir = new File(Resources.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
        } catch (URISyntaxException e) {
            log.warn("ERROR>>", e);
        }

        return jarDir;
    }

    /**
     * 列出资源文件列表
     */
    static void listResourceFile() {
        ClassLoader classLoader = Resources.class.getClassLoader();
        URL resourceUrl = classLoader.getResource(StrUtil.EMPTY_STRING);

        if (resourceUrl != null) {
            File[] files = new File(resourceUrl.getFile()).listFiles(); // 将URL转换为文件路径

            assert files != null;
            for (File file : files) {
                if (file.isFile()) // 是否为普通文件（非目录）
                    System.out.println(file.getName());
            }
        }
    }

    /**
     * 从类路径加载 properties 文件
     *
     * @param filename properties 文件
     */
    public static Properties getProperties(String filename) {
        Properties prop = new Properties();

        try (InputStream input = getResource(filename)) {
            if (input == null)
                throw new FileNotFoundException();
            // 加载输入流中的键值对到 Properties 对象
            prop.load(input);

            return prop;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Properties File not found " + filename, e);
        } catch (IOException e) {
            throw new RuntimeException("Properties File error " + filename, e);
        }
    }
}
