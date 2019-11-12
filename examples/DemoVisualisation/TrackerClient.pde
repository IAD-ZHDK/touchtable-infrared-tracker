import oscP5.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;

class TrackerClient {
  private OscP5 osc;
  private Map<Integer, TactileObject> tactileObjects = new ConcurrentHashMap<Integer, TactileObject>();
  private String namespace;

  private String addPattern;
  private String updatePattern;
  private String removePattern;

  public TrackerClient(int port) {
    this(port, "/tracker");
  }

  public TrackerClient(int port, String namespace) {
    osc = new OscP5(this, port);
    this.namespace = namespace;

    addPattern = createAddress("add");
    updatePattern = createAddress("update");
    removePattern = createAddress("remove");
  }

  private synchronized void oscEvent(OscMessage msg) {
    if (msg.checkAddrPattern(addPattern)) {
      addTactileObject(msg);
      return;
    }

    if (msg.checkAddrPattern(updatePattern)) {
      updateTactileObject(msg);
      return;
    }

    if (msg.checkAddrPattern(removePattern)) {
      removeTactileObject(msg);
      return;
    }
  }

  private void addTactileObject(OscMessage msg) {
    int uniqueId = getUniqueId(msg);

    TactileObject tactileObject = messageToTactileObject(msg);
    tactileObjects.put(uniqueId, tactileObject);

    // add user defined
    tactileObject.creationTime = millis();
    tactileObject.position = new PVector(tactileObject.x, tactileObject.y);
    tactileObject.smoothRotation = tactileObject.rotation;
  }

  private void updateTactileObject(OscMessage msg) {
    int uniqueId = getUniqueId(msg);

    if (tactileObjects.containsKey(uniqueId)) {
      messageToTactileObject(msg, tactileObjects.get(uniqueId));
    } else {
      addTactileObject(msg);
    }
  }

  private void removeTactileObject(OscMessage msg) {
    int uniqueId = getUniqueId(msg);

    if (tactileObjects.containsKey(uniqueId)) {
      TactileObject tactileObject = tactileObjects.get(uniqueId);
      tactileObject.dead = true;
      tactileObjects.remove(uniqueId);
    }
  }

  private TactileObject messageToTactileObject(OscMessage msg) {
    return messageToTactileObject(msg, new TactileObject());
  }

  private TactileObject messageToTactileObject(OscMessage msg, TactileObject tactileObject) {
    tactileObject.uniqueId = getUniqueId(msg);
    tactileObject.identifier = msg.get(1).intValue();
    tactileObject.x = msg.get(2).floatValue();
    tactileObject.y = msg.get(3).floatValue();
    tactileObject.intensity = msg.get(5).floatValue();

    // special rotation update for 0-360 degrees 
    tactileObject.rotation = calculate2PIRotation(tactileObject.rotation, msg.get(4).floatValue());

    tactileObject.updateTime = millis();

    return tactileObject;
  }

  private int getUniqueId(OscMessage msg) {
    return msg.get(0).intValue();
  }

  private String createAddress(String command) {
    return namespace + "/" + command;
  }

  private float calculate2PIRotation(float currentValue, float newValue) {
    float delta = newValue - currentValue;
    float sign = delta / delta;

    if (isBetween(abs(delta), 150f, 300f)) {
      newValue += 180f * sign;

      if (newValue > 360) {
        newValue -= 360;
      }
    }

    return newValue;
  }

  private boolean isBetween(float value, float low, float high) {
    return (value >= low && value < high);
  }

  public synchronized List<TactileObject> getTactileObjects() {
    return new ArrayList<TactileObject>(tactileObjects.values());
  }

  public int count() {
    return tactileObjects.size();
  }
}

class TactileObject {
  int uniqueId;
  int identifier;
  float x;
  float y;
  float rotation;
  float intensity;
  boolean dead;

  long creationTime;
  long updateTime;

  // user specific
  PVector position = new PVector();
  float smoothRotation = 0;

  // update position easing and so on
  public void update() {
    // circular easing
    smoothRotation = ease(rotation, smoothRotation, 0.1);

    // todo: use vector methods
    position.x = ease(x, position.x, 0.1);
    position.y = ease(y, position.y, 0.1);
  }

  // basic easing method
  private float ease(float target, float value, float alpha) {
    float d = target - value;
    return value + (d * alpha);
  }

  private float circularEase(float target, float value, float maxValue, float alpha) {
    float delta = target - value;
    float altDelta = maxValue - abs(delta);

    if (abs(altDelta) < abs(delta)) {
      delta = altDelta * (delta < 0 ? 1 : -1);
    }

    return value + (delta * alpha);
  }
}
