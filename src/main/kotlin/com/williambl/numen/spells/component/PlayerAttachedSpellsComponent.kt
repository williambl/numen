package com.williambl.numen.spells.component

import com.williambl.numen.spells.SpellData
import com.williambl.numen.spells.Spells
import dev.onyxstudios.cca.api.v3.component.CopyableComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.*
import net.minecraft.util.Identifier

class PlayerAttachedSpellsComponent(val owner: PlayerEntity): AttachedSpellsComponent, CopyableComponent<AttachedSpellsComponent>, MutableList<SpellData> by mutableListOf() {
    override fun readFromNbt(tag: NbtCompound) {
        this.addAll(tag.getList("Spells", NbtElement.COMPOUND_TYPE.toInt())
            .map { it as NbtCompound }
            .mapNotNull { Spells.REGISTRY.get(Identifier(it.getString("Id")))?.let { spell -> Pair(spell, it.getCompound("Data")) } })
    }

    override fun writeToNbt(tag: NbtCompound) {
        this.clear()
        tag.put("Spells", NbtList().also { list -> list.addAll(this.map { (spell, data) -> NbtCompound().apply {
            putString("Id", Spells.REGISTRY.getId(spell).toString())
            put("Data", data)
        } }) })
    }

    override fun copyFrom(original: AttachedSpellsComponent) {
        this.clear()
        this.addAll(original)
    }

    override fun tick() {
        this.forEach { (spell, data) ->
            spell.onTick(owner, data)
        }
    }
}