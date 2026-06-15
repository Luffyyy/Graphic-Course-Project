#version 330

layout (location = 0) in vec3 pointObjCoord;
layout (location = 1) in vec3 normalObjCoord;

out vec3 vs_NormalEyeCoord;
out vec3 vs_pointEyeCoord;

uniform mat4 modelM;
uniform mat4 lookatM;
uniform mat4 projectionM;
uniform mat3 normalFromObjectCoordToEyeCoordM;

void main(void) {
    vs_pointEyeCoord = (lookatM * modelM * vec4(pointObjCoord,1.0)).xyz; 
    vs_NormalEyeCoord = normalFromObjectCoordToEyeCoordM * normalObjCoord;

    gl_Position = projectionM * lookatM * modelM * vec4(pointObjCoord,1.0);
}