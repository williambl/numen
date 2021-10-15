package com.williambl.numen.gods.sacrifice

import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

fun interface VotiveOfferingEvaluator {
    fun evaluate(offering: ItemStack): Double
}