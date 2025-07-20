package io.github.drag0n1zed.retinoforge.mixin;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = Window.class)
public abstract class WindowHintsMixin {

    /**
     * Redirects the *last* call to glfwWindowHint in the Window constructor.
     * This avoids the conflict with Oculus/Iris (which redirects glfwDefaultWindowHints)
     * and works around the rule preventing injections in the middle of a constructor.
     *
     * @param hint The first parameter of the original glfwWindowHint call.
     * @param value The second parameter of the original glfwWindowHint call.
     */
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V",
                    // Ordinal '5' targets the 6th call to this method (ordinals are 0-indexed),
                    // which is the last one before the window is created in the source code.
                    ordinal = 5,
                    // We use remap = false because this is an LWJGL method, not a Minecraft one.
                    remap = false
            )
    )
    private void addRetinaHint(int hint, int value) {
        // First, execute the original, redirected call so that its hint is not lost.
        GLFW.glfwWindowHint(hint, value);

        // Only add custom hint if on macOS
        if (Minecraft.ON_OSX) {
            GLFW.glfwWindowHint(GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW.GLFW_FALSE);
        }
    }
}