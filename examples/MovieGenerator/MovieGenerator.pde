import com.hamoid.*;

VideoExport videoExport;

ArrayList<Command> commands = new ArrayList<>();

float stopSize = 300;
float highSize = 200;
float lowSize = 100;

int totalTime = 0;
int lastTimeStamp = 0;
int commandIndex = 0;

int id = 88;
int flankTime = 5;
int startAndEndTime = 100;

void setup() {
  size(1280, 720);
  
  // show pattern:
  for (int i = 0; i < 8; i++) {
    print((bitRead(id, 7 - i) > 0) ? "H, " : "L, ");
  }
  println();

  // create pattern
  commands.add(new Command(startAndEndTime, ShowType.None));

  // display one number
  int count = 5;
  while (count > 0) {
    commands.add(new Command(flankTime, ShowType.Stop));
    for (int i = 0; i < 8; i++) {
      ShowType type = (bitRead(id, 7 - i) > 0) ? ShowType.High : ShowType.Low;
      commands.add(new Command(flankTime, type));
    }
    commands.add(new Command(flankTime, ShowType.Stop));
    count--;
  }

  commands.add(new Command(startAndEndTime, ShowType.None));

  // calc total time
  for (Command c : commands) {
    totalTime += c.time;
  }

  videoExport = new VideoExport(this);
  videoExport.startMovie();
}
void draw() {
  background(0);
  surface.setTitle("Frame: " + frameCount);

  Command cmd = commands.get(commandIndex);

  // render
  noStroke();
  fill(255);
  switch(cmd.type) {
  case Low:
    circle(width / 2, height / 2, lowSize);
    break;

  case High:
    circle(width / 2, height / 2, highSize);
    break;

  case Stop:
    circle(width / 2, height / 2, stopSize);
    break;
  }

  videoExport.saveFrame();

  // stop video
  if (frameCount >= totalTime) {
    videoExport.endMovie();
    exit();
  }

  // switch cmd
  if (frameCount - lastTimeStamp > cmd.time) {
    lastTimeStamp = frameCount;
    commandIndex++;
  }
}

int bitRead(int b, int bitPos)
{
  int x = b & (1 << bitPos);
  return x == 0 ? 0 : 1;
}
