package com.williambl.numen.spells

import com.williambl.numen.gods.AgriculturalGod
import com.williambl.numen.gods.God
import com.williambl.numen.gods.Gods
import com.williambl.numen.gods.Gods.getFavour
import com.williambl.numen.gods.Gods.modifyFavour
import com.williambl.numen.spells.Spells.attachSpell
import com.williambl.numen.spells.Spells.removeSpell
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.FireballEntity
import net.minecraft.entity.projectile.SmallFireballEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
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
        if (fireFavour <= 0) {
            return
        }

        val playerLook = player.rotationVector

        if (data.getBoolean("Sustained")) {
            player.attachSpell(this, NbtCompound().apply {
                putInt("FireballsRemaining", 3)
                putInt("TicksToNextFireball", 4)
                putDouble("Favour", player.getFavour(Gods.AGRICULTURAL))
            })
        } else {
            val fireball = if (fireFavour >= 3) {
                FireballEntity(world, player, playerLook.x, playerLook.y, playerLook.z, max(fireFavour.toInt() - 3, 6))
            } else {
                SmallFireballEntity(world, player, playerLook.x, playerLook.y, playerLook.z)
            }
            world.spawnEntity(fireball)
        }

        relevantGods.forEach { player.modifyFavour(it) { i -> i - min(i*0.2, 3.0) } }
    }

    override fun onTick(attachedTo: LivingEntity, data: NbtCompound) {
        val ticksToNextFireball = data.getInt("TicksToNextFireball")
        data.putInt("TicksToNextFireball", ticksToNextFireball - 1)
        if (ticksToNextFireball <= 0) {
            return
        }

        val fireFavour = data.getDouble("Favour")
        val playerLook = attachedTo.rotationVector

        val fireball = if (fireFavour >= 3) {
            FireballEntity(attachedTo.world, attachedTo, playerLook.x, playerLook.y, playerLook.z, max(fireFavour.toInt() - 3, 6))
        } else {
            SmallFireballEntity(attachedTo.world, attachedTo, playerLook.x, playerLook.y, playerLook.z)
        }

        attachedTo.world.spawnEntity(fireball)
        data.putInt("FireballsRemaining", data.getInt("FireballsRemaining") - 1)
        if (data.getInt("FireballsRemaining") == 0) {
            if (attachedTo is PlayerEntity) {
                attachedTo.removeSpell(this)
                return
            }
        }
    }
}