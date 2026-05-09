#version 300 es
precision mediump float;

in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D uTexture;

void main() {
    vec4 color = texture(uTexture, vTexCoord);

    float gray = (color.r + color.g + color.b) / 3.0;
    fragColor = vec4(gray, gray, gray, 1.0);
}