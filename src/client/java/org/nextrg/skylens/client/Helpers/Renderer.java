package org.nextrg.skylens.client.Helpers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;
import static com.mojang.blaze3d.systems.RenderSystem.*;
import static com.mojang.blaze3d.systems.RenderSystem.disableBlend;

public class Renderer {
    public static void beginRendering() {
        disableCull();
        enableBlend();
        defaultBlendFunc();
        setShaderColor(1, 1, 1, 1);
    }
    public static void finishRendering() {
        enableCull();
        disableBlend();
    }
    public static void drawBuffer(BufferBuilder buf) {
        BufferRenderer.drawWithGlobalProgram(buf.end());
    }
    public static void drawCircle(Matrix4f mat, int cx, int cy, float radius, int start, int end, int color, int z) {
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buf.vertex(mat,(float)cx,(float)cy,z).color(color);
        for (int i = start - 90; i <= end - 90; i++) {
            double angle = Math.toRadians(i);
            float x = (float) -(Math.cos(angle) * radius) + cx;
            float y = (float) (Math.sin(angle) * radius) + cy;
            buf.vertex(mat,x,y,z + 1).color(color);
        }
        beginRendering();
        drawBuffer(buf);
        finishRendering();
    }
    public static void fillRoundRect(Matrix4f mat, float x, float y, float w, float h, float r, int color) {
        r = Math.clamp(r, 1, Math.min(w, h) / 2);
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buf.vertex(mat, x + w / 2F, y + h / 2F, 0).color(color);
        float[][] corners = {
                { x + w - r, y + r },
                { x + w - r, y + h - r},
                { x + r, y + h - r },
                { x + r, y + r }
        };
        for (int corner = 0; corner < 4; corner++) {
            int cornerStart = (corner - 1) * 90;
            int cornerEnd = cornerStart + 90;
            for (int i = cornerStart; i <= cornerEnd; i += 10) {
                float angle = (float)Math.toRadians(i);
                float rx = corners[corner][0] + (float)(Math.cos(angle) * r);
                float ry = corners[corner][1] + (float)(Math.sin(angle) * r);
                buf.vertex(mat, rx, ry, 0).color(color);
            }
        }
        buf.vertex(mat, corners[0][0], y, 0).color(color);
        beginRendering();
        drawBuffer(buf);
        finishRendering();
    }
    public static void drawItem(DrawContext context, ItemStack item, int x, int y, float scale) {
        x = (int)(x / scale);
        y = (int)(y / scale);
        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, scale);
        context.drawItem(item, x, y);
        context.getMatrices().pop();
    }
    public static void drawText(DrawContext context, String text, int x, int y, int color, float scale, boolean centered, boolean shadow) {
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
        if(t <= 0.5f)
            return 2.0f * t * t;
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
    public static float easeInOutExponential(float t) {
        if (t == 0 || t == 1) return t;
        if (t < 0.5f)
            return (float) (0.5f * Math.pow(2, 20 * t - 10));
        return (float) (1 - 0.5f * Math.pow(2, -20 * t + 10));
    }
    public static int getScreenWidth(DrawContext context) {
        return context.getScaledWindowWidth();
    }
    public static int getScreenHeight(DrawContext context) {
        return context.getScaledWindowHeight();
    }
}
