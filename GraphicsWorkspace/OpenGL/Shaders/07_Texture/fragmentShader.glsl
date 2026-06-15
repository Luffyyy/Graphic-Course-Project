#version 330

in vec2 vs_TextureCoord;
out vec4 fragColor;

uniform sampler2D textureSampler;

void main(void)
{
    vec4 textureColor = texture(textureSampler, vs_TextureCoord);
    fragColor = textureColor;
}