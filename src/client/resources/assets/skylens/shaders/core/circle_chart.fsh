#version 150

uniform vec4 startColor;
uniform vec4 endColor;
uniform vec2 center;
uniform float radius;
uniform float progress;
uniform float time;
uniform float startAngle;
uniform int reverse;

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
    if (reverse == 1)
        factor = 1.0 - factor;

    vec4 color = mix(startColor, endColor, factor);
    fragColor = vec4(color.rgb, color.a * finalAlpha);
}