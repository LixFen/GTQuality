package com.plainston.gtquality.integration.ae2;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.net.URLClassLoader;

import org.junit.jupiter.api.Test;

class AE2IntegrationTest {

    @Test
    void optionalEntryPointLoadsWithoutAE2OrForge() throws Exception {
        URL classes = AE2Integration.class.getProtectionDomain()
            .getCodeSource()
            .getLocation();
        try (URLClassLoader isolated = new URLClassLoader(new URL[] { classes }, null)) {
            Class<?> integration = Class.forName(AE2Integration.class.getName(), true, isolated);
            assertFalse(
                (boolean) integration.getMethod("isAvailable", ClassLoader.class)
                    .invoke(null, isolated));
        }
    }

    @Test
    void originalAE2DoesNotEnableUnofficialIntegration() {
        ClassLoader originalAE2 = new ClassLoader(null) {

            @Override
            public URL getResource(String name) {
                return name.equals("appeng/api/AEApi.class")
                    ? AE2IntegrationTest.class.getResource("/appeng/api/AEApi.class")
                    : null;
            }
        };
        assertFalse(AE2Integration.isAvailable(originalAE2));
    }

    @Test
    void unofficialStackApiEnablesIntegration() {
        ClassLoader unofficialAE2 = new ClassLoader(null) {

            @Override
            public URL getResource(String name) {
                return name.equals("appeng/api/storage/data/IAEStackType.class")
                    ? AE2IntegrationTest.class.getResource("/appeng/api/AEApi.class")
                    : null;
            }
        };
        assertTrue(AE2Integration.isAvailable(unofficialAE2));
    }
}
