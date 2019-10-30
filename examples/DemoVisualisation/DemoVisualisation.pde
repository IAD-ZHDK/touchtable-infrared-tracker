TrackerClient tracker;

void setup() {
  size(640, 480, FX2D);
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
  float x = to.position.x * width;
  float y = to.position.y * height;


  ellipseMode(CENTER);
  noFill();
  stroke(255);
  circle(x, y, 30);

  fill(255);
  textAlign(CENTER, CENTER);
  text(to.uniqueId, x, y);
}

// basic easing method
float ease(float target, float value, float alpha) {
  float d = target - value;
  return value + (d * alpha);
}
