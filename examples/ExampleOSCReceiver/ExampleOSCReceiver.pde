import oscP5.*;

OscP5 oscP5;

PVector singleObjPosition = new PVector();

void setup() {
  size(400, 400);
  frameRate(25);

  oscP5 = new OscP5(this, 8000);
}

void draw() {
  background(0);
  
  noStroke();
  fill(250, 200, 235);
  circle(singleObjPosition.x, singleObjPosition.y, 30);
}

void oscEvent(OscMessage msg) {
  if (msg.addrPattern().equals("/ir/object")) {   
    double x = msg.get(2).doubleValue();
    double y = msg.get(3).doubleValue();
    
    singleObjPosition.x = (float)x * width;
    singleObjPosition.y = (float)y * height;
    
    print("Tactile Object [");
    print(msg.get(0).intValue());
    print(" | ");
    print(msg.get(1).intValue());
    print("]: x: ");
    print(msg.get(2).doubleValue());
    print(" y: ");
    print(msg.get(3).doubleValue());
    print(" lifetime: ");
    print(msg.get(4).intValue());
    println();
  }
}
