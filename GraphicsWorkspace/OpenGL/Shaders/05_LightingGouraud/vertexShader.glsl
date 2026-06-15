#version 330

layout (location = 0) in vec3 pointObjCoord;
layout (location = 1) in vec3 normalObjCoord;

uniform mat4 modelM;
uniform mat4 lookatM;
uniform mat4 projectionM;
uniform mat3 normalFromObjectCoordToEyeCoordM;

uniform vec3 lightPositionEyeCoord;
uniform vec4 materialAmbient;
uniform vec4 materialDiffuse;
uniform vec4 materialSpecular;
uniform float materialShininess;

out vec4 vs_color;

void main(void) {
    vec4 pointEyeCoord = (lookatM * modelM * vec4(pointObjCoord,1.0)); 
    vec3 NormalEyeCoord = normalFromObjectCoordToEyeCoordM * normalObjCoord;

    gl_Position = projectionM * lookatM * modelM * vec4(pointObjCoord,1.0);
       
	//diffuse
    vec3 L = normalize(lightPositionEyeCoord - pointEyeCoord.xyz);
    vec3 N = normalize(NormalEyeCoord);
    float LdotN = dot(L,N);
    vec3 diffuse = materialDiffuse.xyz * max(LdotN,0.0);

	//ambient
    vec3 ambient = materialAmbient.xyz;

	//specular
    vec3 V = normalize(-pointEyeCoord.xyz);  // View vector in eye space
	vec3 R = reflect(-L, N);                      // Reflected light vector
	float cosAlpha = dot(R, V);                   // cos(angle between R and V)
	vec3 specular = materialSpecular.xyz * pow(max(cosAlpha, 0.0), materialShininess);

	//combining all
    vs_color = vec4(ambient + diffuse + specular , 1.0);
}