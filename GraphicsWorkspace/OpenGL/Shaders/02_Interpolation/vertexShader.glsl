#version 330

layout (location = 0) in vec3 pointObjCoord;

out vec4 vs_color;

void main(void) {
    gl_Position = vec4(pointObjCoord,1.0);
	vs_color = vec4(pointObjCoord,1.0);
}