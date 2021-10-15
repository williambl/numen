package com.williambl.numen.gods

import com.williambl.numen.gods.sacrifice.EnvironmentEvaluator
import com.williambl.numen.gods.sacrifice.SacrificialVictimEvaluator
import com.williambl.numen.gods.sacrifice.VotiveOfferingEvaluator
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.poi.PointOfInterestType

abstract class God: EnvironmentEvaluator, SacrificialVictimEvaluator, VotiveOfferingEvaluator {
    private val translationKey: String by lazy { Util.createTranslationKey("god", Gods.REGISTRY.getId(this)) }

    open fun getName() = TranslatableText(translationKey)

    abstract val pointOfInterestType: PointOfInterestType
    abstract fun onPlayerTick(world: ServerWorld, player: ServerPlayerEntity, favour: Double): Double
    abstract fun onSacrifice(world: World, pos: BlockPos, sacrificer: PlayerEntity, sacrificed: LivingEntity)
    abstract fun onVotive(world: World, pos: BlockPos, sacrificer: PlayerEntity, offering: ItemStack)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as God

        if (pointOfInterestType != other.pointOfInterestType) return false

        return true
    }

    override fun hashCode(): Int {
        return pointOfInterestType.hashCode()
    }
}