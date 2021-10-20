package com.williambl.numen.spells

import com.mojang.brigadier.arguments.StringArgumentType
import com.williambl.numen.id
import com.williambl.numen.mixin.LeveledCauldronBlockMixin
import com.williambl.numen.registerBlockAndItem
import com.williambl.numen.spells.component.AttachedSpellsComponent
import com.williambl.numen.spells.component.PlayerAttachedSpellsComponent
import com.williambl.numen.spells.tablet.EditClayTabletGuiDescription
import com.williambl.numen.spells.tablet.FiredClayTabletItem
import com.williambl.numen.spells.tablet.WritableClayTabletItem
import com.williambl.numen.spells.tablet.infusion.InfusionCauldronBlock
import com.williambl.numen.spells.tablet.infusion.InfusionCauldronBlock.InfusionCauldronBlockEntity
import com.williambl.numen.spells.tablet.setTabletText
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.block.cauldron.CauldronBehavior
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.resource.ResourceType
import net.minecraft.server.command.CommandManager
import net.minecraft.tag.ItemTags
import net.minecraft.util.ActionResult
import net.minecraft.util.registry.Registry
import java.util.*

object Spells: EntityComponentInitializer {
    val REGISTRY = FabricRegistryBuilder.createSimple(Spell::class.java, id("spells")).buildAndRegister()

    val CROP_GROWING = Registry.register(REGISTRY, id("crop_growing"), CropGrowingSpell)

    val WRITABLE_TABLET = Registry.register(Registry.ITEM, id("writable_tablet"), WritableClayTabletItem)
    val FIRED_TABLET = Registry.register(Registry.ITEM, id("fired_tablet"), FiredClayTabletItem)

    val EDIT_CLAY_TABLET_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(id("edit_clay_tablet")) { syncId, inv -> EditClayTabletGuiDescription(syncId, inv) }

    val INFUSION_CAULDRON_BLOCK = registerBlockAndItem(id("infusion_cauldron"), InfusionCauldronBlock)
    val INFUSION_CAULDRON_BE_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("infusion_cauldron"), BlockEntityType.Builder.create(
        InfusionCauldronBlock::InfusionCauldronBlockEntity,
        INFUSION_CAULDRON_BLOCK
    ).build(null))

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

        ServerPlayNetworking.registerGlobalReceiver(id("edit_clay_tablet")) { server, player, handler, buf, response ->
            val text = buf.readString()
            server.execute { player.mainHandStack.setTabletText(text) }
        }

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(InfusionCauldronBlock.resourceReloadListener)
    }

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerForPlayers(
            AttachedSpellsComponent.KEY,
            ::PlayerAttachedSpellsComponent, RespawnCopyStrategy.ALWAYS_COPY)
    }

    @JvmStatic
    fun fireClayTablet(entity: ItemEntity) {
        val oldStack = entity.stack
        entity.stack = ItemStack(FIRED_TABLET).apply {
            nbt = oldStack.nbt
        }
    }

    fun PlayerEntity.getSpellsComponent(): AttachedSpellsComponent = AttachedSpellsComponent.KEY[this]
    fun PlayerEntity.attachSpell(spell: Spell, data: NbtCompound) = AttachedSpellsComponent.KEY[this].add(Pair(spell, data))
    fun PlayerEntity.removeSpell(toRemove: Spell) = AttachedSpellsComponent.KEY[this].removeIf { (spell) -> spell == toRemove }
}