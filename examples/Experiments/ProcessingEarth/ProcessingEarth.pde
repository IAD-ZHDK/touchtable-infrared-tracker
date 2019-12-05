//import peasy.PeasyCam;

PShape planet1;
PShape clouds;
PImage surftex1;
PImage cloudtex;
float angleY;
float angleX = -0.04;
float angleCloud = 0;
//PeasyCam cam;
public void settings() {
  fullScreen( P3D, 2);
  smooth(5);
}

void setup() {
  // cam = new PeasyCam(this, 400);
  surftex1 = loadImage("planet.jpg");  
  surftex1.resize(surftex1.width, surftex1.height);
  sphereDetail(40);
  noStroke();
  planet1 = createShape(SPHERE, height/2.8);
  planet1.setTexture(surftex1);


  // clouds
  // PImage TempClouds =  loadImage("clouds.jpg");  
  //cloudtex = new PImage(TempClouds.width,TempClouds.height,ARGB); 
  //cloudtex.loadPixels();

  //noStroke();
  //for (int i = 0; i < cloudtex.pixels.length; i++) {
  //  color fillColor = color(255,255,255,map(red(TempClouds.pixels[i]),0,255,0,100));
  //  cloudtex.pixels[i] = fillColor;
  //}
  //cloudtex.updatePixels();
  //clouds.setTexture(cloudtex);
  //  clouds = createShape(SPHERE, 351);
}

void draw() {
  // Even we draw a full screen image after this, it is recommended to use
  // background to clear the screen anyways, otherwise A3D will think
  // you want to keep each drawn frame in the framebuffer, which results in 
  // slower rendering.
  background(0);

  // Disabling writing to the depth mask so the 
  // background image doesn't occludes any 3D object.
  hint(DISABLE_DEPTH_MASK);
  //image(starfield, 0, 0, width, height);
  hint(ENABLE_DEPTH_MASK);

  directionalLight(255, 255, 255, -1, -1, -1);

  ambientLight(100, 100, 100);
  //translate(0, 500, 0);
  translate(width/2, height/2, 0);  
  angleY += 0.001;
  angleCloud-= 0.0001;
  rotateX(angleX);
  rotateY(angleY);
  shape(planet1);
  //rotateY(angleCloud);
  // shape(clouds);
  if(frameCount%120 == 0) println(frameRate);
}
boolean flag = false;
void mouseDragged() {
  if (flag) {
    angleY += (mouseX-pmouseX )*0.0006;
    angleX += (pmouseY-mouseY)*0.0006;
  }
  flag = true;
}
void mouseReleased() {
  flag = false;
}
