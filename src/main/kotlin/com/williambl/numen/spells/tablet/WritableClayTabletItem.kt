package com.williambl.numen.spells.tablet

import com.williambl.numen.numenGroup
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

object WritableClayTabletItem: ClayTabletItem(Settings().maxCount(1).fireproof().group(numenGroup)) {
    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        if (user is ServerPlayerEntity) {
            user.openHandledScreen(object : NamedScreenHandlerFactory {
                override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler =
                    EditClayTabletGuiDescription(syncId, inv)

                override fun getDisplayName(): Text = stack.name
            })
        }
        return TypedActionResult.success(stack)
    }
}