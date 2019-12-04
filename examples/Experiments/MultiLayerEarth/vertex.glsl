uniform mat4 transform;
uniform mat4 texMatrix;

attribute vec4 position;
attribute vec4 color;
attribute vec2 texCoord;

varying vec4 vertColor;
varying vec4 vertTexCoord;

uniform sampler2D bumpMap;
uniform float bumpIntensity;

void main() {
  //gl_Position = transform * position;

  vertColor = color;
  vertTexCoord = texMatrix * vec4(texCoord, 1.0, 1.0);

  vec4 pos = transform * position;
  vec4 bump = texture2D(bumpMap, vertTexCoord.st);
  pos.x -= bump.r * bumpIntensity;
  pos.y -= bump.r * bumpIntensity;
  gl_Position = pos;
}