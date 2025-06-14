package org.nextrg.skylens.client.rendering;

import earth.terrarium.olympus.client.pipelines.RoundedRectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;

import static com.mojang.blaze3d.systems.RenderSystem.setShaderColor;

public class Renderer {
    public static void beginRendering() {
        setShaderColor(1, 1, 1, 1);
    }
    
    public static void legacyRoundRectangle(DrawContext context, float x, float y, float w, float h, float r, int color) {
        r = Math.clamp(r, 1, Math.min(w, h) / 2);
        float[][] corners = {
                {x + w - r, y + r},
                {x + w - r, y + h - r},
                {x + r, y + h - r},
                {x + r, y + r}
        };
        float finalR = r;
        context.draw((source) -> {
            Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
            VertexConsumer buffer = source.getBuffer(RenderLayer.getDebugTriangleFan());
            buffer.vertex(matrix4f, x + w / 2F, y + h / 2F, 0).color(color);
            for (int corner = 0; corner < 4; corner++) {
                int cornerStart = (corner - 1) * 90;
                int cornerEnd = cornerStart + 90;
                for (int i = cornerStart; i <= cornerEnd; i += 10) {
                    float angle = (float) Math.toRadians(i);
                    float rx = corners[corner][0] + (float) (Math.cos(angle) * finalR);
                    float ry = corners[corner][1] + (float) (Math.sin(angle) * finalR);
                    buffer.vertex(matrix4f, rx, ry, 0).color(color);
                }
            }
            buffer.vertex(matrix4f, corners[0][0], y, 0).color(color);
        });
    }
    
    public static void roundRectangle(
            DrawContext graphics,
            int x,
            int y,
            int width,
            int height,
            float borderRadius,
            int backgroundColor,
            int borderWidth,
            int borderColor
    ) {
        RoundedRectangle.draw(graphics, x, y, width, height, backgroundColor, borderColor, borderRadius, borderWidth);
    }
    
    public static void drawItem(DrawContext context, ItemStack item, float x, float y, float scale) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        float offset = 8f * (scale - 1f);
        context.getMatrices().translate(-offset, -offset, 0);
        context.getMatrices().scale(scale, scale, 1f);
        context.drawItem(item, 0, 0);
        context.getMatrices().pop();
    }
    
    public static void drawText(
            DrawContext context,
            String text,
            float x,
            float y,
            int color,
            float scale,
            boolean centered,
            boolean shadow
    ) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, scale);
        if (!centered) {
            context.drawText(MinecraftClient.getInstance().textRenderer, text, 0, 0, color, shadow);
        } else {
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, text, 0, 0, color);
        }
        context.getMatrices().pop();
    }
    
    public static float easeInOutQuadratic(float t) {
        if (t <= 0.5f) {
            return 2.0f * t * t;
        }
        t -= 0.5f;
        return 2.0f * t * (1.0f - t) + 0.5f;
    }
    
    public static float easeInOutCubic(float t) {
        if (t < 0.5f) {
            return 4.0f * t * t * t;
        } else {
            float f = -2.0f * t + 2.0f;
            return 1.0f - (f * f * f) / 2.0f;
        }
    }
    
    public static int getScreenWidth(DrawContext context) {
        return context.getScaledWindowWidth();
    }
    
    public static int getScreenHeight(DrawContext context) {
        return context.getScaledWindowHeight();
    }
    
    public static float[] colorToVec4f(int color) {
        return new float[]{
                (color >> 16 & 0xFF) / 255f,
                (color >> 8 & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                (color >> 24 & 0xFF) / 255f
        };
    }
}
