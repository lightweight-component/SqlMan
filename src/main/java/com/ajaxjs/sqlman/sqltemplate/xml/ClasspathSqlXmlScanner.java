package com.ajaxjs.sqlman.sqltemplate.xml;

import com.ajaxjs.sqlman.sqltemplate.model.SqlXmlResource;
import com.ajaxjs.util.ObjectHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Locates XML resources below one logical classpath directory without Spring.
 * Only ordinary directories and ordinary JAR files are supported.
 */
public final class ClasspathSqlXmlScanner {
    private ClasspathSqlXmlScanner() {
    }

    /**
     * Finds XML resources below one relative classpath directory.
     *
     * <p>The result is sorted by logical resource path so duplicate statement
     * resolution in the caller is deterministic.</p>
     *
     * @param resourceDirectory a relative directory such as {@code sql} or {@code app/sql}
     * @return discovered XML resources in stable order
     * @throws IllegalArgumentException if the directory is invalid or cannot be scanned
     */
    public static List<SqlXmlResource> scan(String resourceDirectory) {
        String root = normalizeRoot(resourceDirectory);
        Map<String, SqlXmlResource> resources = new LinkedHashMap<>();

        scanJavaClassPath(root, resources);
        scanContextClassLoader(root, resources);

        List<SqlXmlResource> result = new ArrayList<>(resources.values());
        result.sort(Comparator.comparing((SqlXmlResource resource) -> resource.logicalPath).thenComparing(resource -> resource.source));

        return result;
    }

    private static void scanJavaClassPath(String root, Map<String, SqlXmlResource> resources) {
        String classPath = System.getProperty("java.class.path", "");

        for (String entry : classPath.split(Pattern.quote(File.pathSeparator))) {
            if (entry.isEmpty())
                continue;

            Path path = Paths.get(entry);

            try {
                if (Files.isDirectory(path))
                    scanDirectory(path, root, resources);
                else if (Files.isRegularFile(path) && entry.toLowerCase(Locale.ROOT).endsWith(".jar"))
                    scanJar(path, root, resources);
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to scan SQL XML classpath entry: " + entry, e);
            }
        }
    }

    private static void scanContextClassLoader(String root, Map<String, SqlXmlResource> resources) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        if (loader == null)
            loader = ClasspathSqlXmlScanner.class.getClassLoader();

        try {
            Enumeration<URL> roots = loader.getResources(root);

            while (roots.hasMoreElements()) {
                URL url = roots.nextElement();

                if ("file".equals(url.getProtocol()))
                    scanDirectoryContents(Paths.get(url.toURI()), root, resources);
                else if ("jar".equals(url.getProtocol()))
                    scanJarUrl(url, root, resources);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to scan SQL XML directory: " + root, e);
        }
    }

    private static void scanDirectory(Path classpathRoot, String root, Map<String, SqlXmlResource> resources) throws IOException {
        Path directory = root.isEmpty() ? classpathRoot : classpathRoot.resolve(root);

        if (!Files.isDirectory(directory))
            return;

        scanDirectoryContents(directory, root, resources);
    }

    private static void scanDirectoryContents(Path directory, String root, Map<String, SqlXmlResource> resources) throws IOException {
        if (!Files.isDirectory(directory))
            return;

        try (Stream<Path> files = Files.walk(directory)) {
            for (Path file : files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".xml"))
                    .sorted()
                    .collect(Collectors.toList())) {
                String relativePath = directory.relativize(file).toString().replace(File.separatorChar, '/');
                String logicalPath = root + "/" + relativePath;
                String source = "file:" + file.toAbsolutePath().normalize();
                add(resources, new SqlXmlResource(logicalPath, source, new String(Files.readAllBytes(file), StandardCharsets.UTF_8)));
            }
        }
    }

    private static void scanJarUrl(URL url, String root, Map<String, SqlXmlResource> resources) throws IOException {
        URLConnection connection = url.openConnection();

        if (!(connection instanceof JarURLConnection))
            return;

        connection.setUseCaches(false);
        JarURLConnection jarConnection = (JarURLConnection) connection;
        URL jarUrl = jarConnection.getJarFileURL();

        try {
            scanJar(Paths.get(jarUrl.toURI()), root, resources);
        } catch (Exception e) {
            throw new IOException("Unable to scan SQL XML JAR: " + jarUrl, e);
        }
    }

    private static void scanJar(Path jarPath, String root, Map<String, SqlXmlResource> resources) throws IOException {
        String prefix = root + "/";
        List<String> entries = new ArrayList<>();

        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> iterator = jar.entries();

            while (iterator.hasMoreElements()) {
                JarEntry entry = iterator.nextElement();

                if (!entry.isDirectory() && entry.getName().startsWith(prefix) && entry.getName().toLowerCase(Locale.ROOT).endsWith(".xml"))
                    entries.add(entry.getName());
            }

            Collections.sort(entries);

            for (String entryName : entries) {
                JarEntry entry = jar.getJarEntry(entryName);
                String source = "jar:" + jarPath.toAbsolutePath().normalize() + "!/" + entryName;
                add(resources, new SqlXmlResource(entryName, source,
                        new String(readAllBytes(jar.getInputStream(entry)), StandardCharsets.UTF_8)));
            }
        }
    }

    private static byte[] readAllBytes(InputStream input) throws IOException {
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;

            while ((read = stream.read(buffer)) != -1)
                output.write(buffer, 0, read);

            return output.toByteArray();
        }
    }

    private static void add(Map<String, SqlXmlResource> resources, SqlXmlResource resource) {
        resources.putIfAbsent(resource.source, resource);
    }

    private static String normalizeRoot(String resourceDirectory) {
        if (ObjectHelper.isEmptyText(resourceDirectory))
            throw new IllegalArgumentException("resourceDirectory should not be null");

        String root = resourceDirectory.trim().replace('\\', '/');

        while (root.startsWith("/"))
            root = root.substring(1);

        while (root.endsWith("/"))
            root = root.substring(0, root.length() - 1);

        if (root.isEmpty() || root.contains(":") || root.contains(".."))
            throw new IllegalArgumentException("SQL XML directory must be a relative classpath directory: " + resourceDirectory);

        return root;
    }
}
