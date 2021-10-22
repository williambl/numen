package com.williambl.numen.spells.tablet.infusion

import com.williambl.numen.mixin.AbstractCauldronBlockAccessor
import com.williambl.numen.spells.Spells
import com.williambl.numen.spells.tablet.addTabletInfusions
import com.williambl.numen.spells.tablet.readInfusions
import com.williambl.numen.spells.tablet.setTabletInfusions
import com.williambl.numen.spells.tablet.write
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.LeveledCauldronBlock
import net.minecraft.block.cauldron.CauldronBehavior
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.resource.ServerResourceManager
import net.minecraft.server.MinecraftServer
import net.minecraft.sound.SoundEvents
import net.minecraft.tag.ItemTags
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

val baseBehaviourMap: Map<Item, CauldronBehavior> = mapOf(
    Pair(Spells.WRITABLE_TABLET, CauldronBehavior { state, world, pos, _, _, stack ->
        stack.setTabletInfusions((world.getBlockEntity(pos) as InfusionCauldronBlock.InfusionCauldronBlockEntity).infusions.toMap())
        (world.getBlockEntity(pos) as InfusionCauldronBlock.InfusionCauldronBlockEntity).infusions.clear()
        world.setBlockState(
            pos,
            Blocks.WATER_CAULDRON.defaultState.with(
                LeveledCauldronBlock.LEVEL,
                state.get(LeveledCauldronBlock.LEVEL)
            )
        )
        ActionResult.SUCCESS
    }),
    Pair(
        Items.BUCKET,
        CauldronBehavior { state, world, pos, player, hand, stack ->
            CauldronBehavior.emptyCauldron(
                state,
                world,
                pos,
                player,
                hand,
                stack,
                ItemStack(Items.WATER_BUCKET),
                { statex: BlockState ->
                    statex.get(
                        LeveledCauldronBlock.LEVEL
                    ) as Int == 3
                },
                SoundEvents.ITEM_BUCKET_FILL
            )
        }
    )
)

object InfusionCauldronBlock: LeveledCauldronBlock(
    Settings.copy(Blocks.WATER_CAULDRON),
    { false },
    baseBehaviourMap.toMutableMap().withDefault { CauldronBehavior { _, _, _, _, _, _ -> ActionResult.PASS } }
), BlockEntityProvider, ServerLifecycleEvents.ServerStarted, ServerLifecycleEvents.EndDataPackReload {
    override fun onSyncedBlockEvent(state: BlockState?, world: World, pos: BlockPos?, type: Int, data: Int): Boolean {
        super.onSyncedBlockEvent(state, world, pos, type, data)
        val blockEntity = world.getBlockEntity(pos)
        return blockEntity?.onSyncedBlockEvent(type, data) ?: false
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = InfusionCauldronBlockEntity(pos, state)

    override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
        super.onEntityCollision(state, world, pos, entity)
        if (entity is ItemEntity) {
            if (ItemTags.FLOWERS.contains(entity.stack.item) && isEntityTouchingFluid(state, pos, entity)) {
                (world.getBlockEntity(pos) as InfusionCauldronBlockEntity).infusions.compute(entity.stack.item) { _, i ->
                    (i ?: 0) + 1
                }
                entity.stack.decrement(1)
            } else if (entity.stack.isOf(Spells.WRITABLE_TABLET)) {
                entity.stack.addTabletInfusions((world.getBlockEntity(pos) as InfusionCauldronBlockEntity).infusions.toMap())
                entity.stack = entity.stack // Force datatracker to set dirty
                (world.getBlockEntity(pos) as InfusionCauldronBlockEntity).infusions.clear()
                world.setBlockState(
                    pos,
                    Blocks.WATER_CAULDRON.defaultState.with(
                        LEVEL,
                        state.get(LEVEL)
                    )
                )
            }
        }

    }

    class InfusionCauldronBlockEntity(pos: BlockPos, state: BlockState) :
        BlockEntity(Spells.INFUSION_CAULDRON_BE_TYPE, pos, state) {
        val infusions: MutableMap<Item, Int> = mutableMapOf()

        override fun writeNbt(nbt: NbtCompound): NbtCompound {
            return super.writeNbt(nbt).also {
                it.put("Infusions", infusions.write())
            }
        }

        override fun readNbt(nbt: NbtCompound) {
            super.readNbt(nbt)
            infusions.clear()
            infusions.putAll(readInfusions(nbt.getCompound("Infusions")))
        }
    }

    fun reload() {
        @Suppress("CAST_NEVER_SUCCEEDS")
        val behaviourMap = (this@InfusionCauldronBlock as AbstractCauldronBlockAccessor).behaviorMap
        behaviorMap.clear()
        behaviourMap.putAll(baseBehaviourMap)
        behaviourMap.putAll(
            ItemTags.FLOWERS.values().map { flower -> flower to
                    CauldronBehavior { _, world1, pos1, _, _, stack ->
                        @Suppress("CAST_NEVER_SUCCEEDS")
                        (world1.getBlockEntity(pos1) as InfusionCauldronBlockEntity).infusions.compute(flower) { _, i ->
                            (i ?: 0) + 1
                        }
                        stack.decrement(1)
                        ActionResult.SUCCESS
                    }
            }
        )

        ItemTags.FLOWERS.values().forEach {
            CauldronBehavior.WATER_CAULDRON_BEHAVIOR[it] = CauldronBehavior { state, world, pos, _, _, stack ->
                world.setBlockState(
                    pos,
                    this@InfusionCauldronBlock.defaultState.with(
                        LEVEL,
                        state.get(LEVEL)
                    )
                )
                (world.getBlockEntity(pos) as InfusionCauldronBlockEntity).infusions[stack.item] = 1
                stack.decrement(1)
                ActionResult.SUCCESS
            }
        }
    }

    override fun onServerStarted(server: MinecraftServer?) {
        reload()
    }

    override fun endDataPackReload(
        server: MinecraftServer?,
        serverResourceManager: ServerResourceManager?,
        success: Boolean
    ) {
        reload()
    }
}