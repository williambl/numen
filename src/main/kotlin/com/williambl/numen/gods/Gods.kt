package com.williambl.numen.gods

import com.williambl.numen.gods.component.GodFavourComponent
import com.williambl.numen.gods.component.PlayerGodFavourComponent
import com.williambl.numen.id
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.poi.PointOfInterestStorage
import net.minecraft.world.poi.PointOfInterestType
import kotlin.streams.asSequence

object Gods: EntityComponentInitializer {
    val REGISTRY = FabricRegistryBuilder.createSimple(God::class.java, id("gods")).buildAndRegister()

    val AGRICULTURAL = Registry.register(REGISTRY, id("agricultural"), AgriculturalGod)

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

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerForPlayers(GodFavourComponent.KEY, ::PlayerGodFavourComponent, RespawnCopyStrategy.ALWAYS_COPY)
    }

    @JvmStatic
    fun onItemEntityDestroyed(entity: ItemEntity) {
        val world = entity.world
        if (world !is ServerWorld) {
            return
        }

        world.pointOfInterestStorage.getInCircle({true}, entity.blockPos, 2, PointOfInterestStorage.OccupationStatus.ANY)
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

                world.getClosestPlayer(entity, 10.0)?.let { player -> god.onVotive(world, pos, player, entity.stack) }
            }
    }

    fun PlayerEntity.getFavourComponent(): GodFavourComponent = GodFavourComponent.KEY[this]
    fun PlayerEntity.getFavour(god: God): Double = GodFavourComponent.KEY[this][god] ?: 0.0 //we know it won't be null because of withdefault
}