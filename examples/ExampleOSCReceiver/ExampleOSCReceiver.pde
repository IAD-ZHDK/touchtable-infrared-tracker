import oscP5.*;

OscP5 oscP5;

void setup() {
  size(400, 400);
  frameRate(25);

  oscP5 = new OscP5(this, 8000);
}

void draw() {
  background(0);
}

void oscEvent(OscMessage msg) {
  if (msg.addrPattern().equals("/ir/object")) {   
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
