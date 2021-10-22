package com.williambl.numen

import net.minecraft.SharedConstants
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import kotlin.math.abs

fun id(path: String) = Identifier("numen", path)

fun isVerySmall(value: Double): Boolean = abs(value) <= 0.0001

fun decay(value: Double, divideByInDay: Double) = if (isVerySmall(value)) 0.0 else value + ((value/divideByInDay)-value)/(SharedConstants.TICKS_PER_IN_GAME_DAY.toDouble())

fun registerBlockAndItem(id: Identifier, block: Block, itemSettings: Item.Settings = Item.Settings().group(numenGroup)): Block =
    Registry.register(Registry.BLOCK, id, block).also {
        Registry.register(Registry.ITEM, id, BlockItem(it, itemSettings))
    }

