package com.williambl.numen.spells.tablet

import com.williambl.numen.spells.Spells
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WPlainPanel
import io.github.cottonmc.cotton.gui.widget.WTextField
import io.github.cottonmc.cotton.gui.widget.data.Insets
import net.minecraft.entity.player.PlayerInventory


class EditClayTabletGuiDescription(
    syncId: Int,
    playerInventory: PlayerInventory,
) : SyncedGuiDescription(Spells.EDIT_CLAY_TABLET_SCREEN_HANDLER, syncId, playerInventory) {
    val root: WPlainPanel
    val text: WTextField

    init {
        root = WPlainPanel()
        setRootPanel(root)
        root.setSize(300, 200)
        root.insets = Insets.ROOT_PANEL

        text = WTextField()
        text.maxLength = 256
        root.add(text, 0, 18, 300, 200)
    }
}