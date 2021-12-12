#version 330 core
layout (points) in;
layout (line_strip, max_vertices = 16) out;

in float width[];

uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;

void main() {
    vec4 worldPosition;

    worldPosition = gl_in[0].gl_Position + vec4(0.0, 0.0, 0.0, 0.0);
    gl_Position = projectionMatrix * transformMatrix * worldPosition;
    EmitVertex();

    worldPosition = gl_in[0].gl_Position + vec4(width[0], 0.0, 0.0, 0.0);
    gl_Position = projectionMatrix * transformMatrix * worldPosition;
    EmitVertex();

    worldPosition = gl_in[0].gl_Position + vec4(width[0], width[0], 0.0, 0.0);
    gl_Position = projectionMatrix * transformMatrix * worldPosition;
    EmitVertex();

    worldPosition = gl_in[0].gl_Position + vec4(0.0, width[0], 0.0, 0.0);
    gl_Position = projectionMatrix * transformMatrix * worldPosition;
    EmitVertex();

    worldPosition = gl_in[0].gl_Position + vec4(0.0, 0.0, 0.0, 0.0);
    gl_Position = projectionMatrix * transformMatrix * worldPosition;
    EmitVertex();

    worldPosition = gl_in[0].gl_Position + vec4(0.0, 0.0, width[0], 0.0);
    gl_Position = projectionMatrix * transformMatrix * worldPosition;
    EmitVertex();

    worldPosition = gl_in[0].gl_Position + vec4(width[0], 0.0, width[0], 0.0);
    gl_Position = projectionMatrix * transformMatrix * worldPosition;
    EmitVertex();

    worldPosition = gl_in[0].gl_Position + vec4(width[0], width[0], width[0], 0.0);
    gl_Position = projectionMatrix * transformMatrix * worldPosition;
    EmitVertex();

    worldPosition = gl_in[0].gl_Position + vec4(0.0, width[0], width[0], 0.0);
    gl_Position = projectionMatrix * transformMatrix * worldPosition;
    EmitVertex();

    worldPosition = gl_in[0].gl_Position + vec4(0.0, 0.0, width[0], 0.0);
    gl_Position = projectionMatrix * transformMatrix * worldPosition;
    EmitVertex();

    EndPrimitive();



    worldPosition = gl_in[0].gl_Position + vec4(width[0], 0.0, 0.0, 0.0);
    gl_Position = projectionMatrix * transformMatrix * worldPosition;
    EmitVertex();

    worldPosition = gl_in[0].gl_Position + vec4(width[0], 0.0, width[0], 0.0);
    gl_Position = projectionMatrix * transformMatrix * worldPosition;
    EmitVertex();

    EndPrimitive();



    worldPosition = gl_in[0].gl_Position + vec4(0.0, width[0], 0.0, 0.0);
    gl_Position = projectionMatrix * transformMatrix * worldPosition;
    EmitVertex();

    worldPosition = gl_in[0].gl_Position + vec4(0.0, width[0], width[0], 0.0);
    gl_Position = projectionMatrix * transformMatrix * worldPosition;
    EmitVertex();

    EndPrimitive();



    worldPosition = gl_in[0].gl_Position + vec4(width[0], width[0], 0.0, 0.0);
    gl_Position = projectionMatrix * transformMatrix * worldPosition;
    EmitVertex();

    worldPosition = gl_in[0].gl_Position + vec4(width[0], width[0], width[0], 0.0);
    gl_Position = projectionMatrix * transformMatrix * worldPosition;
    EmitVertex();

    EndPrimitive();

}