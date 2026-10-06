package com.plainston.gtquality.integration.nei;

/** Resolves both known NEI button event layouts without linking to the removed recipeWidget field. */
final class NEIButtonEventCompat {

    private NEIButtonEventCompat() {}

    static Object handlerRef(Object event) {
        try {
            try {
                // NEI 2.8.155 exposes the reference directly.
                return event.getClass()
                    .getField("handlerRef")
                    .get(event);
            } catch (NoSuchFieldException legacyEvent) {
                Object widget = event.getClass()
                    .getField("recipeWidget")
                    .get(event);
                return widget.getClass()
                    .getMethod("getRecipeHandlerRef")
                    .invoke(widget);
            }
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Cannot read NEI recipe button event reference", error);
        }
    }
}
