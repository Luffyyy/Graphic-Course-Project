#version 330

in vec3 vs_NormalEyeCoord;
in vec3 vs_pointEyeCoord;

out vec4 fragColor;

uniform vec3 lightPositionEyeCoord;
uniform vec4 materialAmbient;
uniform vec4 materialDiffuse;
uniform vec4 materialSpecular;
uniform float materialShininess;

void main(void)
{
	//diffuse
    vec3 L = normalize(lightPositionEyeCoord - vs_pointEyeCoord);
    vec3 N = normalize(vs_NormalEyeCoord);
    float LdotN = dot(L,N);
    vec3 diffuse = materialDiffuse.xyz * max(LdotN,0.0);

	//ambient
    vec3 ambient = materialAmbient.xyz;

	//specular
    vec3 V = normalize(-vs_pointEyeCoord);  // View vector in eye space
	vec3 R = reflect(-L, N);                      // Reflected light vector
	float cosAlpha = dot(R, V);                   // cos(angle between R and V)
	vec3 specular = materialSpecular.xyz * pow(max(cosAlpha, 0.0), materialShininess);

	//combining all
    fragColor = vec4(ambient + diffuse + specular , 1.0);
}