package com.williambl.numen.spells

import com.williambl.numen.gods.AgriculturalGod
import com.williambl.numen.gods.God
import com.williambl.numen.gods.Gods
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World

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

    override fun onTick(attachedTo: LivingEntity) {
        if (attachedTo.world !is ServerWorld) {
            return
        }
        if (attachedTo.world.random.nextDouble() >= 0.1) {
            return
        }

        AgriculturalGod.fertiliseAroundPlayer(attachedTo, 25, 3, 0.0015)
    }
}