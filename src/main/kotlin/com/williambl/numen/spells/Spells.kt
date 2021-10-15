package com.williambl.numen.spells

import com.mojang.brigadier.arguments.StringArgumentType
import com.sun.jdi.connect.Connector
import com.williambl.numen.gods.God
import com.williambl.numen.gods.component.GodFavourComponent
import com.williambl.numen.id
import com.williambl.numen.spells.component.AttachedSpellsComponent
import com.williambl.numen.spells.component.PlayerAttachedSpellsComponent
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.command.CommandManager
import net.minecraft.util.registry.Registry

object Spells: EntityComponentInitializer {
    val REGISTRY = FabricRegistryBuilder.createSimple(Spell::class.java, id("spells")).buildAndRegister()

    val CROP_GROWING = Registry.register(REGISTRY, id("crop_growing"), CropGrowingSpell)

    fun init() {
        CommandRegistrationCallback.EVENT.register { dispatcher, dedicated ->
            dispatcher.register(CommandManager.literal("runspell").then(
                CommandManager.argument("spell", StringArgumentType.greedyString()).executes { ctx ->
                    val spellText = StringArgumentType.getString(ctx, "spell")
                    val spell = REGISTRY.find { it.matches(spellText) } ?: return@executes 0
                    spell.run(ctx.source.world, ctx.source.player, spell.getData(spellText))
                    return@executes 0
                }
            ))
        }
    }

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerForPlayers(
            AttachedSpellsComponent.KEY,
            ::PlayerAttachedSpellsComponent, RespawnCopyStrategy.ALWAYS_COPY)
    }


    fun PlayerEntity.getSpellsComponent(): AttachedSpellsComponent = AttachedSpellsComponent.KEY[this]
    fun PlayerEntity.attachSpell(spell: Spell, data: NbtCompound) = AttachedSpellsComponent.KEY[this].add(Pair(spell, data))
    fun PlayerEntity.removeSpell(toRemove: Spell) = AttachedSpellsComponent.KEY[this].removeIf { (spell) -> spell == toRemove }
}