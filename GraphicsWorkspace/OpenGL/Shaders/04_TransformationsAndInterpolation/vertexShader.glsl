#version 330

layout (location = 0) in vec3 pointObjCoord;

uniform mat4 modelM;
uniform mat4 lookatM;
uniform mat4 projectionM;

out vec4 vs_color;

void main(void) {
    gl_Position = projectionM * lookatM * modelM * vec4(pointObjCoord,1.0);
	vs_color = vec4(pointObjCoord,1.0);
}