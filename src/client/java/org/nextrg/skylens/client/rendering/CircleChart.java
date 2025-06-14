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

public class CircleChart {
    private static final RenderPipeline PROGRESS_CHART = RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation(SkylensClient.id("circle_chart"))
                    .withVertexShader(SkylensClient.id("core/basic_transform"))
                    .withFragmentShader(SkylensClient.id("core/circle_chart"))
                    .withCull(false)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withColorLogic(LogicOp.NONE)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withVertexFormat(VertexFormats.POSITION, VertexFormat.DrawMode.QUADS)
                    .withUniform("modelViewMat", UniformType.MATRIX4X4)
                    .withUniform("projMat", UniformType.MATRIX4X4)
                    .withUniform("startColor", UniformType.VEC4)
                    .withUniform("endColor", UniformType.VEC4)
                    .withUniform("center", UniformType.VEC2)
                    .withUniform("radius", UniformType.FLOAT)
                    .withUniform("progress", UniformType.FLOAT)
                    .withUniform("time", UniformType.FLOAT)
                    .withUniform("startAngle", UniformType.FLOAT)
                    .withUniform("reverse", UniformType.INT)
                    .build()
    );
    
    public CircleChart() {
    }
    
    public static void draw(
            DrawContext graphics,
            int x,
            int y,
            float progress,
            float radius,
            int startColor,
            int endColor,
            float startAngle,
            float time,
            boolean invert,
            float borderWidth,
            int borderColor
    ) {
        Window window = MinecraftClient.getInstance().getWindow();
        float scale = (float) window.getScaleFactor();
        float scaledX = x * scale;
        float scaledY = y * scale;
        float scaledRadius = radius * scale;
        
        float yOffset = window.getFramebufferHeight() - scaledY * 2.0F;
        Matrix4f matrix = graphics.getMatrices().peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        
        buffer.vertex(matrix, x - radius, y - radius, 1.0F);
        buffer.vertex(matrix, x - radius, y + radius, 1.0F);
        buffer.vertex(matrix, x + radius, y + radius, 1.0F);
        buffer.vertex(matrix, x + radius, y - radius, 1.0F);
        
        PipelineRenderer.draw(
                PROGRESS_CHART, buffer.end(), pass -> {
                    pass.setUniform("modelViewMat", RenderSystem.getModelViewMatrix());
                    pass.setUniform("projMat", RenderSystem.getProjectionMatrix());
                    pass.setUniform("startColor", colorToVec4f(startColor));
                    pass.setUniform("endColor", colorToVec4f(endColor));
                    pass.setUniform("center", scaledX, scaledY + yOffset);
                    pass.setUniform("radius", scaledRadius);
                    pass.setUniform("progress", progress);
                    pass.setUniform("time", time);
                    pass.setUniform("startAngle", startAngle);
                    pass.setUniform("borderWidth", borderWidth);
                    pass.setUniform("borderColor", colorToVec4f(borderColor));
                    pass.setUniform("reverse", invert ? 1 : 0);
                });
    }
}