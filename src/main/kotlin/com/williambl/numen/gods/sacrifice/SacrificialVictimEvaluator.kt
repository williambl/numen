package com.williambl.numen.gods.sacrifice

import net.minecraft.entity.LivingEntity

fun interface SacrificialVictimEvaluator {
    fun evaluate(victim: LivingEntity): Double
}