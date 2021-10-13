package com.williambl.numen.mixin;

import com.williambl.numen.gods.Gods;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "onItemEntityDestroyed", at = @At("HEAD"))
    void onVotiveOffering(ItemEntity entity, CallbackInfo ci) {
        Gods.onItemEntityDestroyed(entity);
    }
}
