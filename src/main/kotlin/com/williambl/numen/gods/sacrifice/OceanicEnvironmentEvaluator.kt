package com.williambl.numen.gods.sacrifice

import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.WaterCreatureEntity
import net.minecraft.tag.BlockTags
import net.minecraft.tag.FluidTags
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.pow
import kotlin.streams.asSequence

object OceanicEnvironmentEvaluator: EnvironmentEvaluator {

    override fun evaluate(world: World, centre: BlockPos, radius: Int, yRadius: Int): Double {
        if (world.dimension.isUltrawarm) {
            return -1.0
        }

        val blockTypeScore = BlockPos.streamOutwards(centre, radius, yRadius, radius).asSequence()
            .map(world::getBlockState)
            .map { state ->
                when {
                    BlockTags.CORALS.contains(state.block) -> 3.0
                    FluidTags.WATER.contains(state.fluidState.fluid) -> 2.0
                    BlockTags.SAND.contains(state.block) -> 2.0
                    else -> 0.0
                }
            }
            .sum()

        val yScore = if (centre.y < 0) 5.0 else (5.0 - ((centre.y-world.seaLevel)/10.0).pow(2))

        val entityScore = world.getEntitiesByClass(LivingEntity::class.java, Box.of(Vec3d.ofBottomCenter(centre),
            radius.toDouble(), yRadius.toDouble(), radius.toDouble()
        ), Entity::isAlive).map { if (it is WaterCreatureEntity) 1.0 else 0.0 }.average()

        return (blockTypeScore+yScore+entityScore) / ((3*radius*yRadius*radius*8).toDouble()+5+1)
    }
}