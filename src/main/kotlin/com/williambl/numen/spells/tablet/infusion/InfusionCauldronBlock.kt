package com.williambl.numen.spells.tablet.infusion

import com.williambl.numen.id
import com.williambl.numen.mixin.AbstractCauldronBlockAccessor
import com.williambl.numen.spells.Spells
import com.williambl.numen.spells.tablet.addTabletInfusions
import com.williambl.numen.spells.tablet.readInfusions
import com.williambl.numen.spells.tablet.setTabletInfusions
import com.williambl.numen.spells.tablet.write
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.LeveledCauldronBlock
import net.minecraft.block.cauldron.CauldronBehavior
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.item.Item
import net.minecraft.nbt.NbtCompound
import net.minecraft.resource.ResourceManager
import net.minecraft.tag.ItemTags
import net.minecraft.util.ActionResult
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

val baseBehaviourMap: Map<Item, CauldronBehavior> = mapOf(
    Pair(Spells.WRITABLE_TABLET, CauldronBehavior { state, world, pos, player, hand, stack ->
        stack.setTabletInfusions((world.getBlockEntity(pos) as InfusionCauldronBlock.InfusionCauldronBlockEntity).infusions.toMap())
        (world.getBlockEntity(pos) as InfusionCauldronBlock.InfusionCauldronBlockEntity).infusions.clear()
        ActionResult.SUCCESS
    })
)

object InfusionCauldronBlock: LeveledCauldronBlock(
    Settings.copy(Blocks.WATER_CAULDRON),
    { false },
    baseBehaviourMap.toMutableMap().withDefault { CauldronBehavior { _, _, _, _, _, _ -> ActionResult.PASS } }
), BlockEntityProvider {
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
            infusions.putAll(readInfusions(nbt))
        }
    }

    val resourceReloadListener = object : SimpleSynchronousResourceReloadListener {
        override fun reload(manager: ResourceManager) {
            @Suppress("CAST_NEVER_SUCCEEDS")
            val behaviourMap = (this@InfusionCauldronBlock as AbstractCauldronBlockAccessor).behaviorMap
            behaviorMap.clear()
            behaviourMap.putAll(baseBehaviourMap)
            /*
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
                        INFUSION_CAULDRON_BLOCK.defaultState.with(
                            LeveledCauldronBlockMixin.LEVEL,
                            state.get(LeveledCauldronBlockMixin.LEVEL)
                        )
                    )
                    (world.getBlockEntity(pos) as InfusionCauldronBlockEntity).infusions[stack.item] = 1
                    stack.decrement(1)
                    ActionResult.SUCCESS
                }
            }
             */
        }

        override fun getFabricId(): Identifier = id("infusion_cauldron")

        override fun getFabricDependencies(): MutableCollection<Identifier> = mutableSetOf(ResourceReloadListenerKeys.TAGS)
    }
}