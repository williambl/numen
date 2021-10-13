package com.williambl.numen.gods.sacrifice

import com.williambl.numen.DEAD_PLANTS
import net.minecraft.block.Fertilizable
import net.minecraft.block.FluidBlock
import net.minecraft.block.SpreadableBlock
import net.minecraft.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.streams.asSequence

object NatureEnvironmentEvalutator: EnvironmentEvaluator {

    override fun evaluate(world: World, centre: BlockPos, radius: Int, yRadius: Int): Double {
        val rawScore = BlockPos.streamOutwards(centre, radius, yRadius, radius).asSequence()
            .map(world::getBlockState)
            .map { state ->
                when {
                    DEAD_PLANTS.contains(state.block) -> -3
                    BlockTags.FLOWERS.contains(state.block) -> 3
                    BlockTags.CORALS.contains(state.block) -> 3
                    BlockTags.LEAVES.contains(state.block) -> 2
                    BlockTags.LOGS.contains(state.block) -> 1
                    state.block is Fertilizable -> 2
                    state.block is SpreadableBlock -> 1
                    else -> 0
                }
            }
            .sum()

        return rawScore / (3*(radius*yRadius*radius).toDouble())
    }
}