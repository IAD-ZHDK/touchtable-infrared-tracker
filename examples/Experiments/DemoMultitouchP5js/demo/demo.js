
let maxDistance = 200;
// Object tracking library:
//https://github.com/andypotato/tritra
// triangle with 18, 36 and 54 degrees vertex angles
var vertexAngles = [18, 36, 54];

// create the recognizer
var R = new tritra.Recognizer(vertexAngles, {
        /*
          Apex angles have a distance of 18 degrees each therefore we choose
          18 / 2 = 9 degrees of tolerance to each side. You can go lower than
          that if your application doesn't require markers to be moved quickly.
        */
        maxAngleTolerance: 4,
        /*
          The maximum distance for two points to still be considered as part of
          the same triangle. You should adjust these according to your screen
          size and DPI. As a general rule, the larger the screen and the higher
          the DPI, the higher you should set this value.
          Do not set it too high though as this will cause two nearby markers to
          no longer be recognized.
        */
        maxPointDistance: 170
      });

let mousePoints = [];;

function setup() {
  createCanvas(windowWidth, windowHeight);
  noStroke();
  fill(255);
  angleMode(DEGREES)
}



function draw() {
  let points = [];
  background(255, 0, 0);
  let display = 'touches:' + touches.length ;
  let display2 =  'frame Rate: ' + floor(frameRate());
  text(display, 5, 10);
  text(display2, 5, 20);
  circle(mouseX,  mouseY, 15);

  for (let i = 0; i < touches.length; i++) {
    circle(touches[i].x,  touches[i].y, 10);
    points.push(new tritra.Vector2d(touches[i].x, touches[i].y));
  }

  for (let i = 0; i < mousePoints.length; i++) {
    circle(mousePoints[i].x,  mousePoints[i].y, 10);
    points.push(mousePoints[i]);
  }



  var matches = R.findMatches(points);
  for (let i = 0; i < matches.length; i++) {
      var centerPoint = matches[i].getCenter();
      var orientation =  -matches[i].getOrientation();
      // index within your vertexAngles
      console.log(matches[i].getOrientation())
      fill(0);
      circle(centerPoint.x,  centerPoint.y, 200);
      stroke(255);
      strokeWeight(10);
      line(centerPoint.x, centerPoint.y, centerPoint.x+(sin(orientation)*150), centerPoint.y+(cos(orientation)*150));
      noStroke();
      strokeWeight(1);
  }

// connecting lines
  for (let i = 0; i < points.length; i++) {
    circle(points[i].x,  points[i].y, 10);
    for (let j = 0; j < points.length; j++) {
      let distance = dist(points[i].x,  points[i].y, points[j].x,  points[j].y)
      if (j!=i && distance<maxDistance) {
       stroke(255);
       line(points[i].x,  points[i].y, points[j].x,  points[j].y);
       noStroke();
      }
    }
  }

}




function mousePressed() {
    let fs = fullscreen();
    mousePoints.push(new tritra.Vector2d(mouseX, mouseY));
    fullscreen();
}

function windowResized() {
  resizeCanvas(windowWidth, windowHeight);
}
