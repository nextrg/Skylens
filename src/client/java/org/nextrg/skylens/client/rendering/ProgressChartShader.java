package org.nextrg.skylens.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import earth.terrarium.olympus.client.shader.Shader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.function.Supplier;

public class ProgressChartShader {
    private static final Supplier<String> VERTEX = () -> """
            #version 150
            in vec3 Position;
            uniform mat4 modelViewMat;
            uniform mat4 projMat;
            void main() {
                gl_Position = projMat * modelViewMat * vec4(Position, 1.0);
            }
            """;
    
    private static final Supplier<String> FRAGMENT = () -> """
            #version 150
            uniform vec4 startColor;
            uniform vec4 endColor;
            uniform vec2 center;
            uniform float radius;
            uniform float progress;
            uniform float time;
            uniform float startAngle;
            uniform bool reverse;
            
            out vec4 fragColor;
            
            const float TAU = 6.2831853;
            const float edgeSoftness = 1.5;
            
            void main() {
                vec2 pos = gl_FragCoord.xy - center;
                float angle = atan(pos.y, pos.x);
                angle = mod(angle + TAU, TAU);
            
                float dist = length(pos);
                float edgeAlpha = 1.0 - smoothstep(radius - edgeSoftness, radius, dist);
                if (edgeAlpha <= 0.0) discard;
            
                float angleOffset = mod(angle - startAngle + TAU, TAU);
                float angularLength = progress * TAU;
            
                float angleSoft = edgeSoftness / radius;
                float angleAlpha = 1.0 - smoothstep(angularLength - angleSoft, angularLength, angleOffset);
            
                float finalAlpha = edgeAlpha * angleAlpha;
                if (finalAlpha <= 0.0) discard;
            
                float factor = fract(angleOffset / TAU + time);
                if (reverse)
                    factor = 1.0 - factor;
            
                vec4 color = mix(startColor, endColor, factor);
                fragColor = vec4(color.rgb, color.a * finalAlpha);
            }
            """;
    
    public static Shader<ProgressChartUniform> SHADER;
    
    static {
        SHADER = Shader.make(VERTEX, FRAGMENT, ProgressChartUniform::new);
    }
    
    public ProgressChartShader() {
    }
    
    public static void drawPie(DrawContext graphics, int x, int y, float progress, float radius, int startColor, int endColor, float startAngle, float time, boolean invert, float borderWidth, int borderColor) {
        Window window = MinecraftClient.getInstance().getWindow();
        float scale = (float) window.getScaleFactor();
        float scaledX = x * scale;
        float scaledY = y * scale;
        float scaledRadius = radius * scale;
        
        float yOffset = window.getFramebufferHeight() - scaledY * 2.0F;
        
        ProgressChartUniform uniforms = SHADER.uniforms();
        uniforms.modelViewMat.set(new Matrix4f(RenderSystem.getModelViewMatrix()));
        uniforms.projMat.set(new Matrix4f(RenderSystem.getProjectionMatrix()));
        uniforms.startColor.set(colorToVec4f(startColor));
        uniforms.endColor.set(colorToVec4f(endColor));
        uniforms.center.set(new Vector2f(scaledX, scaledY + yOffset));
        uniforms.radius.set(scaledRadius);
        uniforms.progress.set(progress);
        uniforms.time.set(time);
        uniforms.startAngle.set(startAngle);
        uniforms.borderWidth.set(borderWidth);
        uniforms.borderColor.set(colorToVec4f(borderColor));
        uniforms.invert.set(invert);
        
        SHADER.use(() -> {
            RenderSystem.enableBlend();
            Matrix4f matrix = graphics.getMatrices().peek().getPositionMatrix();
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            
            buffer.vertex(matrix, x - radius, y - radius, 0.0F);
            buffer.vertex(matrix, x - radius, y + radius, 0.0F);
            buffer.vertex(matrix, x + radius, y + radius, 0.0F);
            buffer.vertex(matrix, x + radius, y - radius, 0.0F);
            
            BufferRenderer.draw(buffer.end());
            RenderSystem.disableBlend();
        });
    }
    
    private static Vector4f colorToVec4f(int color) {
        return new Vector4f(
                (color >> 16 & 0xFF) / 255f,
                (color >> 8 & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                (color >> 24 & 0xFF) / 255f
        );
    }
}