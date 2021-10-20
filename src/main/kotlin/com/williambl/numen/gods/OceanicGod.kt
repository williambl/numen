package com.williambl.numen.gods

import com.williambl.numen.decay
import com.williambl.numen.gods.Gods.getFavour
import com.williambl.numen.gods.Gods.getFavourComponent
import com.williambl.numen.gods.sacrifice.NatureEnvironmentEvaluator
import com.williambl.numen.gods.sacrifice.OceanicEnvironmentEvaluator
import com.williambl.numen.id
import net.fabricmc.fabric.api.`object`.builder.v1.world.poi.PointOfInterestHelper
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.WaterCreatureEntity
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolItem
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.tag.ItemTags
import net.minecraft.text.LiteralText
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldEvents
import net.minecraft.world.poi.PointOfInterestType
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.streams.asSequence

object OceanicGod: God() {
    override val pointOfInterestType: PointOfInterestType = PointOfInterestHelper.register(id("oceanic_altar"), 0, 1, Gods.OCEANIC_ALTAR)

    override fun onPlayerTick(world: ServerWorld, player: ServerPlayerEntity, favour: Double): Double {
        return decay(favour, 1.2)
    }

    override fun onSacrifice(world: World, pos: BlockPos, sacrificer: PlayerEntity, sacrificed: LivingEntity) {
        sacrificer.getFavourComponent().modifyFavour(this) { fv -> fv + evaluate(sacrificed) * 0.2 * evaluate(world, pos, 20, 3) }
    }

    override fun onVotive(world: World, pos: BlockPos, sacrificer: PlayerEntity, offering: ItemStack) {
        sacrificer.getFavourComponent().modifyFavour(this) { fv -> fv + evaluate(offering) * evaluate(world, pos, 20, 3) }
    }

    override fun evaluate(world: World, centre: BlockPos, radius: Int, yRadius: Int): Double {
        return OceanicEnvironmentEvaluator.evaluate(world, centre, radius, yRadius)
    }

    override fun evaluate(victim: LivingEntity): Double {
        var result = victim.maxHealth.toDouble()

        if (victim is WaterCreatureEntity) {
            result *= 1.4
        }

        if (victim.isBaby) {
            result *= 1.1
        }

        if (victim.isOnFire) {
            result *= 1.2
        }

        victim.recentDamageSource?.let { damageSource ->
            if (damageSource.isFallingBlock || damageSource.isMagic || damageSource.isFromFalling || damageSource.isProjectile || damageSource.isOutOfWorld || damageSource.isExplosive) {
                result *= 0.4
            }
        }

        return result
    }

    override fun evaluate(offering: ItemStack): Double {
        val foodComponent = offering.item.foodComponent
        if (foodComponent != null && foodComponent.isMeat) {
            return foodComponent.hunger.toDouble()*0.02*(if (ItemTags.FISHES.contains(offering.item)) 3.0 else 1.0)
        }
        return 0.0
    }

    @JvmStatic
    fun getLuckOfTheSeaModifier(player: PlayerEntity): Int {
        return player.getFavour(this).roundToInt() / 3
    }

    @JvmStatic
    fun getLureModifier(player: PlayerEntity): Int {
        return player.getFavour(this).roundToInt()
    }
}