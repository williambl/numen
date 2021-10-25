package com.williambl.numen.spells

import com.williambl.numen.gods.AgriculturalGod
import com.williambl.numen.gods.God
import com.williambl.numen.gods.Gods
import com.williambl.numen.gods.Gods.getFavour
import com.williambl.numen.gods.Gods.modifyFavour
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
import kotlin.math.min

object WeatherControllingSpell: Spell() {
    override val relevantGods: List<God> = listOf(Gods.WEATHER)

    override fun matches(text: String): Boolean {
        return text.startsWith("weder")
    }

    override fun getData(text: String): NbtCompound {
        return NbtCompound().apply {
            val arg = text.split(' ').getOrElse(1) { "" }
            putByte("Operation", when (arg) {
                "seli" -> Operation.SUN.ordinal
                "preci" -> Operation.PRECIPITATION.ordinal
                "dun" -> Operation.THUNDER.ordinal
                else -> -1
            }.toByte())
        }
    }

    override fun run(world: World, player: PlayerEntity, data: NbtCompound) {
        val favour = player.getFavour(Gods.WEATHER)
        if (favour <= 0.5) {
            return
        }

        val operationOrdinal = data.getByte("Operation").toInt()
        if (operationOrdinal < 0) {
            return
        }

        val operation = Operation.values()[operationOrdinal]

        if (world is ServerWorld) {
            world.setWeather(operation.clearDuration, operation.rainDuration, operation.isRaining, operation.isThundering)
        }

        relevantGods.forEach { player.modifyFavour(it) { i -> i - min(i*0.4, 3.0) } }
    }

    override fun onTick(attachedTo: LivingEntity, data: NbtCompound): Boolean {
        if (data.getInt("TicksRemaining") == 0) {
            if (attachedTo is PlayerEntity) {
                return true
            }
        }
        if (attachedTo.world !is ServerWorld) {
            return false
        }
        if (attachedTo.world.random.nextDouble() >= 0.1) {
            return false
        }

        AgriculturalGod.fertiliseAroundPlayer(attachedTo, 2+min(data.getDouble("Favour").toInt()*5, 10), 3+min(data.getDouble("Favour").toInt(), 4), 0.05)
        data.putInt("TicksRemaining", data.getInt("TicksRemaining") - 1)
        return false
    }

    private enum class Operation(val clearDuration: Int, val rainDuration: Int, val isRaining: Boolean, val isThundering: Boolean) {
        SUN(6000, 0, false, false),
        PRECIPITATION(0, 6000, true, false),
        THUNDER(0, 6000, true, true)
    }
}