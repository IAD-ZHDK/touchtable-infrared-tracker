#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D colorMap;
uniform sampler2D nightMap;
uniform float dayNightMix;

varying vec4 vertColor;
varying vec3 ecNormal;
varying vec3 lightDir;
varying vec4 vertTexCoord;

void main() {
  vec3 direction = normalize(lightDir);
  vec3 normal = normalize(ecNormal);
  float intensity = max(0.0, dot(direction, normal));
  vec4 tintColor = vec4(intensity, intensity, intensity, 1) * vertColor;

  vec4 dayColor = texture2D(colorMap, vertTexCoord.st);
  vec4 nightColor = texture2D(nightMap, vertTexCoord.st);
  vec4 color = mix(dayColor, nightColor, dayNightMix);

  gl_FragColor = color * tintColor;
}