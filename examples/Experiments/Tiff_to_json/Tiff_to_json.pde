PImage crowther;
ArrayList<PVector> visObjects;
void setup() {
  size(100, 100);
  visObjects = new ArrayList<PVector>();
  background(0);
  crowther = loadImage("input.png"); 
  crowther.resize(width, height);
  for (int y = 0; y < height; y++) {
    for (int x = 0; x < width; x++) {
      int loc = x + y*width;
      if (crowther.pixels[loc]>-16777214) {
        float latitude = float(y)/height;
        float longitude = float(x)/width;
        latitude -= 0.5;
        longitude -= 0.5;
        latitude *= 180.0;
        longitude *= 360.0;
        //crowther.pixels[i]
        visObjects.add(new PVector(longitude, latitude, 200000));
      }
    };
  };
  crowther.loadPixels();
  image(crowther, 0, 0);

  json(); 
  //exit();
}
void draw() {
}
void mouseMoved() {
  float latitude = float(mouseY)/height;
  float longitude = float(mouseX)/width;
  latitude -= 0.5;
  longitude -= 0.5;
  latitude *= 180.0;
  longitude *= 360.0;
  println(longitude+" "+latitude+" "+color(crowther.pixels[mouseX+(mouseY*width)]));
}
