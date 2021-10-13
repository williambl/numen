package com.williambl.numen.gods

import com.williambl.numen.id
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.poi.PointOfInterestStorage
import net.minecraft.world.poi.PointOfInterestType
import kotlin.streams.asSequence

object Gods {
    val REGISTRY = FabricRegistryBuilder.createSimple(God::class.java, id("gods")).buildAndRegister()

    val AGRICULTURAL = Registry.register(REGISTRY, id("agricultural"), AgriculturalGod())

    fun init() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register { world, entity, killed ->
            if (entity !is PlayerEntity) {
                return@register
            }
            world.pointOfInterestStorage.getInCircle({true}, killed.blockPos, 8, PointOfInterestStorage.OccupationStatus.ANY)
                .asSequence()
                .map { Pair(it.pos, REGISTRY.find { god -> it.type == god.pointOfInterestType }) }
                .filter { it.second != null }
                .map {
                    @Suppress("UNCHECKED_CAST")
                    it as Pair<BlockPos, God>
                }
                .firstOrNull()
                ?.let {
                    val pos = it.first
                    val god = it.second

                    god.onSacrifice(world, pos, entity, killed)
                }
        }
    }
}