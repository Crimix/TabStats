package com.black_dog20.tabstats.common.compat;

import com.black_dog20.tabstats.client.keybinds.Keybinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import snownee.jade.api.Accessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.ui.IBoxElement;
import snownee.jade.api.ui.TooltipRect;

@WailaPlugin
public class Jade implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addBeforeRenderCallback(this::beforeRender);
    }

    private boolean beforeRender(IBoxElement var1, TooltipRect var2, GuiGraphics var3, Accessor<?> var4) {
        return (Minecraft.getInstance().options.keyPlayerList.isDown() && !Minecraft.getInstance().hasSingleplayerServer()) || Keybinds.SHOW.isDown();
    }
}
