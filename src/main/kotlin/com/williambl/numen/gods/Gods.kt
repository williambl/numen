package com.williambl.numen.gods

import com.williambl.numen.gods.component.GodFavourComponent
import com.williambl.numen.gods.component.PlayerGodFavourComponent
import com.williambl.numen.gods.sacrifice.AltarBlock
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
import net.minecraft.command.argument.ArgumentTypes
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.TranslatableText
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
    val FIRE_ALTAR = registerBlockAndItem(id("fire_altar"), AltarBlock(AbstractBlock.Settings.of(Material.STONE, MapColor.GRAY)))

    val AGRICULTURAL = Registry.register(REGISTRY, id("agricultural"), AgriculturalGod)
    val OCEANIC = Registry.register(REGISTRY, id("oceanic"), OceanicGod)
    val WEATHER = Registry.register(REGISTRY, id("weather"), WeatherGod)
    val BATTLE = Registry.register(REGISTRY, id("battle"), BattleGod)
    val FIRE = Registry.register(REGISTRY, id("fire"), FireGod)

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

        ArgumentTypes.register("numen:god", GodArgumentType::class.java, GodArgumentType.Serialiser)

        CommandRegistrationCallback.EVENT.register { dispatcher, dedicated ->
            dispatcher.register(literal("numen").then(literal("god")
                .then(literal("favour")
                    .then(argument("god", GodArgumentType.ability())
                        .then(literal("get")
                            .executes { ctx ->
                                val god = GodArgumentType.getAbility(ctx, "god")
                                val favour = ctx.source.player.getFavour(god)
                                ctx.source.sendFeedback(TranslatableText("gui.ender_soul.favour", god, favour), false)
                                return@executes (favour * 1000).toInt()
                            }
                        )
                    )
                )
            ))
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
    fun PlayerEntity.getFavour(god: God): Double = getFavourComponent()[god] ?: 0.0
    fun PlayerEntity.addFavour(god: God, amount: Double) = getFavourComponent().addFavour(god, amount)
    fun PlayerEntity.modifyFavour(god: God, modifier: (Double) -> Double) = getFavourComponent().modifyFavour(god, modifier)
}