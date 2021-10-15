package com.williambl.numen.spells

import com.williambl.numen.gods.AgriculturalGod
import com.williambl.numen.gods.God
import com.williambl.numen.gods.Gods
import com.williambl.numen.spells.Spells.attachSpell
import com.williambl.numen.spells.Spells.removeSpell
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World

object CropGrowingSpell: Spell() {
    override val relevantGods: List<God> = listOf(Gods.AGRICULTURAL)

    override fun matches(text: String): Boolean {
        return text.startsWith("colo")
    }

    override fun getData(text: String): NbtCompound {
        return NbtCompound()
    }

    override fun run(world: World, player: PlayerEntity, data: NbtCompound) {
        player.attachSpell(this, NbtCompound().apply { putInt("TicksRemaining", 200) })
    }

    override fun onTick(attachedTo: LivingEntity, data: NbtCompound) {
        if (data.getInt("TicksRemaining") == 0) {
            if (attachedTo is PlayerEntity) {
                attachedTo.removeSpell(this)
                return
            }
        }
        if (attachedTo.world !is ServerWorld) {
            return
        }
        if (attachedTo.world.random.nextDouble() >= 0.1) {
            return
        }

        AgriculturalGod.fertiliseAroundPlayer(attachedTo, 10, 3, 0.05)
        data.putInt("TicksRemaining", data.getInt("TicksRemaining") - 1)
    }
}