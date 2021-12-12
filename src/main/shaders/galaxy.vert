#version 330 core

layout (location=0) in vec3 iPos;
layout (location=1) in vec3 iRot;
layout (location=2) in float iRad;

out vec3 rot;
out float rad;

void main(){
    gl_Position = vec4(iPos, 1.0);

    rot = iRot;
    rad = iRad;
}
