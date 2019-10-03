import processing.core.*;

public class Rect {
    private int x, y, w,h,lowestX,highestX,lowestY,highestY;
    float xAverage;
    float yAverage;
    float wAverage = 0f;
    int lastW = 0;
    float thesholdHigh = 1.02f;
    float thesholdStartBit = 1.5f;
    int lastBit = 0;
    int ledState = 0;
    String bitStream = "";
    int frameCount = 0;

    private int freqency;
    public Rect(int x1, int y1,int x2, int y2) {
        this.lowestX = x1;
        this.highestX = x2;
        this.lowestY = y1;
        this.highestY = y2;
        setWidth();
        setHeight();
        setX();
        setY();
    }
    public void set(int x1, int y1,int x2, int y2, int frameDuration) {
        this.lowestX = x1;
        this.lowestY = y1;
        this.highestX = x2;
        this.highestY = y2;
        setWidth();
        setHeight();
        setX();
        setY();
        int bit = LEDState();
        frameCount += frameDuration;
        if (bit!= lastBit) {
           lastBit = bit;
           int noCycles = frameCount/80;  // arduino is outputing on # mili periods
              if (bit == 2) {
              //start bit
              bitStream = "";
              }  else {
               bit ^= 1; // invert, since we are on the rising or falling edge of the last bit
               int j = 0;
               do {
                   bitStream += bit;
                   //System.out.print(bit);
                   j++;
               } while (j < noCycles);
           }
            frameCount = 0;
        }
       }
    private int LEDState() {
         int outPut;
        //compare the led is greater than % of average
        //maybe it would work to just compare it to the last frame.
        if (w >= wAverage*thesholdStartBit) {
            outPut = 2;
        } else if (w >= wAverage*thesholdHigh) {
            outPut =  1;
        } else {
            outPut =  0;
        }
          lastW = w;
          return outPut;
    }
    private void setWidth() {
        this.w = Math.abs(highestX-lowestX);
        wAverage *= .95;
        wAverage += w*.05;
    }
    private void setHeight() {
        this.h = Math.abs(highestY-lowestY);
    }
    private void setX() {
        int newX = lowestX+(w/2);
        this.xAverage *= .3;
        this.xAverage += newX*.7;
        this.x = (int)xAverage;
    }
    private void setY() {
        int newY = lowestY+(h/2);
        this.yAverage *= .3;
        this.yAverage += newY*.7;
        this.y = (int)yAverage;
    }

    public void draw(PApplet applet) {
        applet.stroke(255, 0, 0);
        applet.fill(255, 0, 0);
        applet.ellipse(x,y,2,2);
        applet.text("ID:"+ bitStream,x+15,y);
        applet.noFill();
        applet.rect(lowestX,lowestY,w,h);

    }

    public void reset() {
        this.lowestX = 0;
        this.highestX = 0;
        this.lowestY = 0;
        this.highestY = 0;
        this.y = 0;
        this.x = 0;
        this.w = 0;
        this.h = 0;
    }
     public void findID(boolean video) {
      }
}
