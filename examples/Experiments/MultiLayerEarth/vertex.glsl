uniform mat4 modelview;
uniform mat4 transform;
uniform mat3 normalMatrix;
uniform mat4 texMatrix;

uniform vec4 lightPosition;

attribute vec4 position;
attribute vec4 color;
attribute vec3 normal;
attribute vec2 texCoord;

varying vec4 vertColor;
varying vec3 ecNormal;
varying vec3 lightDir;
varying vec4 vertTexCoord;

uniform sampler2D bumpMap;
uniform float bumpIntensity;

void main() {
	vec3 ecPosition = vec3(modelview * position);

	ecNormal = normalize(normalMatrix * normal);
	lightDir = normalize(lightPosition.xyz - ecPosition);
	vertColor = color;

	vertTexCoord = texMatrix * vec4(texCoord, 1.0, 1.0);

	vec4 pos = transform * position;
	vec4 bump = texture2D(bumpMap, vertTexCoord.st);
	pos.x -= bump.r * bumpIntensity;
	pos.y -= bump.r * bumpIntensity;
	gl_Position = pos;
}