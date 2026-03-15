#version 330 core

in vec2 outTexCoord;
in vec3 outNormal;
in float visibility;

out vec4 fragColor;
uniform sampler2D texture_sampler;
const vec3 skyColor = vec3(0.5, 0.8, 1.0);
const vec3 lightDirection = normalize(vec3(0.5, 1.0, 0.3));

void main() {
    vec4 texColor = texture(texture_sampler, outTexCoord);
    if(texColor.a < 0.1) discard;

    // Direct light
    float diff = max(dot(outNormal, lightDirection), 0.2); // ambient 0.2
    
    vec4 finalColor = vec4(texColor.rgb * diff, texColor.a);
    
    // Apply fog
    fragColor = mix(vec4(skyColor, 1.0), finalColor, visibility);
}
