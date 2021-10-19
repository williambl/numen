package com.williambl.numen.mixin;

import com.williambl.numen.spells.Spells;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.BlockTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow public abstract ItemStack getStack();

    @Inject(method = "tick", at=@At("HEAD"))
    void fireClayTablets(CallbackInfo ci) {
        if (this.getStack().getItem() == Spells.INSTANCE.getWRITABLE_TABLET()) {
            if (BlockTags.FIRE.contains(this.getBlockStateAtPos().getBlock())) {
                Spells.fireClayTablet((ItemEntity) (Object) this);
            }
        }
    }
}
