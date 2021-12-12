#version 330 core
layout (points) in;
layout (line_strip, max_vertices = 34) out;

in vec3 rot[];
in float rad[];

uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;


mat3 rotation3dX(float angle) {
    float s = sin(angle);
    float c = cos(angle);

    return mat3(
    1.0, 0.0, 0.0,
    0.0, c, s,
    0.0, -s, c
    );
}
vec3 rotateX(vec3 v, float angle) {
    return rotation3dX(angle) * v;
}


mat3 rotation3dY(float angle) {
    float s = sin(angle);
    float c = cos(angle);

    return mat3(
    c, 0.0, -s,
    0.0, 1.0, 0.0,
    s, 0.0, c
    );
}
vec3 rotateY(vec3 v, float angle) {
    return rotation3dY(angle) * v;
}


mat3 rotation3dZ(float angle) {
    float s = sin(angle);
    float c = cos(angle);

    return mat3(
    c, s, 0.0,
    -s, c, 0.0,
    0.0, 0.0, 1.0
    );
}
vec3 rotateZ(vec3 v, float angle) {
    return rotation3dZ(angle) * v;
}

void main() {
    vec4 worldPosition;

    for(float i = 0; i < 21; i ++){
        float angle = i / 20 * 3.1415*2;

        worldPosition = vec4(rad[0] * sin(angle), 0.0, rad[0] * cos(angle), 0.0);
        worldPosition = vec4(rotateX(worldPosition.xyz, rot[0].x), 1.0);
        worldPosition = vec4(rotateY(worldPosition.xyz, rot[0].y), 1.0);
        worldPosition = vec4(rotateZ(worldPosition.xyz, rot[0].z), 1.0);
        worldPosition += gl_in[0].gl_Position;

        gl_Position = projectionMatrix * transformMatrix * worldPosition;
        EmitVertex();
    }
    EndPrimitive();

    for(float i = 0; i < 13; i ++){
        float angle = i / 12 * 3.1415*2;

        worldPosition = vec4(rad[0] * 0.1 * sin(angle), 0.0, rad[0] * 0.1 * cos(angle), 0.0);
        worldPosition = vec4(rotateX(worldPosition.xyz, rot[0].x), 1.0);
        worldPosition = vec4(rotateY(worldPosition.xyz, rot[0].y), 1.0);
        worldPosition = vec4(rotateZ(worldPosition.xyz, rot[0].z), 1.0);
        worldPosition += gl_in[0].gl_Position;

        gl_Position = projectionMatrix * transformMatrix * worldPosition;
        EmitVertex();
    }
    EndPrimitive();
}