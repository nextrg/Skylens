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

public class RoundGradShader {
    private static Supplier<String> VERTEX = () -> "#version 150\n\nin vec3 Position;\n\nuniform mat4 modelViewMat;\nuniform mat4 projMat;\n\nvoid main() {\n    gl_Position = projMat * modelViewMat * vec4(Position, 1.0);\n}\n";
    private static Supplier<String> FRAGMENT = () -> """
    #version 150
    uniform mat4 modelViewMat;
    uniform mat4 projMat;
    uniform vec4 startColor;
    uniform vec4 endColor;
    uniform vec4 borderColor;
    uniform vec4 borderRadius;
    uniform float borderWidth;
    uniform vec2 size;
    uniform vec2 center;
    uniform float scaleFactor;
    uniform int gradientDirection;
    uniform float time;
    out vec4 fragColor;
    float sdRoundedBox(vec2 p, vec2 b, vec4 r){
        r.xy = (p.x > 0.0) ? r.xy : r.zw;
        r.x  = (p.y > 0.0) ? r.x  : r.y;
        vec2 q = abs(p)-b+r.x;
        return min(max(q.x,q.y),0.0) + length(max(q,0.0)) - r.x;
    }
    void main() {
        vec2 halfSize = size / 2.0;
        vec2 pos = gl_FragCoord.xy - center;
        float distance = sdRoundedBox(pos, halfSize, borderRadius * scaleFactor);
        float gradientFactor;
        
        float gradientOffset = fract(time);
        if (gradientDirection == 0) {
            gradientFactor = (pos.y + halfSize.y) / size.y + gradientOffset;
        } else if (gradientDirection == 1) {
            gradientFactor = (pos.x + halfSize.x) / size.x + gradientOffset;
        } else {
            gradientFactor = ((pos.x + halfSize.x) + (pos.y + halfSize.y)) / (size.x + size.y) + gradientOffset;
        }
        gradientFactor = fract(gradientFactor);
        
        vec4 color;
        vec4 colorA = startColor;
        vec4 colorB = endColor;
        vec4 colorC = startColor;
        
        if (gradientFactor < 0.5) {
            color = mix(colorA, colorB, gradientFactor * 2.0);
        } else {
            color = mix(colorB, colorC, (gradientFactor - 0.5) * 2.0);
        }
        float smoothed = min(1.0 - distance, color.a);
        float border = min(1.0 - smoothstep(borderWidth, borderWidth, abs(distance)), borderColor.a);
        if (border > 0.0) {
            fragColor = borderColor * vec4(1.0, 1.0, 1.0, border);
        } else {
            fragColor = color * vec4(1.0, 1.0, 1.0, smoothed);
        }
    }
    """;
    public static Shader<RoundGradUniform> SHADER;
    
    public RoundGradShader() {}
    
    public static void fill(DrawContext graphics, int x, int y, int width, int height, float radius,
                            int startColor, int endColor, int gradientDirection, float animationTime, int borderWidth, int borderColor) {
        Window window = MinecraftClient.getInstance().getWindow();
        float scale = (float)window.getScaleFactor();
        float scaledX = (float)x * scale;
        float scaledY = (float)y * scale;
        float scaledWidth = (float)width * scale;
        float scaledHeight = (float)height * scale;
        float yOffset = (float)window.getFramebufferHeight() - scaledHeight - scaledY * 2.0F;
        RoundGradUniform uniforms = (RoundGradUniform)SHADER.uniforms();
        uniforms.modelViewMat.set(new Matrix4f(RenderSystem.getModelViewMatrix()));
        uniforms.projMat.set(new Matrix4f(RenderSystem.getProjectionMatrix()));
        uniforms.startColor.set(colorToVec4f(startColor));
        uniforms.endColor.set(colorToVec4f(endColor));
        uniforms.gradientDirection.set(gradientDirection);
        uniforms.borderColor.set(new Vector4f((float)(borderColor >> 16 & 255) / 255.0F, (float)(borderColor >> 8 & 255) / 255.0F, (float)(borderColor & 255) / 255.0F, (float)(borderColor >> 24 & 255) / 255.0F));
        uniforms.borderRadius.set(new Vector4f(radius, radius, radius, radius));
        uniforms.borderWidth.set((float)borderWidth);
        uniforms.size.set(new Vector2f(scaledWidth - (float)borderWidth * 2.0F * scale, scaledHeight - (float)borderWidth * 2.0F * scale));
        uniforms.center.set(new Vector2f(scaledX + scaledWidth / 2.0F, scaledY + scaledHeight / 2.0F + yOffset));
        uniforms.scaleFactor.set(scale);
        uniforms.time.set(animationTime);
        SHADER.use(() -> {
            RenderSystem.enableBlend();
            Matrix4f matrix = graphics.getMatrices().peek().getPositionMatrix();
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            buffer.vertex(matrix, (float)x, (float)y, 0.0F);
            buffer.vertex(matrix, (float)x, (float)(y + height), 0.0F);
            buffer.vertex(matrix, (float)(x + width), (float)(y + height), 0.0F);
            buffer.vertex(matrix, (float)(x + width), (float)y, 0.0F);
            BufferRenderer.draw(buffer.end());
            RenderSystem.disableBlend();
        });
    }
    
    private static Vector4f colorToVec4f(int color) {
        return new Vector4f(
                (color >> 16 & 0xFF) / 255f,
                (color >> 8  & 0xFF) / 255f,
                (color       & 0xFF) / 255f,
                (color >> 24 & 0xFF) / 255f
        );
    }
    
    static {
        SHADER = Shader.make(VERTEX, FRAGMENT, RoundGradUniform::new);
    }
}
