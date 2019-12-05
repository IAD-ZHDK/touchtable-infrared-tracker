class Planet {
  String name;

  PShape mesh;
  PShader shader;

  PImage colorMap;
  PImage nightMap;
  PImage bumpMap;
  
  float dayNightMix = 0.0;

  int detail = 300;
  int size = 200;
  float bumpIntensity = 20.0f;

  public Planet(String name) {
    this.name = name;

    sphereDetail(detail);
    mesh = createShape(SPHERE, size);
    
    mesh.setFill(color(255));
    mesh.setStroke(color(0, 0));

    shader = loadShader("fragment.glsl", "vertex.glsl");

    colorMap = loadImage(name + "color8k.jpg");
    nightMap = loadImage(name + "night8k.jpg");
    bumpMap = loadImage(name + "bump8k.jpg");
  }

  private void prepareShader() {
    shader.set("colorMap", colorMap);
    shader.set("nightMap", nightMap);
    shader.set("dayNightMix", dayNightMix);

    shader.set("bumpMap", bumpMap);
    shader.set("bumpIntensity", bumpIntensity);
  }

  void render(PGraphics g) {
    prepareShader();
    g.shader(shader);
    g.shape(mesh);
    g.resetShader();
  }
}
