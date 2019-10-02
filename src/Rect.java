import processing.core.*;

public class Rect {
    private int x, y, w,h,lowestX,highestX,lowestY,highestY;
    float xAverage;
    float yAverage;
    float wAverage = 0f;
    float thesholdHigh = -2f;
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
    public void set(int x1, int y1,int x2, int y2) {
        this.lowestX = x1;
        this.lowestY = y1;
        this.highestX = x2;
        this.highestY = y2;
        setWidth();
        setHeight();
        setX();
        setY();
        if (LEDisHigh()) {
                  System.out.println("1___");
                } else {
                   System.out.println("0");
                 }
        }

    private boolean LEDisHigh() {
        if ((w-wAverage) <= thesholdHigh) {
            return false;
        } else {
            return true;
        }
    }
    private void setWidth() {
        this.w = Math.abs(highestX-lowestX);
        wAverage *= .9;
        wAverage += w*.1;
    }
    private void setHeight() {
        this.h = Math.abs(highestY-lowestY);
    }
    private void setX() {
        int newX = lowestX+(w/2);
        this.xAverage *= .5;
        this.xAverage += newX*.5;
        this.x = (int)xAverage;
    }
    private void setY() {
        int newY = lowestY+(h/2);
        this.yAverage *= .5;
        this.yAverage += newY*.5;
        this.y = (int)yAverage;
    }

    public void draw(PApplet applet) {
        applet.stroke(255, 0, 0);
        applet.fill(255, 0, 0);
        applet.ellipse(x,y,2,2);
        applet.text("ID:"+(w-wAverage),x+15,y);
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
