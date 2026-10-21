package com.plainston.gtquality.integration.ae2;

import cpw.mods.fml.common.Loader;

/** This entry point must remain loadable without any AE2 classes. */
public final class AE2Integration {

    // AE2 Unofficial's extended stack API, absent from the original AE2 rv3 API.
    private static final String UNOFFICIAL_API = "appeng/api/storage/data/IAEStackType.class";

    private AE2Integration() {}

    public static boolean isAvailable(ClassLoader loader) {
        return loader.getResource(UNOFFICIAL_API) != null;
    }

    public static void register() {
        if (Loader.isModLoaded("appliedenergistics2") && isAvailable(AE2Integration.class.getClassLoader())) {
            PowderCell.register();
        }
    }
}
