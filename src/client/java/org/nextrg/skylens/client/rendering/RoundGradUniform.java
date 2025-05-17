package org.nextrg.skylens.client.rendering;

import earth.terrarium.olympus.client.shader.Shader;
import earth.terrarium.olympus.client.shader.Uniform;
import earth.terrarium.olympus.client.shader.UniformTypes;
import earth.terrarium.olympus.client.shader.Uniforms;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class RoundGradUniform extends Uniforms {
    public final Uniform<Matrix4f> modelViewMat;
    public final Uniform<Matrix4f> projMat;
    public final Uniform<Vector4f> startColor;
    public final Uniform<Vector4f> endColor;
    public final Uniform<Integer> gradientDirection;
    public final Uniform<Vector4f> borderColor;
    public final Uniform<Vector4f> borderRadius;
    public final Uniform<Float> borderWidth;
    public final Uniform<Vector2f> size;
    public final Uniform<Vector2f> center;
    public final Uniform<Float> scaleFactor;
    public final Uniform<Float> time;
    
    public RoundGradUniform(Shader<RoundGradUniform> shader) {
        this.modelViewMat = this.create(shader, UniformTypes.MAT4, "modelViewMat");
        this.projMat = this.create(shader, UniformTypes.MAT4, "projMat");
        this.startColor = this.create(shader, UniformTypes.VEC4, "startColor");
        this.endColor = this.create(shader, UniformTypes.VEC4, "endColor");
        this.gradientDirection = this.create(shader, UniformTypes.INT, "gradientDirection");
        this.borderColor = this.create(shader, UniformTypes.VEC4, "borderColor");
        this.borderRadius = this.create(shader, UniformTypes.VEC4, "borderRadius");
        this.borderWidth = this.create(shader, UniformTypes.FLOAT, "borderWidth");
        this.size = this.create(shader, UniformTypes.VEC2, "size");
        this.center = this.create(shader, UniformTypes.VEC2, "center");
        this.scaleFactor = this.create(shader, UniformTypes.FLOAT, "scaleFactor");
        this.time = this.create(shader, UniformTypes.FLOAT, "time");
    }
}