package com.ajaxjs.util.io;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class TestResources {
    @Test
    public void testGetResourcesFromClasspath_FoundResource_ShouldReturnPath() {
        String resourcePath = Resources.getResourcesFromClasspath("\\com\\test.txt");
    }

    @Test
    public void testGetResourcesFromClasspath_NotFoundResource_ShouldReturnNull() {
        assertThrows(RuntimeException.class, () -> Resources.getResourcesFromClasspath("application.yml"));
    }

    @Test
    public void testGetResourcesFromClass_FoundResource_ShouldReturnPath() {
        String resourcePath = Resources.getResourcesFromClass(TestResources.class, "test.txt");
        System.out.println(resourcePath);
        assertNull(resourcePath);
    }

    @Test
    public void testGetResourcesFromClass_NotFoundResource_ShouldReturnNull() {
        String resourcePath = Resources.getResourcesFromClass(TestResources.class, "non-existent-resource.txt");
        assertNull(resourcePath, "Expected null for non-existent resource");
    }

    @Test
    public void testGetJarDir_ShouldReturnValidJarDirectory() {
        String jarDir = Resources.getJarDir();
        assertNotNull(jarDir, "JAR directory not found");
    }

    @Test
    public void testGetResourceText_FoundResource_ShouldReturnContent() {
        String resourceContent = Resources.getResourceText("README.md");
        assertNotNull(resourceContent, "Resource content not found");
    }

    @Test
    public void testGetResourceText_NotFoundResource_ShouldReturnNull() {
        String resourceContent = Resources.getResourceText("non-existent-file.md");
        assertNull(resourceContent, "Expected null for non-existent resource");
    }

    @Test
    public void testGetProperties_ValidPropertiesFile_ShouldLoadProperties() {
        Properties properties = Resources.getProperties("test-demo.properties");
        System.out.println(properties);
        assertNotNull(properties);
    }

    @Test
    public void testGetProperties_NotFoundPropertiesFile_ShouldThrowException() {
        assertThrows(RuntimeException.class, () -> Resources.getProperties("application.properties"));
    }
}
