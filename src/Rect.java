import processing.core.*;

public class Rect {

    private int x, y, w,h,lowestX,highestX,lowestY,highestY;
    float xAverage;
    float yAverage;
    float wAverage = 0f;
    int lastW = 0;
    int lastBit = 0;
    int ledState = 0;
    String bitStream = "";
    String bitStreamTotal = "";
    int frameCount = 0;


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
        LEDState();
        int bit = LEDState();
        frameCount += frameDuration;

            if (bit == 2) {
                bitStream = "";
                frameCount = 0;
            } else {
               if (bit!= lastBit ) {
                  lastBit = bit;
                  int noCycles = frameCount/120;  // arduino is outputing on # mili periods
                      bit ^= 1; // invert, since we are on the rising or falling edge of the last bit
                      int j = 0;
                      do {
                          bitStream += bit;
                          j++;
                      } while (j < noCycles);
                   frameCount = 0;
                }
              }

            if (bitStream.length() == 8) {
                bitStreamTotal = bitStream;
            }
      }

    private int LEDState() {
        //compare if the led is greater or less than % of last reading

        switch(ledState) {
            case 0:
            if (w > lastW*1.2f) {
                ledState =  1;
            }
            if (w > lastW*1.7) {
                ledState = 2;
            }
            break;
            case 1:
            if (w < lastW*.8f) {
                    ledState =  0;
            }
            if (w > lastW*1.4) {
                  ledState =  2;
            }
            break;
            case 2:
           //start bit
            if (w < lastW*.85) {
                 ledState = 1;
            }
            if (w < lastW*.6) {
                  ledState = 0;
            }
            break;
        }
          lastW = w;

        return ledState;
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
        this.xAverage *= .2;
        this.xAverage += newX*.8;
        this.x = (int)xAverage;
    }

    private void setY() {
        int newY = lowestY+(h/2);
        this.yAverage *= .2;
        this.yAverage += newY*.8;
        this.y = (int)yAverage;
    }

    public void draw(PApplet applet) {
        applet.stroke(255, 0, 0);
        applet.fill(255, 0, 0);
        applet.ellipse(x,y,2,2);
        applet.text("ID:"+ bitStreamTotal,x+15,y);
        applet.text("State:"+ ledState,x+15,y+15);
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
