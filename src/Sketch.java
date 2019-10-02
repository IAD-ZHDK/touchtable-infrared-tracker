import processing.core.*;
import processing.video.*;
//import gab.opencv.*;
import java.awt.Rectangle;
import java.util.ArrayList;

public class Sketch extends PApplet {
  public static void main(String args[]) {
    PApplet.main("ExampleApplet");
  }

  Capture cam;

  int camFPS = 0;
  double lastS = 0;
  int IRColor;
  PImage src;

  // the total marked pixels
  int totalPixels = 0;
  boolean marks[][];
  // the most top left pixel
  PVector topLeft;

  // the most bottom right pixel
  PVector bottomRight;
  // <1> Set the range of Hue values for our filter
//ArrayList<Integer> colors;

  int hue;

  @Override
  public void settings() {
    size(1280, 720, FX2D);

  }

  @Override
  public void setup() {
    // String[] cameras = Capture.list();
    // if (cameras.length == 0) {
    //   println("There are no cameras available for capture.");
    //    exit();
    // } else {
    //   println("Available cameras:");
    //   for (int i = 0; i < cameras.length; i++) {
    //     print(i+"+");
    //     println(cameras[i]);
    //   }

    //1920 (H) x 1080 (V) pixels    MJPEG 30fps      YUY2 6fps
    //1280 (H) x 1024 (V) pixels    MJPEG 30fps      YUY2 6fps
    //1280 (H) x  720 (V) pixels    MJPEG 60fps      YUY2 9fps
    //1024 (H) x  768 (V) pixels    MJPEG 30fps      YUY2 9fps
    //800  (H) x  600 (V) pixels    MJPEG 60fps      YUY2 21fps
    //640  (H) x  480 (V) pixels    MJPEG 120fps     YUY2 30fps
    //352  (H) x  288 (V) pixels    MJPEG 120fps     YUY2 30fps
    //320  (H) x  240 (V) pixels    MJPEG 120fps     YUY2 30fps
    //cam = new Capture(this);
    cam = new Capture(this, 1280, 720, "USB 2.0 Camera #2", 60);
    cam.start();
    // initialize track color to IR
    IRColor = color(255, 255, 255);
    hue = floor(map(hue(IRColor), 0, 255, 0, 180));
    marks = new boolean[width][height];

    topLeft = new PVector(0, 0);
    bottomRight = new PVector(0, 0);
    //
    // Array for detection colors


  }

  @Override
  public void draw() {

    int s = second();
    //println(m%1000);
    if (s != lastS) {
      surface.setTitle("fps" + camFPS);
      camFPS = 0;
      lastS = s;
    }

    if (cam.available() == true) {
      cam.read();
      findBlob(cam);
      set(0, 0, cam);
      camFPS++;
    }
    //image(cam, 0, 0);

    //CV
    // load pixels

    stroke(255, 0, 0);
    noFill();
    rect(topLeft.x, topLeft.y, bottomRight.x-topLeft.x, bottomRight.y-topLeft.y);



    // draw bounding box
    //stroke(255, 0, 0);
    //noFill();
    //rect(topLeft.x, topLeft.y, bottomRight.x-topLeft.x, bottomRight.y-topLeft.y);

  }


  void findBlob(PImage video) { {
    int threshold = 20;

    // reset total
    totalPixels = 0;

    // prepare point trackers
    int lowestX = width;
    int lowestY = height;
    int highestX = 0;
    int highestY = 0;


    // go through image pixel by pixel
    for (int x = 0; x < video.width; x ++ ) {
      for (int y = 0; y < video.height; y ++ ) {
        // get pixel location
        int loc = x + y*video.width;

        // get color of pixel

        int currentColor = (video.pixels[loc] >> 16) & 0xFF; // red

        // get distance to track color
        int dist = abs(255-currentColor);

        // reset mark
        marks[x][y] = false;

        // check if distance is below threshold
        if (dist < threshold) {
          // mark pixel
          marks[x][y] = true;
          totalPixels++;
          // update point trackers
          if (x < lowestX) lowestX = x;
          if (x > highestX) highestX = x;
          if (y < lowestY) lowestY = y;
          if (y > highestY) highestY = y;
        }
      }
    }

    // save locations
    topLeft = new PVector(lowestX, lowestY);
    bottomRight = new PVector(highestX, highestY);
  }
  }

  void detectColorsSimple(PImage video) {
// initialize record to number greater than the diagonal of the screen
    float record = width+height;

    // initialize variable to store closest point
    PVector closestPoint = new PVector();

    // get track color as vector

    // go through image pixel by pixel
    for (int x=0; x < video.width; x++) {
      for (int y=0; y < video.height; y++) {
        // get pixel location
        int loc = x + y * video.width;

        // get pixel color
        int currentColor = (video.pixels[loc] >> 16) & 0xFF; // red

        // calculate difference between current color and track color
        int dist = abs(255-currentColor);

        // save point if closer than previous
        if (dist < record) {
          record = dist;
          closestPoint.x = x;
          closestPoint.y = y;
        }
      }
    }

    // draw point if we found a one that is less than 10 apart
    if (record < 10) {
      noFill();
      strokeWeight(2);
      stroke(255,0,0);
      ellipse(closestPoint.x, closestPoint.y, 20, 20);
    }
  }


}