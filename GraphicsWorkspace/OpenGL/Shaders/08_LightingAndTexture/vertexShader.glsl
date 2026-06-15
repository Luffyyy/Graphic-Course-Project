#version 330

layout (location = 0) in vec3 pointObjCoord;
layout (location = 1) in vec3 normalObjCoord;
layout (location = 2) in vec2 textureCoord; 

out vec3 vs_NormalEyeCoord;
out vec3 vs_pointEyeCoord;
out vec2 vs_TextureCoord; 

uniform mat4 modelM, lookatM, projectionM;
uniform mat3 normalFromObjectCoordToEyeCoordM;

void main(void) {
    vs_pointEyeCoord = (lookatM * modelM * vec4(pointObjCoord,1.0)).xyz; 
    vs_NormalEyeCoord = normalFromObjectCoordToEyeCoordM * normalObjCoord;

    vs_TextureCoord = textureCoord; 
    gl_Position = projectionM * lookatM * modelM * vec4(pointObjCoord,1.0);
}