package com.williambl.numen.gods.sacrifice

import com.williambl.numen.DEAD_PLANTS
import net.minecraft.block.Blocks
import net.minecraft.block.Fertilizable
import net.minecraft.block.FluidBlock
import net.minecraft.block.SpreadableBlock
import net.minecraft.tag.BlockTags
import net.minecraft.tag.FluidTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.World
import kotlin.math.min
import kotlin.math.pow
import kotlin.streams.asSequence

object ChthonicEnvironmentEvaluator: EnvironmentEvaluator {

    override fun evaluate(world: World, centre: BlockPos, radius: Int, yRadius: Int): Double {
        val blockTypeScore = BlockPos.streamOutwards(centre, radius, yRadius, radius).asSequence()
            .map(world::getBlockState)
            .map { state ->
                when {
                    BlockTags.BASE_STONE_NETHER.contains(state.block) -> 3.0
                    DEAD_PLANTS.contains(state.block) -> 3.0
                    state.block == Blocks.BEDROCK -> 2.5
                    FluidTags.LAVA.contains(state.fluidState.fluid) -> 2.0
                    BlockTags.BASE_STONE_OVERWORLD.contains(state.block) -> 2.0
                    BlockTags.FLOWERS.contains(state.block) -> -3.0
                    state.block is Fertilizable -> -3.0
                    BlockTags.CORALS.contains(state.block) -> -3.0
                    BlockTags.LEAVES.contains(state.block) -> -2.0
                    else -> 0.0
                }
            }
            .sum()

        val lightScore = BlockPos.streamOutwards(centre, radius, yRadius, radius).asSequence()
            .filter { pos -> world.getBlockState(pos).isAir }
            .filter { pos -> pos.y < world.getTopY(Heightmap.Type.MOTION_BLOCKING, pos.x, pos.z) }
            .map(world::getLightLevel)
            .map { l -> min(1.4 - (l/10f).pow(2), 1.0) }
            .average()

        val yScore = if (centre.y < 0) 5.0 else (5.0 - (centre.y/28.0).pow(2))

        return (blockTypeScore+yScore+lightScore) / ( 3.0*(radius)*(yRadius)*(radius)*8.0 + 5.0 + 1.0)
    }
}