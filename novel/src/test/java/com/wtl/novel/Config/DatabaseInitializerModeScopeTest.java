package com.wtl.novel.Config;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DatabaseInitializerModeScopeTest {

    @Test
    void readerModeOnlyRequiresReaderFacingTables() {
        List<String> readerTables = DatabaseInitializer.getRequiredPrimaryTables("reader");

        assertTrue(readerTables.contains("novel"));
        assertTrue(readerTables.contains("chapter"));
        assertTrue(readerTables.contains("reading_record"));
        assertFalse(readerTables.contains("terminology"));
        assertFalse(readerTables.contains("chapter_execute"));
        assertFalse(readerTables.contains("platform_api_key"));
    }

    @Test
    void maintainerModeKeepsFullPrimaryTableSurface() {
        List<String> maintainerTables = DatabaseInitializer.getRequiredPrimaryTables("maintainer");

        assertTrue(maintainerTables.contains("novel"));
        assertTrue(maintainerTables.contains("chapter"));
        assertTrue(maintainerTables.contains("terminology"));
        assertTrue(maintainerTables.contains("chapter_execute"));
        assertTrue(maintainerTables.contains("platform_api_key"));
    }

    @Test
    void secondaryInitializationStillDependsOnSecondaryDatasourcePresence() {
        DatabaseInitializer withoutSecondary = new DatabaseInitializer(mock(DataSource.class), null);
        DatabaseInitializer withSecondary = new DatabaseInitializer(mock(DataSource.class), mock(DataSource.class));

        assertFalse(withoutSecondary.shouldInitializeSecondaryDatabase());
        assertTrue(withSecondary.shouldInitializeSecondaryDatabase());
    }
}
