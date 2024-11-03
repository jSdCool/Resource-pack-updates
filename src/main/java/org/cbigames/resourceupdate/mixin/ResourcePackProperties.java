package org.cbigames.resourceupdate.mixin;

import net.minecraft.server.MinecraftServer;
import org.cbigames.resourceupdate.ResourcePackUpdates;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.ServerResourcePackProperties.class)
public class ResourcePackProperties {
    @Shadow @Final private String hash;

    @Inject(at = @At("HEAD"), method = "hash", cancellable = true)
    private void hash(CallbackInfoReturnable<String> cir){
        //check if resource pack hash has changed
        //ResourcePackUpdates.LOGGER.info("current hash: "+ResourcePackUpdates.globalConfig.getHash());
        cir.setReturnValue(ResourcePackUpdates.globalConfig.getHash());
    }
}
