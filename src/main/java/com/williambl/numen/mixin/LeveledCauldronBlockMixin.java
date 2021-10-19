package com.williambl.numen.mixin;

import com.williambl.numen.spells.Spells;
import com.williambl.numen.spells.tablet.infusion.InfusionCauldronBlock;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.state.property.IntProperty;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Objects;

@Mixin(LeveledCauldronBlock.class)
public abstract class LeveledCauldronBlockMixin extends AbstractCauldronBlock {
    @Shadow @Final public static IntProperty LEVEL;

    public LeveledCauldronBlockMixin(Settings settings, Map<Item, CauldronBehavior> behaviorMap) {
        super(settings, behaviorMap);
    }

    @Inject(method = "onEntityCollision", at=@At("TAIL"))
    void onTabletPutInCauldron(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (this == Blocks.WATER_CAULDRON) {
            if (entity instanceof ItemEntity && ItemTags.FLOWERS.contains(((ItemEntity) entity).getStack().getItem()) && this.isEntityTouchingFluid(state, pos, entity)) {
                world.setBlockState(pos, Spells.INSTANCE.getINFUSION_CAULDRON_BLOCK().getDefaultState().with(LEVEL, state.get(LEVEL)));
                ((InfusionCauldronBlock.InfusionCauldronBlockEntity) Objects.requireNonNull(world.getBlockEntity(pos))).getInfusions().put(((ItemEntity) entity).getStack().getItem(), 1);
            }
        }
    }
}
