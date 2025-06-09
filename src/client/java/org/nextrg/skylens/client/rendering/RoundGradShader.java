package org.nextrg.skylens.client.rendering;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.LogicOp;
import com.mojang.blaze3d.vertex.VertexFormat;
import earth.terrarium.olympus.client.pipelines.PipelineRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import org.joml.Matrix4f;
import org.nextrg.skylens.client.SkylensClient;

public class RoundGradShader {
    private static final RenderPipeline PROGRESS_CHART = RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation(SkylensClient.id("round_grad"))
                    .withVertexShader(SkylensClient.id("core/round_grad"))
                    .withFragmentShader(SkylensClient.id("core/round_grad"))
                    .withCull(false)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withColorLogic(LogicOp.NONE)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
                    .withSampler("Sampler0")
                    .withUniform("Size", UniformType.INT)
                    .withUniform("Vertical", UniformType.INT)
                    .build()
    );

    public RoundGradShader() {
    }

    public static void fill(
            DrawContext graphics, int x, int y, int width, int height, float radius,
            int startColor, int endColor, int gradientDirection, float animationTime, int borderWidth, int borderColor
    ) {
        Window window = MinecraftClient.getInstance().getWindow();
        float scale = (float) window.getScaleFactor();
        float scaledX = (float) x * scale;
        float scaledY = (float) y * scale;
        float scaledWidth = (float) width * scale;
        float scaledHeight = (float) height * scale;
        float yOffset = (float) window.getFramebufferHeight() - scaledHeight - scaledY * 2.0F;

        Matrix4f matrix = graphics.getMatrices().peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

        buffer.vertex(matrix, x, y, 0.0F);
        buffer.vertex(matrix, x, (y + height), 0.0F);
        buffer.vertex(matrix, (x + width), (y + height), 0.0F);
        buffer.vertex(matrix, (x + width), y, 0.0F);
        PipelineRenderer.draw(
                PROGRESS_CHART, buffer.end(), pass -> {
                    pass.setUniform("startColor", colorToVec4f(startColor));
                    pass.setUniform("endColor", colorToVec4f(endColor));
                    pass.setUniform("center", scaledX, scaledY + yOffset);
                    pass.setUniform("gradientDirection", gradientDirection);
                    pass.setUniform("borderColor", (float) (borderColor >> 16 & 255) / 255.0F, (float) (borderColor >> 8 & 255) / 255.0F, (float) (borderColor & 255) / 255.0F, (float) (borderColor >> 24 & 255) / 255.0F);
                    pass.setUniform("borderRadius", radius, radius, radius, radius);
                    pass.setUniform("borderWidth", borderWidth);
                    pass.setUniform("size", scaledWidth - (float) borderWidth * 2.0F * scale, scaledHeight - (float) borderWidth * 2.0F * scale);
                    pass.setUniform("center", scaledX + scaledWidth / 2.0F, scaledY + scaledHeight / 2.0F + yOffset);
                    pass.setUniform("borderColor", colorToVec4f(borderColor));
                    pass.setUniform("time", animationTime);
                });
    }

    private static float[] colorToVec4f(int color) {
        return new float[]{
                (color >> 16 & 0xFF) / 255f,
                (color >> 8 & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                (color >> 24 & 0xFF) / 255f
        };
    }
}
