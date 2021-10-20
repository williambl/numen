package com.williambl.numen.spells.tablet

import com.williambl.numen.id
import com.williambl.numen.numenGroup
import com.williambl.numen.spells.Spells
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

object FiredClayTabletItem: ClayTabletItem(Settings().maxCount(1).fireproof().group(numenGroup)) {
    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        val spellText = stack.getTabletText()
        val spell = Spells.REGISTRY.find { it.matches(spellText) } ?: return TypedActionResult.fail(stack)
        if (!world.isClient) {
            spell.run(world, user, spell.getData(spellText))
            ServerPlayNetworking.send(user as ServerPlayerEntity?, id("show_clay_tablet"), PacketByteBufs.create().writeItemStack(stack))
        }
        stack.decrement(1)
        return TypedActionResult.consume(stack)
    }
}