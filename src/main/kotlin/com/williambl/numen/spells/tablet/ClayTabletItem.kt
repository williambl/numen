package com.williambl.numen.spells.tablet

import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
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
        stack.getTabletInfusions().let {
            if (it.isEmpty()) {
                tooltip.add(TranslatableText("item.numen.clay_tablet.tooltip.no_infusions").formatted(Formatting.GRAY))
            } else {
                tooltip.add(TranslatableText("item.numen.clay_tablet.tooltip.infusions").formatted(Formatting.GRAY))
                tooltip.addAll(it.map { entry ->
                    TranslatableText("item.numen.clay_tablet.tooltip.infusion", entry.key.name, entry.value).formatted(Formatting.GRAY)
                })
            }
        }
        super.appendTooltip(stack, world, tooltip, context)
    }
}

fun ItemStack.getTabletText(): String = this.getOrCreateSubNbt("ClayTablet").getString("Text")
fun ItemStack.setTabletText(text: String) = this.getOrCreateSubNbt("ClayTablet").putString("Text", text)

fun ItemStack.getTabletInfusions(): Map<Item, Int> = readInfusions(this.getOrCreateSubNbt("ClayTablet").getCompound("Infusions"))
fun ItemStack.setTabletInfusions(infusions: Map<Item, Int>) = this.getOrCreateSubNbt("ClayTablet").put("Infusions", infusions.write())
fun ItemStack.addTabletInfusions(infusions: Map<Item, Int>) {
    val currentInfusions = getTabletInfusions()
    setTabletInfusions(infusions.mapValues { currentInfusions.getOrDefault(it.key, 0) + it.value }
                    + currentInfusions.filter { !infusions.containsKey(it.key) })
}

fun Map<Item, Int>.write(): NbtCompound = NbtCompound().also { nbt ->
    this.forEach {
        nbt.putInt(Registry.ITEM.getId(it.key).toString(), it.value)
    }
}

fun readInfusions(nbt: NbtCompound): Map<Item, Int> = nbt.keys.associate { Registry.ITEM.get(Identifier(it)) to nbt.getInt(it) }