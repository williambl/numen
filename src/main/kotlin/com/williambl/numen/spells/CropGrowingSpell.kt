package com.williambl.numen.spells

import com.williambl.numen.gods.God
import com.williambl.numen.gods.Gods
import net.minecraft.block.Fertilizable
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldEvents
import kotlin.streams.asSequence

class CropGrowingSpell: Spell() {
    override val relevantGods: List<God> = listOf(Gods.AGRICULTURAL)

    override fun matches(text: String): Boolean {
        return false
    }

    override fun getData(text: String): NbtCompound {
        return NbtCompound()
    }

    override fun run(world: World, player: PlayerEntity, data: NbtCompound) {
    }

    override fun onTick(player: LivingEntity) {
        if (player.world !is ServerWorld) {
            return
        }
        if (player.world.random.nextDouble() >= 0.1) {
            return
        }
        BlockPos.streamOutwards(player.blockPos, 25, 3, 25).asSequence()
            .map { Pair(it, player.world.getBlockState(it)) }
            .filter { it.second.block is Fertilizable }
            .filter { player.world.random.nextDouble() < 0.0015 }
            .forEach { (pos, state) ->
                (state.block as Fertilizable).grow(player.world as ServerWorld, player.random, pos, state)
                player.world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, pos, 0)
            }
    }
}