package com.williambl.numen.client.spells.tablet

import com.williambl.numen.id
import com.williambl.numen.spells.tablet.EditClayTabletGuiDescription
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

class EditClayTabletGui(description: EditClayTabletGuiDescription, player: PlayerEntity, title: Text) :
    CottonInventoryScreen<EditClayTabletGuiDescription>(description, player, title) {

    override fun onClose() {
        ClientPlayNetworking.send(id("edit_clay_tablet"), PacketByteBufs.create().writeString((description as EditClayTabletGuiDescription).text.text))
        super.onClose()
    }
}