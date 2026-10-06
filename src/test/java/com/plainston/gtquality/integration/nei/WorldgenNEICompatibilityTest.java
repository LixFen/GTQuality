package com.plainston.gtquality.integration.nei;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class WorldgenNEICompatibilityTest {

    @Test
    void modernEventDoesNotRequireRemovedRecipeWidgetField() {
        ModernEvent event = new ModernEvent();
        assertSame(event.handlerRef, NEIButtonEventCompat.handlerRef(event));
    }

    @Test
    void legacyEventResolvesReferenceFromRecipeWidget() {
        LegacyEvent event = new LegacyEvent();
        assertSame(event.recipeWidget.reference, NEIButtonEventCompat.handlerRef(event));
    }

    public static class ModernEvent {

        public final Object handlerRef = new Object();
    }

    public static class LegacyEvent {

        public final LegacyWidget recipeWidget = new LegacyWidget();
    }

    public static class LegacyWidget {

        final Object reference = new Object();

        public Object getRecipeHandlerRef() {
            return reference;
        }
    }
}
