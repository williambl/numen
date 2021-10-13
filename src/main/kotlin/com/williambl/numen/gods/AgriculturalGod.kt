package com.williambl.numen.gods

import com.williambl.numen.gods.sacrifice.NatureEnvironmentEvaluator
import com.williambl.numen.id
import net.fabricmc.fabric.api.`object`.builder.v1.world.poi.PointOfInterestHelper
import net.minecraft.block.Blocks
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.poi.PointOfInterestType

class AgriculturalGod: God() {
    override val pointOfInterestType: PointOfInterestType = PointOfInterestHelper.register(id("agricultural_altar"), 0, 1, Blocks.AMETHYST_BLOCK);

    override fun onSacrifice(world: World, pos: BlockPos, sacrificer: PlayerEntity, sacrificed: LivingEntity) {
        sacrificer.sendMessage(LiteralText("hmm.. I rate this sacrifice ${evaluate(world, pos, 25, 3) * 10}/10."), false)
    }

    override fun onVotive(world: World, pos: BlockPos, sacrificer: PlayerEntity, offering: ItemStack) {
    }

    override fun evaluate(world: World, centre: BlockPos, radius: Int, yRadius: Int): Double {
        return NatureEnvironmentEvaluator.evaluate(world, centre, radius, yRadius)
    }
}