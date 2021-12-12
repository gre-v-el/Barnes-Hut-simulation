#version 330 core

in vec4 fColor;
in vec2 gl_PointCoord;

out vec4 color;

void main(){
    vec4 outColor = vec4(fColor.xyz*vec3(gl_PointCoord.y+0.5), 1.0);

    float distSq = (gl_PointCoord.x - 0.5) * (gl_PointCoord.x - 0.5) + (gl_PointCoord.y - 0.5) * (gl_PointCoord.y - 0.5);

    if(distSq > 0.25){
        discard;
    }

    //outColor.a *= cos(distSq*13);
    color = outColor;
}