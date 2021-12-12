#version 330 core
layout (location=0) in vec3 aPos;
layout (location=1) in vec4 aColor;
layout (location=2) in float aSize;

out vec4 fColor;

uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;

void main(){
    fColor = aColor;
    vec4 pos = projectionMatrix*transformMatrix * vec4(aPos, 1.0);
    gl_PointSize = sqrt(aSize/pos.z)*50.0;
    gl_Position = pos;
}