package com.plainston.gtquality;

import net.minecraft.client.Minecraft;

/** Applies the vanilla font flags expected by non-Unicode locales. */
public final class AngelicaFontFix {

    private AngelicaFontFix() {}

    public static void apply() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.fontRenderer != null) {
            minecraft.fontRenderer.setUnicodeFlag(false);
            minecraft.fontRenderer.setBidiFlag(false);
        }
    }
}
