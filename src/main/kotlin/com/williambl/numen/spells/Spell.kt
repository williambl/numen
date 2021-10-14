package com.williambl.numen.spells

import com.williambl.numen.gods.God
import com.williambl.numen.gods.sacrifice.EnvironmentEvaluator
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.poi.PointOfInterestType

abstract class Spell {
    private val translationKey: String by lazy { Util.createTranslationKey("spell", Spells.REGISTRY.getId(this)) }

    open fun getName() = TranslatableText(translationKey)

    abstract val relevantGods: List<God>
    abstract fun matches(text: String): Boolean
    abstract fun getData(text: String): NbtCompound
    abstract fun run(world: World, player: PlayerEntity, data: NbtCompound)
    abstract fun onTick(attachedTo: LivingEntity)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Spell

        if (relevantGods != other.relevantGods) return false

        return true
    }

    override fun hashCode(): Int {
        return relevantGods.hashCode()
    }
}