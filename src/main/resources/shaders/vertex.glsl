#version 330 core

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 inNormal;

out vec2 outTexCoord;
out vec3 outNormal;
out float visibility;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

const float density = 0.015;
const float gradient = 1.5;

void main() {
    vec4 positionRelativeToCam = viewMatrix * modelMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * positionRelativeToCam;
    outTexCoord = texCoord;
    outNormal = inNormal;
    
    float distance = length(positionRelativeToCam.xyz);
    visibility = exp(-pow((distance * density), gradient));
    visibility = clamp(visibility, 0.0, 1.0);
}
