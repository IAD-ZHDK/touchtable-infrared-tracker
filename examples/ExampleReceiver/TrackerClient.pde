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
    tactileObject.rotation = msg.get(4).floatValue();
    tactileObject.intensity = msg.get(5).floatValue();

    return tactileObject;
  }

  private int getUniqueId(OscMessage msg) {
    return msg.get(0).intValue();
  }

  private String createAddress(String command) {
    return namespace + "/" + command;
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
}
