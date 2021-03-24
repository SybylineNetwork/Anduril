#version 110

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

uniform vec2 InSize;

uniform vec2 BlurDir;
uniform float Anduril_Blur_Radius;

void main() {
  float radius = Anduril_Blur_Radius;
  if (radius > 0.5) {
    vec4 blurred = vec4(0.0);
    float totalStrength = 0.0;
    float totalAlpha = 0.0;
    float totalSamples = 0.0;
    float samples = 0.0;
    float step = radius / 10.0;
    for (float r = -radius; r <= radius; r += step) {
        vec4 sampleValue = texture2D(DiffuseSampler, texCoord + oneTexel * r * BlurDir);
        totalAlpha = totalAlpha + sampleValue.a;
        totalSamples = totalSamples + 1.0;
        float strength = 1.0 - abs(r / radius);
        totalStrength = totalStrength + strength;
        blurred = blurred + sampleValue;
        samples += 1.0;
    }
    gl_FragColor = vec4(blurred.rgb / samples, totalAlpha);
  } else {
    gl_FragColor = texture2D(DiffuseSampler, texCoord + oneTexel);
  }
}
