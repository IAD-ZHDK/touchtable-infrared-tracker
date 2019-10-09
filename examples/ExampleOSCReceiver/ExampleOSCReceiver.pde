import oscP5.*;

OscP5 oscP5;

PVector singleObjPosition = new PVector();

void setup() {
  size(400, 400);
  frameRate(25);

  oscP5 = new OscP5(this, 8002);
}

void draw() {
  background(0);
  
  noStroke();
  fill(250, 200, 235);
  circle(singleObjPosition.x, singleObjPosition.y, 30);
}

void oscEvent(OscMessage msg) {
  if (msg.addrPattern().equals("/newID")) {   
    double x = msg.get(2).floatValue();
    double y = msg.get(3).floatValue();
    
    singleObjPosition.x = (float)x * width;
    singleObjPosition.y = (float)y * height;
    
    print("Tactile Object [");
    print(msg.get(0).intValue()); // unique
    print(" | ");
    print(msg.get(1).intValue()); // identifier
    print("]: x: ");
    print(msg.get(2).floatValue()); // normalized x
    print(" y: ");
    print(msg.get(3).floatValue()); // normalized y
    print(" lifetime: ");
   // print(msg.get(4).intValue());
    println();
  }
}
