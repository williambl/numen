package com.williambl.numen.gods

import com.williambl.numen.decay
import com.williambl.numen.gods.Gods.getFavourComponent
import com.williambl.numen.gods.sacrifice.NatureEnvironmentEvaluator
import com.williambl.numen.id
import net.fabricmc.fabric.api.`object`.builder.v1.world.poi.PointOfInterestHelper
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.*
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.HostileEntity
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
import kotlin.streams.asSequence

object AgriculturalGod: God() {
    override val pointOfInterestType: PointOfInterestType = PointOfInterestHelper.register(id("agricultural_altar"), 0, 1, Blocks.AMETHYST_BLOCK)

    override fun onPlayerTick(world: ServerWorld, player: ServerPlayerEntity, favour: Double): Double {
        if (world.random.nextDouble() < 0.0005*favour) {
            fertiliseAroundPlayer(player, 25, 3, 0.04) { (_, state) -> state.block is CropBlock || state.block is SugarCaneBlock || state.block is SaplingBlock }
        }
        return decay(favour, 2.0)
    }

    override fun onSacrifice(world: World, pos: BlockPos, sacrificer: PlayerEntity, sacrificed: LivingEntity) {
        sacrificer.getFavourComponent().modifyFavour(this) { fv -> fv + evaluate(sacrificed) * 0.2 * evaluate(world, pos, 20, 3) }
    }

    override fun onVotive(world: World, pos: BlockPos, sacrificer: PlayerEntity, offering: ItemStack) {
        sacrificer.getFavourComponent().modifyFavour(this) { fv -> fv + evaluate(offering) * evaluate(world, pos, 20, 3) }
    }

    override fun evaluate(world: World, centre: BlockPos, radius: Int, yRadius: Int): Double {
        return NatureEnvironmentEvaluator.evaluate(world, centre, radius, yRadius)
    }

    override fun evaluate(victim: LivingEntity): Double {
        if (victim.isUndead || victim !is AnimalEntity) {
            return 0.0
        }
        var result = victim.maxHealth.toDouble()

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
            return foodComponent.hunger.toDouble()*0.02
        } else if (FabricToolTags.HOES.contains(offering.item) && offering.item is ToolItem) {
            return (offering.item as ToolItem).material.run { enchantability * 0.05 + (offering.damage - offering.damage) * 0.02 }
        }
        return 0.0
    }

    fun fertiliseAroundPlayer(player: LivingEntity, horizontalRadius: Int, verticalRadius: Int, perBlockChance: Double, predicate: (Pair<BlockPos, BlockState>) -> Boolean = { true }) {
        BlockPos.streamOutwards(player.blockPos, horizontalRadius, verticalRadius, horizontalRadius).asSequence()
            .map { Pair(it, player.world.getBlockState(it)) }
            .filter { it.second.block is Fertilizable }
            .filter(predicate)
            .filter { player.world.random.nextDouble() < perBlockChance }
            .forEach { (pos, state) ->
                (state.block as Fertilizable).grow(player.world as ServerWorld, player.random, pos, state)
                player.world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, pos, 0)
            }
    }
}