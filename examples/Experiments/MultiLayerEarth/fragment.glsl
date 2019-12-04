#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D colorMap;

varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {
  gl_FragColor = texture2D(colorMap, vertTexCoord.st) * vertColor;
}