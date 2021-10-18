package com.williambl.numen.client

import com.williambl.numen.client.spells.tablet.EditClayTabletGui
import com.williambl.numen.spells.Spells
import com.williambl.numen.spells.tablet.EditClayTabletGuiDescription
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text


fun clientInit() {
    ScreenRegistry.register(Spells.EDIT_CLAY_TABLET_SCREEN_HANDLER) { gui: EditClayTabletGuiDescription, inventory: PlayerInventory, title: Text ->
        EditClayTabletGui(
            gui,
            inventory.player,
            title
        )
    }
}