package com.williambl.numen.gods.component

import com.williambl.numen.gods.God
import com.williambl.numen.gods.Gods
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import org.jetbrains.annotations.ApiStatus

class PlayerGodFavourComponent(val owner: PlayerEntity): GodFavourComponent, PlayerComponent<GodFavourComponent>, MutableMap<God, Double> by (HashMap<God, Double>().withDefault { 0.0 }) {
    override fun readFromNbt(tag: NbtCompound) {
        tag.getCompound("Values").let { compound ->
            compound.keys.forEach { key ->
                Gods.REGISTRY.get(Identifier(key))?.let { god -> this[god] = compound.getDouble(key) }
            }
        }
    }

    override fun writeToNbt(tag: NbtCompound) {
        tag.put("Values", NbtCompound().also { compound ->
            this.entries.forEach { (god, favour) ->
                compound.putDouble(Gods.REGISTRY.getId(god).toString(), favour)
            }
        })
    }

    override fun shouldCopyForRespawn(lossless: Boolean, keepInventory: Boolean, sameCharacter: Boolean): Boolean {
        return true
    }

    override fun copyFrom(original: GodFavourComponent) {
        this.clear()
        this.putAll(original)
    }

    override fun serverTick() {
        this.entries.forEach { (god, favour) ->
            god.onPlayerTick(owner.world as ServerWorld, owner as ServerPlayerEntity, favour)
        }
    }
}