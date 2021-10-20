package com.williambl.numen.gods

import com.williambl.numen.gods.component.GodFavourComponent
import com.williambl.numen.gods.component.PlayerGodFavourComponent
import com.williambl.numen.gods.sacrifice.AltarBlock
import com.williambl.numen.gods.sacrifice.ChthonicEnvironmentEvaluator
import com.williambl.numen.gods.sacrifice.NatureEnvironmentEvaluator
import com.williambl.numen.gods.sacrifice.OceanicEnvironmentEvaluator
import com.williambl.numen.id
import com.williambl.numen.registerBlockAndItem
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.block.AbstractBlock
import net.minecraft.block.MapColor
import net.minecraft.block.Material
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.CommandManager
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.LiteralText
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.poi.PointOfInterestStorage
import kotlin.streams.asSequence

object Gods: EntityComponentInitializer {
    val REGISTRY = FabricRegistryBuilder.createSimple(God::class.java, id("gods")).buildAndRegister()

    val AGRICULTURAL_ALTAR = registerBlockAndItem(id("agricultural_altar"), AltarBlock(AbstractBlock.Settings.of(Material.STONE, MapColor.GRAY)))
    val OCEANIC_ALTAR = registerBlockAndItem(id("oceanic_altar"), AltarBlock(AbstractBlock.Settings.of(Material.STONE, MapColor.GRAY)))
    val WEATHER_ALTAR = registerBlockAndItem(id("weather_altar"), AltarBlock(AbstractBlock.Settings.of(Material.STONE, MapColor.GRAY)))
    val BATTLE_ALTAR = registerBlockAndItem(id("battle_altar"), AltarBlock(AbstractBlock.Settings.of(Material.STONE, MapColor.GRAY)))

    val AGRICULTURAL = Registry.register(REGISTRY, id("agricultural"), AgriculturalGod)
    val OCEANIC = Registry.register(REGISTRY, id("oceanic"), OceanicGod)
    val WEATHER = Registry.register(REGISTRY, id("weather"), WeatherGod)
    val BATTLE = Registry.register(REGISTRY, id("battle"), WeatherGod)

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

        CommandRegistrationCallback.EVENT.register { dispatcher, dedicated ->
            dispatcher.register(CommandManager.literal("evaluate_naturalness").executes { ctx ->
                val pos = BlockPos(ctx.source.position)
                val world = ctx.source.world
                ctx.source.sendFeedback(
                    LiteralText(NatureEnvironmentEvaluator.evaluate(world, pos, 25, 3).toString()),
                    false
                )
                return@executes 0
            })
            dispatcher.register(CommandManager.literal("evaluate_chthonicness").executes { ctx ->
                val pos = BlockPos(ctx.source.position)
                val world = ctx.source.world
                ctx.source.sendFeedback(
                    LiteralText(
                        ChthonicEnvironmentEvaluator.evaluate(world, pos, 25, 3).toString()
                    ), false
                )
                return@executes 0
            })
            dispatcher.register(CommandManager.literal("evaluate_oceanicness").executes { ctx ->
                val pos = BlockPos(ctx.source.position)
                val world = ctx.source.world
                ctx.source.sendFeedback(
                    LiteralText(OceanicEnvironmentEvaluator.evaluate(world, pos, 25, 3).toString()),
                    false
                )
                return@executes 0
            })
            dispatcher.register(CommandManager.literal("favour").executes { ctx ->
                try {
                    ctx.source.sendFeedback(LiteralText(ctx.source.player.getFavour(AGRICULTURAL).toString()), false)
                } catch (e: Exception) {
                    println(e)
                }
                return@executes 0
            })
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