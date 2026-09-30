package com.plainston.gtquality.mixin.client;

import com.plainston.gtquality.GTQuality;
import cpw.mods.fml.common.Loader;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FontRenderer.class)
public abstract class AngelicaUnicodeFontMixin {

    @ModifyVariable(method = "setUnicodeFlag", at = @At("HEAD"), argsOnly = true)
    private boolean gtquality$keepNonUnicodeFont(boolean unicode) {
        return GTQuality.fixAngelicaUnicodeFont && Loader.isModLoaded("angelica") ? false : unicode;
    }

    @ModifyVariable(method = "setBidiFlag", at = @At("HEAD"), argsOnly = true)
    private boolean gtquality$disableBidi(boolean bidi) {
        return GTQuality.fixAngelicaUnicodeFont && Loader.isModLoaded("angelica") ? false : bidi;
    }
}
