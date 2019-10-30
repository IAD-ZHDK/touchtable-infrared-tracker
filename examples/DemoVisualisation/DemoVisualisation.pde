TrackerClient tracker;

float toSize = 75;

void setup() {
  size(1080, 720, FX2D);
  //fullScreen(FX2D);

  tracker = new TrackerClient(8002);
}

void draw() {
  background(0);

  for (TactileObject to : tracker.getTactileObjects()) {
    to.update();
    drawTactileObject(to);
  }
}

void drawTactileObject(TactileObject to) {
  // fix mirror
  float x = width - (to.position.x * width);
  float y = height - (to.position.y * height);
  
  println(to.smoothRotation);
  
  float r = radians(map(to.smoothRotation, 0f, 180f, 0f, PI));

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
  float rx = constrain(0, width, x);
  float ry = constrain (0, height, y);

  rectMode(CENTER);
  fill(255);
  rect(rx, ry, 20, 20);
}

// basic easing method
float ease(float target, float value, float alpha) {
  float d = target - value;
  return value + (d * alpha);
}
