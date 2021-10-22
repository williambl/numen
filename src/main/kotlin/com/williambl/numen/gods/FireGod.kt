package com.williambl.numen.gods

import com.williambl.numen.BETTER_PROCESSED_STONE
import com.williambl.numen.PROCESSED_STONE
import com.williambl.numen.decay
import com.williambl.numen.gods.Gods.getFavourComponent
import com.williambl.numen.id
import net.fabricmc.fabric.api.`object`.builder.v1.world.poi.PointOfInterestHelper
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Blocks
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolItem
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.poi.PointOfInterestType
import kotlin.streams.asSequence

object FireGod: God() {
    override val pointOfInterestType: PointOfInterestType = PointOfInterestHelper.register(id("fire_altar"), 0, 1, Gods.FIRE_ALTAR)

    override fun onPlayerTick(world: ServerWorld, player: ServerPlayerEntity, favour: Double): Double {
        return decay(favour, 4.0)
    }

    override fun onSacrifice(world: World, pos: BlockPos, sacrificer: PlayerEntity, sacrificed: LivingEntity) {
        sacrificer.getFavourComponent().modifyFavour(this) { fv -> fv + evaluate(sacrificed) * 1.2 * evaluate(world, pos, 20, 3) }

        if (world.random.nextDouble() < 0.2 && world.getBlockState(pos.up()).isAir) {
            world.setBlockState(pos.up(), Blocks.FIRE.defaultState)
        }
    }

    override fun onVotive(world: World, pos: BlockPos, sacrificer: PlayerEntity, offering: ItemStack) {
        sacrificer.getFavourComponent().modifyFavour(this) { fv -> fv + evaluate(offering) * evaluate(world, pos, 20, 3) }
        if (world.random.nextDouble() < 0.2 && world.getBlockState(pos.up()).isAir) {
            world.setBlockState(pos.up(), Blocks.FIRE.defaultState)
        }
    }

    override fun evaluate(world: World, centre: BlockPos, radius: Int, yRadius: Int): Double {
        val rawScore = BlockPos.streamOutwards(centre, radius, yRadius, radius).asSequence()
            .map(world::getBlockState)
            .map { state ->
                when {
                    BETTER_PROCESSED_STONE.contains(state.block) -> 3.0
                    BlockTags.FIRE.contains(state.block) -> 3.0
                    PROCESSED_STONE.contains(state.block) -> 1.0
                    state.block == Blocks.NETHERRACK -> 3.0
                    state.block == Blocks.BEDROCK -> 3.0
                    state.block == Blocks.GOLD_BLOCK -> 2.0
                    state.block == Blocks.AMETHYST_BLOCK -> 2.0
                    else -> 0.0
                }
            }
            .sum()

        return rawScore / ((3*radius*yRadius*radius*8).toDouble())
    }

    override fun evaluate(victim: LivingEntity): Double {
        var result = victim.maxHealth.toDouble()

        if (victim is AnimalEntity) {
            result *= 1.4
        }

        if (victim.isBaby) {
            result *= 1.1
        }

        if (victim.isOnFire) {
            result *= 2.0
        }

        victim.recentDamageSource?.let { damageSource ->
            if (damageSource.isFallingBlock || damageSource.isMagic || damageSource.isFromFalling || damageSource.isOutOfWorld) {
                result *= 0.4
            } else if (damageSource.isProjectile) {
                result *= 1.1
            }
        }

        return result
    }

    override fun evaluate(offering: ItemStack): Double {
        val foodComponent = offering.item.foodComponent
        if (foodComponent != null && foodComponent.isMeat) {
            return foodComponent.hunger.toDouble()*0.02
        }

        if (offering.isIn(FabricToolTags.SWORDS)) {
            return if (offering.item is ToolItem) {
                (offering.item as ToolItem).material.run { enchantability * 0.05 + (offering.damage - offering.damage) * 0.02 }
            } else {
                (offering.maxDamage - offering.damage) * 0.02
            }
        }
        return 0.0
    }
}