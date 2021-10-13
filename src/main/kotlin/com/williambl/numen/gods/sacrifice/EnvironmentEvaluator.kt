package com.williambl.numen.gods.sacrifice

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun interface EnvironmentEvaluator {
    fun evaluate(world: World, centre: BlockPos, radius: Int, yRadius: Int): Double
}