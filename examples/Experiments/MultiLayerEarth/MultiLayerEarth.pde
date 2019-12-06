import ch.bildspur.postfx.builder.*;
import ch.bildspur.postfx.pass.*;
import ch.bildspur.postfx.*;
import peasy.PeasyCam;

PeasyCam cam;
PGraphics canvas;

Planet earth;

PostFX fx;

void setup() {
  size(1280, 800, P3D);
  //fullScreen(P3D);
  //pixelDensity(2);

  // setup camera
  cam = new PeasyCam(this, 450);
  cam.setMinimumDistance(280);
  cam.setMaximumDistance(800);

  // setup canvas
  canvas = createGraphics(width, height, P3D);

  // remove clipping
  canvas.perspective((PI / 3.0f), (float)width / height, 0.001f, 100000f);

  // setup objects
  earth = new Planet("earth");

  // setup fx
  fx = new PostFX(this);
}

void draw() {
  earth.dayNightMix = (float)mouseX / width;
  
  // render canvas
  canvas.beginDraw();
  canvas.background(0);
  canvas.ambient(255);
  earth.render(canvas);
  canvas.endDraw();

  // rotate
  cam.rotateY(0.001);

  // apply view matrix of peasy to canvas
  cam.getState().apply(canvas);

  // draw canvas onto onscreen
  cam.beginHUD();
  image(canvas, 0, 0);

  // post fx
  fx.render()
    //.pixelate(100)
    //.sobel()
    .bloom(0.5, 20, 50)
    .compose();

  // show info
  fill(255);
  textSize(20);
  text("FPS: " + frameRate, 50, 50);
  cam.endHUD();
}
