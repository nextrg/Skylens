#version 150

uniform mat4 modelViewMat;
uniform mat4 projMat;
uniform vec4 startColor;
uniform vec4 endColor;
uniform vec4 borderColor;
uniform vec4 borderRadius;
uniform vec2 size;
uniform vec2 center;
uniform float borderWidth;
uniform float scaleFactor;
uniform float time;
uniform int gradientDirection;

out vec4 fragColor;

float sdRoundedBox(vec2 p, vec2 b, vec4 r) {
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x = (p.y > 0.0) ? r.x : r.y;
    vec2 q = abs(p) - b + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
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