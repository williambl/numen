package com.williambl.numen.gods

import com.williambl.numen.decay
import com.williambl.numen.gods.Gods.getFavourComponent
import com.williambl.numen.gods.sacrifice.NatureEnvironmentEvaluator
import com.williambl.numen.id
import net.fabricmc.fabric.api.`object`.builder.v1.world.poi.PointOfInterestHelper
import net.minecraft.entity.EntityType
import net.minecraft.entity.LightningEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.poi.PointOfInterestType
import kotlin.math.min

object WeatherGod: God() {
    override val pointOfInterestType: PointOfInterestType = PointOfInterestHelper.register(id("weather_altar"), 0, 1, Gods.WEATHER_ALTAR)

    override fun onPlayerTick(world: ServerWorld, player: ServerPlayerEntity, favour: Double): Double {
        return decay(favour, 3.0)
    }

    override fun onSacrifice(world: World, pos: BlockPos, sacrificer: PlayerEntity, sacrificed: LivingEntity) {
        sacrificer.getFavourComponent().modifyFavour(this) { fv -> fv + evaluate(sacrificed) * 0.2 * evaluate(world, pos, 20, 3) }

        if (world.random.nextDouble() < 0.2) {
            world.spawnEntity(LightningEntity(EntityType.LIGHTNING_BOLT, world).apply { setPosition(pos.x+0.5, pos.y+0.0, pos.z+0.5) })
        }
    }

    override fun onVotive(world: World, pos: BlockPos, sacrificer: PlayerEntity, offering: ItemStack) {
        sacrificer.getFavourComponent().modifyFavour(this) { fv -> fv + evaluate(offering) * evaluate(world, pos, 20, 3) }
        if (world.random.nextDouble() < 0.2) {
            world.spawnEntity(LightningEntity(EntityType.LIGHTNING_BOLT, world).apply { setPosition(pos.x+0.5, pos.y+0.0, pos.z+0.5) })
        }
    }

    override fun evaluate(world: World, centre: BlockPos, radius: Int, yRadius: Int): Double {
        return NatureEnvironmentEvaluator.evaluate(world, centre, radius, yRadius) * min(1.0, (1.0/((world.height*0.5)-world.bottomY))*(centre.y - (world.height*0.5))) * if (world.isSkyVisible(centre)) 1.0 else 0.1
    }

    override fun evaluate(victim: LivingEntity): Double {
        var result = victim.maxHealth.toDouble()

        if (victim is HostileEntity) {
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
            } else if (damageSource == DamageSource.LIGHTNING_BOLT) {
                result *= 4
            }
        }

        return result
    }

    override fun evaluate(offering: ItemStack): Double {
        val foodComponent = offering.item.foodComponent
        if (foodComponent != null && foodComponent.isMeat) {
            return foodComponent.hunger.toDouble()*0.02
        }
        return 0.0
    }
}