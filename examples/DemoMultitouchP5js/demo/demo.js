function setup() {
  createCanvas(windowWidth, windowHeight);
  noStroke();
  fill(255);
}

function draw() {
  background(255, 0, 0);
  let display = touches.length + ' touches';

  text(display, 5, 10);
  for (let i = 0; i < touches.length; i++) {
 
    circle( touches[i].x,  touches[i].y, 50);
  }
   circle( mouseX,  mouseY, 50);
}

function mousePressed() {
    let fs = fullscreen();
    fullscreen();
}

function windowResized() {
  resizeCanvas(windowWidth, windowHeight);
}
