#version 150

in vec3 Position;
uniform mat4 modelViewMat;
uniform mat4 projMat;

void main() {
    gl_Position = projMat * modelViewMat * vec4(Position, 1.0);
}