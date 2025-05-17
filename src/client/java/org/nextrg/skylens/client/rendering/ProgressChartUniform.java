package org.nextrg.skylens.client.rendering;

import earth.terrarium.olympus.client.shader.Shader;
import earth.terrarium.olympus.client.shader.Uniform;
import earth.terrarium.olympus.client.shader.UniformTypes;
import earth.terrarium.olympus.client.shader.Uniforms;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class ProgressChartUniform extends Uniforms {
    public final Uniform<Matrix4f> modelViewMat;
    public final Uniform<Matrix4f> projMat;
    public final Uniform<Vector4f> startColor;
    public final Uniform<Vector4f> endColor;
    public final Uniform<Vector2f> center;
    public final Uniform<Float> radius;
    public final Uniform<Float> progress;
    public final Uniform<Float> time;
    public final Uniform<Float> startAngle;
    public final Uniform<Float> borderWidth;
    public final Uniform<Vector4f> borderColor;
    public final Uniform<Boolean> invert;
    
    public ProgressChartUniform(Shader<ProgressChartUniform> shader) {
        this.modelViewMat = this.create(shader, UniformTypes.MAT4, "modelViewMat");
        this.projMat = this.create(shader, UniformTypes.MAT4, "projMat");
        this.startColor = this.create(shader, UniformTypes.VEC4, "startColor");
        this.endColor = this.create(shader, UniformTypes.VEC4, "endColor");
        this.center = this.create(shader, UniformTypes.VEC2, "center");
        this.time = this.create(shader, UniformTypes.FLOAT, "time");
        this.radius = this.create(shader, UniformTypes.FLOAT, "radius");
        this.progress = this.create(shader, UniformTypes.FLOAT, "progress");
        this.startAngle = this.create(shader, UniformTypes.FLOAT, "startAngle");
        this.borderWidth = this.create(shader, UniformTypes.FLOAT, "borderWidth");
        this.borderColor = this.create(shader, UniformTypes.VEC4, "borderColor");
        this.invert = this.create(shader, UniformTypes.BOOL, "invert");
    }
}