package com.williambl.numen.spells.tablet

import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

abstract class ClayTabletItem(settings: Settings) : Item(settings) {
    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
    ) {
        tooltip.add(stack.getTabletText().let {
            if (it.isBlank()) {
                TranslatableText("item.numen.clay_tablet.tooltip.no_inscription")
            } else {
                TranslatableText("item.numen.clay_tablet.tooltip.inscription", it)
            }
        }.formatted(Formatting.GRAY))
        super.appendTooltip(stack, world, tooltip, context)
    }
}

fun ItemStack.getTabletText(): String = this.getOrCreateSubNbt("ClayTablet").getString("Text")
fun ItemStack.setTabletText(text: String) = this.getOrCreateSubNbt("ClayTablet").putString("Text", text)

fun ItemStack.getTabletInfusions(): Map<Item, Int> = this.getOrCreateSubNbt("ClayTablet").getCompound("Infusions").let { nbt ->
    nbt.keys.associate { Registry.ITEM.get(Identifier(it)) to nbt.getInt(it) }
}
fun ItemStack.setTabletInfusions(infusions: Map<Item, Int>) = this.getOrCreateSubNbt("ClayTablet").put("Infusions", NbtCompound().apply {
    infusions.forEach {
        putInt(Registry.ITEM.getId(it.key).toString(), it.value)
    }
})
