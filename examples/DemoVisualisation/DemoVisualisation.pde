TrackerClient tracker;

float toSize = 150;
float oSize = 20;

boolean hideCursor = false;

boolean useSmoothRotation = true;
boolean useSmoothPosition = true;

void setup() {
  size(1080, 720, FX2D);
  //fullScreen(FX2D);

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
  float tox = useSmoothPosition ? to.position.x : to.x;
  float toy = useSmoothPosition ? to.position.y : to.y;

  float x = (tox * width);
  float y = (toy * height);
  
  float r = radians(useSmoothRotation ? to.smoothRotation : to.rotation);

  // check out of bounds
  if (x < 0 || x >= width || y < 0 || y >= height) {
    drawOutOfBounds(x, y, to.uniqueId);
  }

  // render object
  float hto = toSize * 0.6;

  ellipseMode(CENTER);
  noFill();
  stroke(255);
  strokeWeight(8);
  circle(x, y, toSize);

  // draw rotation
  stroke(255, 0, 0);
  arc(x, y, toSize, toSize, 0, r);

  // text
  fill(255);
  textAlign(CENTER, CENTER);
  text(to.uniqueId + " - R: " + round(to.smoothRotation), x + hto, y + hto);
}

void drawOutOfBounds(float x, float y, int uniqueId) { 
  push();
  float rx = constrain(x, 0, width - 1);
  float ry = constrain(y, 0, height - 1);

  translate(rx, ry);

  if (x < 0)
    rotate(radians(90));

  if (x > width)
    rotate(radians(-90));

  if (y < 0)
    rotate(radians(-180));

  float hs = oSize * 0.5;

  rectMode(CENTER);
  noFill();
  stroke(255);
  strokeWeight(3);
  triangle(-hs, -hs, hs, -hs, 0, 0);

  // text
  fill(255);
  textAlign(CENTER, CENTER);
  text(uniqueId, 0, -oSize * 1.2);
  pop();
}

void mouseClicked() {
  // show and hide cursor if necessary
  if (hideCursor)
    cursor();
  else
    noCursor();

  hideCursor = !hideCursor;
}

void keyPressed() {
  if (key == 'R') {
    useSmoothRotation = !useSmoothRotation;
    println("Smooth Rotation: " + useSmoothRotation);
  }

  if (key == 'P') {
    useSmoothPosition = !useSmoothPosition;
    println("Smooth Position: " + useSmoothPosition);
  }
}

void stop() {
  cursor();
}
