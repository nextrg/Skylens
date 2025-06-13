package org.nextrg.skylens.client.rendering;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.LogicOp;
import com.mojang.blaze3d.systems.RenderSystem;
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

import static org.nextrg.skylens.client.rendering.Renderer.colorToVec4f;

public class RoundGradShader {
    private static final RenderPipeline ROUND_GRAD = RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation(SkylensClient.id("round_grad"))
                    .withVertexShader(SkylensClient.id("core/basic_transform"))
                    .withFragmentShader(SkylensClient.id("core/round_grad"))
                    .withCull(false)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withColorLogic(LogicOp.NONE)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS)
                    .withUniform("modelViewMat", UniformType.MATRIX4X4)
                    .withUniform("projMat", UniformType.MATRIX4X4)
                    .withUniform("startColor", UniformType.VEC4)
                    .withUniform("endColor", UniformType.VEC4)
                    .withUniform("borderColor", UniformType.VEC4)
                    .withUniform("borderRadius", UniformType.VEC4)
                    .withUniform("size", UniformType.VEC2)
                    .withUniform("center", UniformType.VEC2)
                    .withUniform("borderWidth", UniformType.FLOAT)
                    .withUniform("scaleFactor", UniformType.FLOAT)
                    .withUniform("time", UniformType.FLOAT)
                    .withUniform("gradientDirection", UniformType.INT)
                    .build()
    );
    
    public RoundGradShader() {
    }
    
    public static void draw(
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
        
        buffer.vertex(matrix, x, y, 1.0F);
        buffer.vertex(matrix, x, (y + height), 1.0F);
        buffer.vertex(matrix, (x + width), (y + height), 1.0F);
        buffer.vertex(matrix, (x + width), y, 1.0F);
        
        PipelineRenderer.draw(
                ROUND_GRAD, buffer.end(), pass -> {
                    pass.setUniform("modelViewMat", RenderSystem.getModelViewMatrix());
                    pass.setUniform("projMat", RenderSystem.getProjectionMatrix());
                    pass.setUniform("startColor", colorToVec4f(startColor));
                    pass.setUniform("endColor", colorToVec4f(endColor));
                    pass.setUniform("gradientDirection", gradientDirection);
                    pass.setUniform("borderRadius", radius, radius, radius, radius);
                    pass.setUniform("borderWidth", (float) borderWidth);
                    pass.setUniform("scaleFactor", scale);
                    pass.setUniform("size", scaledWidth - (float) borderWidth * 2.0F * scale, scaledHeight - (float) borderWidth * 2.0F * scale);
                    pass.setUniform("center", scaledX + scaledWidth / 2.0F, scaledY + scaledHeight / 2.0F + yOffset);
                    pass.setUniform("borderColor", colorToVec4f(borderColor));
                    pass.setUniform("time", animationTime);
                });
    }
}
