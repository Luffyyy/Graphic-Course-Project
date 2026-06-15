#version 330

layout (location = 0) in vec3 pointObjCoord;
layout (location = 1) in vec3 normalObjCoord;
layout (location = 2) in vec2 textureCoord; 

out vec2 vs_TextureCoord; 

uniform mat4 modelM, lookatM, projectionM;

void main(void) {
    vs_TextureCoord = textureCoord; 
    gl_Position = projectionM * lookatM * modelM * vec4(pointObjCoord,1.0);
}