#version 330

layout (location = 0) in vec3 pointObjCoord;

void main() {
    gl_Position = vec4(pointObjCoord,1.0);
}