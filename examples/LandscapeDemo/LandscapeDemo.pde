TrackerClient tracker;

boolean hideCursor = false;

void setup() {
  size(800, 600, P3D);
  pixelDensity(2);

  tracker = new TrackerClient(8002);

  if (hideCursor)
    noCursor();
}

void draw() {
  background(0);
  renderMap();
}

void renderMap() {
}

void renderObjects() {
  for (TactileObject to : tracker.getTactileObjects()) {
    to.update();
    drawTactileObject(to);
  }
}

void drawTactileObject(TactileObject to) {
  float x = (to.position.x * width);
  float y = (to.position.y * height);
}

void mouseClicked() {
  // show and hide cursor if necessary
  if (hideCursor)
    cursor();
  else
    noCursor();

  hideCursor = !hideCursor;
}

void stop() {
  cursor();
}
