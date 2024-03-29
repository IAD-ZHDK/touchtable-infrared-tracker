import oscP5.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;

class TrackerClient {
  private OscP5 osc;
  private Map<Integer, TactileObject> markers = new ConcurrentHashMap<Integer, TactileObject>();
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

    TactileObject marker = messageToTactileObject(msg);
    markers.put(uniqueId, marker);
  }

  private void updateTactileObject(OscMessage msg) {
    int uniqueId = getUniqueId(msg);

    if (markers.containsKey(uniqueId)) {
      messageToTactileObject(msg, markers.get(uniqueId));
    } else {
      addTactileObject(msg);
    }
  }

  private void removeTactileObject(OscMessage msg) {
    int uniqueId = getUniqueId(msg);

    if (markers.containsKey(uniqueId)) {
      TactileObject marker = markers.get(uniqueId);
      marker.dead = true;
      markers.remove(uniqueId);
    }
  }

  private TactileObject messageToTactileObject(OscMessage msg) {
    return messageToTactileObject(msg, new TactileObject());
  }

  private TactileObject messageToTactileObject(OscMessage msg, TactileObject marker) {
    marker.uniqueId = getUniqueId(msg);
    marker.identifier = msg.get(1).intValue();
    marker.x = msg.get(2).floatValue();
    marker.y = msg.get(3).floatValue();
    marker.rotation = msg.get(4).floatValue();
    marker.intensity = msg.get(5).floatValue();

    return marker;
  }

  private int getUniqueId(OscMessage msg) {
    return msg.get(0).intValue();
  }

  private String createAddress(String command) {
    return namespace + "/" + command;
  }

  public synchronized List<TactileObject> getTactileObjects() {
    return new ArrayList<TactileObject>(markers.values());
  }

  public int count() {
    return markers.size();
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
