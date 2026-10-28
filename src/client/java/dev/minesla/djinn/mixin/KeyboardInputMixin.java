package dev.minesla.djinn.mixin;

import dev.minesla.djinn.Djinns;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
    @Inject(method = "tick(ZF)V", at = @At("TAIL"))
    private void djinns$possessedInput(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
        var player = MinecraftClient.getInstance().player;
        if (player == null || !player.hasStatusEffect(Djinns.POSSESSION)) return;
        // A different deterministic direction every 10 ticks; applied before movement physics.
        long step = player.age / 10L;
        long noise = (step * 1103515245L + player.getId() * 12345L) & 0x7fffffffL;
        double angle = (noise % 6283) / 1000.0;
        movementForward = (float)Math.cos(angle) * .85f;
        movementSideways = (float)Math.sin(angle) * .85f;
        jumping = false; sneaking = false;
        pressingForward = movementForward > 0; pressingBack = movementForward < 0;
        pressingLeft = movementSideways > 0; pressingRight = movementSideways < 0;
        player.setSprinting(false);
    }
}
