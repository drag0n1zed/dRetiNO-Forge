package io.github.drag0n1zed.retinoforge.mixin;

import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({Window.class})
public abstract class WindowHintsMixin {
    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/glfw/GLFW;glfwDefaultWindowHints()V"
            ),
            method = {"<init>"}
    )
    private void redirectDefaultWindowHints() {
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(143361, 0);
    }
}
