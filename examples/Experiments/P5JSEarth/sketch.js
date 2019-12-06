let img;
let sky;
let theta = 0.001;
let r = 400;
let easycam;
let toggle = false;
function preload() {
  img = loadImage('earth.jpg');
	sky = loadImage('sky.jpg');
}

function setup() {
	createCanvas(windowWidth, windowHeight, WEBGL);
	noStroke();
	easycam = createEasyCam();
	// fix for EasyCam to work with p5 v0.7.2
 easycam = new Dw.EasyCam(this._renderer, {distance:700, center:[0,0,0]});
 easycam.setDistanceMin(500)
  easycam.setDistanceMax(r*6)
	// suppress right-click context menu
//	  document.oncontextmenu = function() { return false; }
}

function draw() {
  background(0);
	easycam.rotateY(theta);
  //theta += 0.001;
	ambientLight(60, 60, 60);
 	let v1 = easycam.getPosition(500);
//	console.log(v1);

 pointLight(255,255, 255, v1[0], v1[1]+300, v1[2]);
 pointLight(255, 255, 255, v1[0], v1[1]+300, v1[2]);
//	pointLight(255, 255, 255,-700, -700, 0);
	//pointLight(255, 255, 255,-700, -700, 0);

// let dirX = (mouseX / width - 0.5) * 2;
 //let dirY = (mouseY / height - 0.5) * 2;
	//pointLight(255, 255, 255, -dirX, -dirY, -1);

	shininess(50);

  texture(img);
  sphere(r,80,80);
	noLights();
 	ambientLight(50, 50, 50);
  texture(sky);
	sphere(r*10,6,6);

	//Sydney, Australia Latitude/Longtidue
	  var lat = radians(-33.8688);
	  var lon = radians(151.2093);

	  //cartesian coordinates
	  var x = r * Math.cos(lat) * Math.sin(lon+radians(180));
	  var y = r * Math.sin(-lat);
	  var z = r * Math.cos(lat) * Math.cos(lon+radians(180));

		var x2 = (r+100) * Math.cos(lat) * Math.sin(lon+radians(180));
	 	var y2 = (r+100) * Math.sin(-lat);
	 	var z2 = (r+100) * Math.cos(lat) * Math.cos(lon+radians(180));

		strokeWeight(100);
		drawLine(x,y,z,x2,y2,z2);

		draw2d();
}
function draw2d() {
	easycam.beginHUD()
	ambientLight(255, 255, 255);
	if (toggle) {
		fill(78	,196,164);
	} else {
		fill(255,0,0);
	}

	rect(100,100,100,100)
	//pointLight(0, 0, 255, 0, 0, -600);
//	pointLight(0, 0, 255, 0, 0, -600);
	easycam.endHUD()
}

function windowResized() {
  resizeCanvas(windowWidth, windowHeight);
}

function mouseClicked() {
	toggle = !toggle;
}

function drawLine(x1, y1, z1, x2, y2, z2){
	stroke(255,255,255);
  beginShape();
  vertex(x1,y1,z1);
  vertex(x2,y2,z2);
  endShape();
	noStroke();
}
