import processing.core.*;

public class Rect {
    private int x,y,w,h,lowestX,highestX,lowestY,highestY;

    public Rect(int x1,int x2,int y1,int y2) {
        this.lowestX = x1;
        this.highestX = x2;
        this.lowestY = y1;
        this.highestY = y2;
        setWidth();
        setHeight();
        setX();
        setY();
    }
    public void topLeft(int x1, int y1) {
        this.lowestX = x1;
        this.lowestY = y1;
        setWidth();
        setX();
    }
    public void bottomRight(int x2, int y2) {
        this.highestX = x2;
        this.highestY = y2;
        setHeight();
        setY();
    }
    private void setWidth() {
        this.w = Math.abs(highestX-lowestX);
    }
    private void setHeight() {
        this.h = Math.abs(highestY-lowestY);
    }
    private void setX() {
        this.x = lowestX+(w/2);
    }
    private void setY() {
        this.y = lowestY+(h/2);
    }


    public void draw(PApplet applet) {
        applet.ellipse(highestX,highestY,5,5);
        applet.rect(lowestX,lowestY,w,h);
    }
}
