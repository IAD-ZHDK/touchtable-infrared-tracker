TrackerClient tracker;

float toSize = 150;
boolean hideCursor = true;

void setup() {
  //size(1080, 720, FX2D);
  fullScreen(FX2D);

  tracker = new TrackerClient(8002);

  if (hideCursor)
    noCursor();
}

void draw() {
  background(0);

  for (TactileObject to : tracker.getTactileObjects()) {
    to.update();
    drawTactileObject(to);
  }
}

void drawTactileObject(TactileObject to) {
  // fix mirror (maybe flip with width - term)
  float x = (to.position.x * width);
  float y = (to.position.y * height);

  println(to.smoothRotation);

  float r = radians(to.smoothRotation);

  // check out of bounds
  if (x < 0 || x >= width || y < 0 || y >= height) {
    drawOutOfBounds(x, y, to.uniqueId);
  }

  // render object
  float hto = toSize * 0.6;

  ellipseMode(CENTER);
  noFill();
  stroke(255);
  strokeWeight(5);
  circle(x, y, toSize);

  // draw rotation
  stroke(255, 0, 0);
  arc(x, y, toSize, toSize, 0, r);

  // text
  fill(255);
  textAlign(CENTER, CENTER);
  text(to.uniqueId, x + hto, y + hto);
}

void drawOutOfBounds(float x, float y, int uniqueId) { 
  float rx = x;
  float ry = y;

  if (x < 0) {
    rx = 0;
  }

  if (x > width) {
    rx = width - 1;
  }

  if (y < 0) {
    ry = 0;
  }

  if (y > height) {
    ry = height - 1;
  }

  rectMode(CENTER);
  noFill();
  stroke(255);
  strokeWeight(2);
  rect(rx, ry, 20, 20);
}

// basic easing method
float ease(float target, float value, float alpha) {
  float d = target - value;
  return value + (d * alpha);
}

void mouseClicked() {
  // show and hide cursor if necessary
  if (hideCursor)
    cursor();
  else
    noCursor();

  hideCursor = !hideCursor;
}
