package com.williambl.numen.spells

import com.williambl.numen.gods.God
import com.williambl.numen.gods.Gods
import com.williambl.numen.gods.Gods.getFavour
import com.williambl.numen.gods.Gods.modifyFavour
import com.williambl.numen.spells.Spells.attachSpell
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.FireballEntity
import net.minecraft.entity.projectile.SmallFireballEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.world.World
import net.minecraft.world.WorldEvents
import kotlin.math.max
import kotlin.math.min

object FireballSpell: Spell() {
    override val relevantGods: List<God> = listOf(Gods.FIRE)

    override fun matches(text: String): Boolean {
        return text.startsWith("seli")
    }

    override fun getData(text: String): NbtCompound {
        return NbtCompound().apply {
            putBoolean("Sustained", text.contains("mute"))
        }
    }

    override fun run(world: World, player: PlayerEntity, data: NbtCompound) {
        val fireFavour = player.getFavour(Gods.FIRE)
        if (fireFavour <= 0.01) {
            return
        }

        if (data.getBoolean("Sustained")) {
            player.attachSpell(this, NbtCompound().apply {
                putInt("FireballsRemaining", 3)
                putInt("TicksToNextFireball", 4)
                putDouble("Favour", fireFavour)
            })
        } else {
            fireFireball(world, player, fireFavour)
        }

        relevantGods.forEach { player.modifyFavour(it) { i -> i - min(i*0.2, 3.0) } }
    }

    override fun onTick(attachedTo: LivingEntity, data: NbtCompound): Boolean {
        val ticksToNextFireball = data.getInt("TicksToNextFireball")
        data.putInt("TicksToNextFireball", ticksToNextFireball - 1)
        if (ticksToNextFireball > 0) {
            return false
        }

        val fireFavour = data.getDouble("Favour")

        fireFireball(attachedTo.world, attachedTo, fireFavour)

        data.putInt("FireballsRemaining", data.getInt("FireballsRemaining") - 1)
        data.putInt("TicksToNextFireball", 10)

        if (data.getInt("FireballsRemaining") == 0) {
            return true
        }

        return false
    }

    private fun fireFireball(world: World, player: LivingEntity, fireFavour: Double) {
        val playerLook = player.rotationVector
        val fireball = if (fireFavour >= 3) {
            FireballEntity(world, player, playerLook.x, playerLook.y, playerLook.z, max(fireFavour.toInt() - 3, 6))
        } else {
            SmallFireballEntity(world, player, playerLook.x, playerLook.y, playerLook.z)
        }
        fireball.setPosition(fireball.x+playerLook.x, player.eyeY, fireball.z+playerLook.z)
        world.spawnEntity(fireball)
        world.syncWorldEvent(null, WorldEvents.BLAZE_SHOOTS, player.blockPos, 0)
    }
}