TrackerClient tracker;

void setup() {
  size(640, 480, FX2D);
  tracker = new TrackerClient(8002);
}

void draw() {
  background(0);

  for (TactileObject to : tracker.getTactileObjects()) {
    ellipseMode(CENTER);
    noFill();
    stroke(255);
    circle(to.x * width, to.y * height, 30);

    fill(255);
    textAlign(CENTER, CENTER);
    text(to.identifier, to.x * width, to.y * height);
  }
}
