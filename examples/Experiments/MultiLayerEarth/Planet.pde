class Planet {
  String name;

  PShape mesh;
  PShader shader;

  PImage colorMap;
  PImage bumpMap;

  int detail = 120;
  int size = 200;
  float bumpIntensity = 100.0f;

  public Planet(String name) {
    this.name = name;

    sphereDetail(detail);
    mesh = createShape(SPHERE, size);
    
    mesh.setFill(color(255));
    mesh.setStroke(color(0, 255, 0));

    shader = loadShader("fragment.glsl", "vertex.glsl");

    colorMap = loadImage(name + "color.jpg");
    bumpMap = loadImage(name + "bump.jpg");
  }

  private void prepareShader() {
    shader.set("colorMap", colorMap);

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
