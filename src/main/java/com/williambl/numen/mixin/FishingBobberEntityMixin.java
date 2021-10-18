package com.williambl.numen.mixin;

import com.williambl.numen.gods.OceanicGod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityMixin {
    @Shadow @Final @Mutable private int luckOfTheSeaLevel;

    @Shadow @Final @Mutable private int lureLevel;

    @Inject(method = "<init>(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;II)V", at = @At(value = "TAIL"))
    private void modifyLuckOfTheSea(PlayerEntity thrower, World world, int lureLevel, int luckOfTheSeaLevel, CallbackInfo ci) {
        this.luckOfTheSeaLevel += OceanicGod.getLuckOfTheSeaModifier(thrower);
        this.lureLevel += OceanicGod.getLureModifier(thrower);
    }
}
