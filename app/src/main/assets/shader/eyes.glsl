#version 300 es
precision mediump float;

in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D uTexture;

// 👇 参数
uniform vec2 uCenter;   // 眼睛中心 (0~1)
uniform float uRadius;  // 半径
uniform float uScale;   // 放大强度 (0~1 推荐 0.3~0.8)

void main() {
    vec2 uv = vTexCoord;

    float dist = distance(uv, uCenter);

    if (dist < uRadius) {
        float ratio = (uRadius - dist) / uRadius;

        // 👇 放大函数（平滑）
        float factor = 1.0 - uScale * ratio * ratio;

        uv = uCenter + (uv - uCenter) * factor;
    }

    fragColor = texture(uTexture, uv);
}