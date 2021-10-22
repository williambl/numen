package com.williambl.numen.client

import com.williambl.numen.client.spells.tablet.EditClayTabletGui
import com.williambl.numen.id
import com.williambl.numen.spells.Spells
import com.williambl.numen.spells.tablet.EditClayTabletGuiDescription
import com.williambl.numen.spells.tablet.getTabletText
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.color.block.BlockColorProvider
import net.minecraft.client.color.world.BiomeColors
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockRenderView


fun clientInit() {
    ScreenRegistry.register(Spells.EDIT_CLAY_TABLET_SCREEN_HANDLER) { gui: EditClayTabletGuiDescription, inventory: PlayerInventory, title: Text ->
        EditClayTabletGui(
            gui,
            inventory.player,
            title
        )
    }

    ClientPlayNetworking.registerGlobalReceiver(id("show_clay_tablet")) { client, handler, buf, response ->
        val stack = buf.readItemStack()
        client.execute {
            client.gameRenderer.showFloatingItem(stack)
        }
    }

    FabricModelPredicateProviderRegistry.register(Spells.FIRED_TABLET, id("lines_written")) { stack, _, _, _ ->
        val text = stack.getTabletText()
        when {
            text.length > 50 -> 3.0f
            text.length > 20 -> 2.0f
            text.isNotEmpty() -> 1.0f
            else -> 0.0f
        }
    }

    ClientLifecycleEvents.CLIENT_STARTED.register { client ->
        client.blockColors.registerColorProvider({ _, world, pos, _ ->
            if (world != null && pos != null) BiomeColors.getWaterColor(
                world,
                pos
            ) else -1
        }, Spells.INFUSION_CAULDRON_BLOCK)
    }
}