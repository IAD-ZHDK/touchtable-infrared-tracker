import processing.core.*;
import processing.video.*;
import gab.opencv.*;
import java.awt.Rectangle;
import java.util.ArrayList;

public class Sketch extends PApplet {
  public static void main(String args[]) {
    PApplet.main("ExampleApplet");
  }

  Capture cam;
  OpenCV opencv;
  int camFPS = 0;
  double lastS = 0;
  int IRColor;
  PImage src;

  ArrayList<Contour> contours;
  // <1> Set the range of Hue values for our filter
//ArrayList<Integer> colors;

  int hue;
  int rangeWidth = 10;
  PImage output;
  int colorToChange = -1;

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
    //
    opencv = new OpenCV(this, 1280, 720);

    contours = new ArrayList<Contour>();
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
      camFPS++;
    }
    //image(cam, 0, 0);

    //CV
    // load pixels


    // <2> Load the new frame of our movie in to OpenCV
    opencv.loadImage(cam);

    // Tell OpenCV to use color information
    opencv.useColor();
    src = opencv.getSnapshot();

    // <3> Tell OpenCV to work in HSV color space.
    opencv.useColor(HSB);

    set(0, 0, src);
    detectColors();

    displayContoursBoundingBoxes();


  }


  void detectColors() {

    if (hue <= 0) {
      opencv.loadImage(src);
      opencv.useColor(RGB);
      // <4> Copy the Hue channel of our image into
      //     the gray channel, which we process.
      opencv.setGray(opencv.getR());
      int hueToDetect = hue;
      int colorToDetect = IRColor;
      int brightnessToDetect = 255;
      //println("index " + i + " - hue to detect: " + hueToDetect);

      // <5> Filter the image based on the range of
      //     hue values that match the object we want to track.
      opencv.inRange(brightnessToDetect - rangeWidth / 2, brightnessToDetect + rangeWidth / 2);
      //opencv.dilate();
      opencv.erode();

      // TO DO:
      // Add here some image filtering to detect blobs better

      // <6> Save the processed image for reference.
      output = opencv.getSnapshot();
    }

    // <7> Find contours in our range image.
    //     Passing 'true' sorts them by descending area.
    if (output != null) {

      opencv.loadImage(output);
      contours = opencv.findContours(true, true);
    }
  }




  void displayContoursBoundingBoxes() {

    for (int i=0; i<contours.size(); i++) {

      Contour contour = contours.get(i);
      Rectangle r = contour.getBoundingBox();

      if (r.width < 20 || r.height < 20)
        continue;

      stroke(255, 0, 0);
      fill(255, 0, 0, 150);
      strokeWeight(2);
      rect(r.x, r.y, r.width, r.height);
    }
  }


}