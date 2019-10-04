import processing.core.*;
import processing.video.*;
import java.util.ArrayList;

public class Sketch extends PApplet {

  public static void main(String args[]) {
    PApplet.main("ExampleApplet");
  }

  Capture cam;

  int camFPS = 0;
  double lastS = 0;
  int IRColor;
  int count = 0;
  // the total marked pixels
  int totalPixels = 0;
  boolean marks[][];
  long lastMillis;
  int frameDelay = 0;
  int frameDuration;
  ArrayList<Rect> blobs;
  ArrayList<PImage> images;


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
    String extcam = "USB 2.0 Camera #3";
    cam = new Capture(this, 1280, 720, extcam, 60);
    cam.start();
    // initialize track color to IR
    IRColor = color(255, 255, 255);
    marks = new boolean[width][height];

    blobs = new ArrayList<Rect>();
    blobs.add(new Rect(0,0,0,0));
    //
    // Array for detection colors
     images = new ArrayList<PImage>();
  }

  @Override
  public void keyPressed() {

  }


    @Override
  public void draw() {

    int s = second();
    //println(m%1000);
    if (s != lastS) {
      //surface.setTitle("Cam_fps: " + camFPS +" frameDelay: "+frameDelay);
      camFPS = 0;
      lastS = s;
    }

    if (cam.available()) {
      frameDelay = (int) ( millis()- lastMillis);
      lastMillis = millis();
      // Compensate for variable frame rate in counter
      surface.setTitle("frameDelay: "+frameDelay +" fps:"+floor(frameRate));
      cam.read();
      findBlob(cam);
      set(0, 0, cam);
      camFPS++;
    }

      blobs.get(0).draw(this);

  }


  public void findBlob(PImage video) {
    int threshold = 140;

    // reset total
    totalPixels = 0;

    // prepare point trackers
    int lowestX = video.width;
    int lowestY = video.height;
    int highestX = 0;
    int highestY = 0;

    // threshold image pass
    for (int x = 1; x < video.width-1; x ++ ) {
      for (int y = 1; y < video.height-1; y ++ ) {
        // get pixel location
        int loc = x + y*video.width;
        int up = x + (y+1)*video.width;
        int down = x + (y-1)*video.width;
        int left = loc-1;
        int right = loc+1;
        int KernalVal = (video.pixels[loc] >> 16) & 0xFF;
        KernalVal += (video.pixels[up] >> 16) & 0xFF;
        KernalVal += (video.pixels[down] >> 16) & 0xFF;
        KernalVal += (video.pixels[left] >> 16) & 0xFF;
        KernalVal += (video.pixels[right] >> 16) & 0xFF;
        // get color of pixel

        int currentColor = KernalVal/5; // red

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
    blobs.get(0).set(lowestX, lowestY,highestX, highestY, frameDelay);
    // save locations
  }

}



