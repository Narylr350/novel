package com.wtl.novel.Config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RuntimeDefaultsTest {

    @Test
    void applicationProfileDefaultsToDevWithoutMavenFiltering() throws Exception {
        assertEquals("${SPRING_PROFILES_ACTIVE:dev}", loadProperties("application.properties").getProperty("spring.profiles.active"));
    }

    @Test
    void devAndProdProfilesDoNotExposeUnusedSignatureSecret() throws Exception {
        assertNull(loadProperties("application-dev.properties").getProperty("api.signature.secret"));
        assertNull(loadProperties("application-prod.properties").getProperty("api.signature.secret"));
    }

    @Test
    void prodProfileKeepsReaderDeploymentsSafeByDefault() throws Exception {
        Properties prod = loadProperties("application-prod.properties");

        assertEquals("${TASK_SCHEDULER_ENABLED:false}", prod.getProperty("task.scheduler.enabled"));
        assertEquals("${TASK_NOVELPIA_TASK2_ENABLED:false}", prod.getProperty("task.novelpia.task2.enabled"));
        assertEquals("${TASK_NOVELPIA_PHOTO_ENABLED:false}", prod.getProperty("task.novelpia.photo.enabled"));
        assertEquals("${TASK_NOVELPIA_TASK3_ENABLED:false}", prod.getProperty("task.novelpia.task3.enabled"));
        assertEquals("${TASK_NOVELPIA_FIX_ERROR_CHAPTER_ENABLED:false}", prod.getProperty("task.novelpia.fix.error.chapter.enabled"));
        assertEquals("${TASK_SITEMAP_HTML_GENERATOR_ENABLED:false}", prod.getProperty("task.sitemap.html.generator.enabled"));
        assertEquals("${TASK_SITEMAP_INDEX_GENERATOR_ENABLED:false}", prod.getProperty("task.sitemap.index.generator.enabled"));
        assertEquals("${TASK_NOVELPIA_CRAWLER_ENABLED:false}", prod.getProperty("task.novelpia.crawler.enabled"));
        assertEquals("${TASK_UPDATE_NOVEL_FROM_FILE_ENABLED:false}", prod.getProperty("task.update.novel.from.file.enabled"));
        assertEquals("${APP_UI_MODE:reader}", prod.getProperty("app.ui.mode"));
    }

    private Properties loadProperties(String resourceName) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (stream == null) {
                throw new IOException("Missing resource: " + resourceName);
            }
            Properties properties = new Properties();
            properties.load(stream);
            return properties;
        }
    }
}
