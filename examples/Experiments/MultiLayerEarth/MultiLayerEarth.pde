import peasy.PeasyCam;

PeasyCam cam;
PGraphics canvas;

Planet earth;

void setup() {
  size(1280, 800, P3D);
  pixelDensity(2);

  // setup camera
  cam = new PeasyCam(this, 450);
  cam.setMinimumDistance(400);
  cam.setMaximumDistance(800);

  // setup canvas
  canvas = createGraphics(width, height, P3D);

  // remove clipping
  //perspective((PI / 3.0f), (float)width / height, 0.1f, 100000f);

  // setup objects
  earth = new Planet("earth");
}

void draw() {
  // render canvas
  canvas.beginDraw();
  canvas.background(0);
  earth.render(canvas);
  canvas.endDraw();
  
  // rotate
  cam.rotateY(0.001);

  // apply view matrix of peasy to canvas
  cam.getState().apply(canvas);

  // draw canvas onto onscreen
  cam.beginHUD();
  image(canvas, 0, 0);

  // show info
  fill(255);
  textSize(20);
  text("FPS: " + frameRate, 50, 50);
  cam.endHUD();
}
