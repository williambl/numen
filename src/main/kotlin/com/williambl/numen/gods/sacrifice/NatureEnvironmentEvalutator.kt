package com.williambl.numen.gods.sacrifice

import com.williambl.numen.DEAD_PLANTS
import net.minecraft.block.Fertilizable
import net.minecraft.block.FluidBlock
import net.minecraft.block.SpreadableBlock
import net.minecraft.tag.BlockTags
import net.minecraft.tag.FluidTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.streams.asSequence

object NatureEnvironmentEvalutator: EnvironmentEvaluator {

    override fun evaluate(world: World, centre: BlockPos, radius: Int, yRadius: Int): Double {
        val rawScore = BlockPos.streamOutwards(centre, radius, yRadius, radius).asSequence()
            .map(world::getBlockState)
            .map { state ->
                when {
                    DEAD_PLANTS.contains(state.block) -> -3.0
                    BlockTags.FLOWERS.contains(state.block) -> 3.0
                    BlockTags.CORALS.contains(state.block) -> 3.0
                    BlockTags.LEAVES.contains(state.block) -> 2.0
                    BlockTags.LOGS.contains(state.block) -> 1.0
                    state.block is Fertilizable -> 2.0
                    state.block is SpreadableBlock -> 1.0
                    FluidTags.WATER.contains(state.fluidState.fluid) -> 1.0
                    state.isAir -> 0.5
                    else -> 0.0
                }
            }
            .sum()

        return rawScore / ((3*radius*yRadius*radius*8).toDouble())
    }
}