void json() {
  JSONObject json;

  JSONArray features = new JSONArray();
  for (int i = 0; i < visObjects.size(); i++) {
    JSONObject geometry = new JSONObject();
    geometry.setString("type", "Feature");

    JSONObject polygon = new JSONObject();
    polygon.setString("type", "Polygon");
    JSONArray cord2D = new JSONArray();
    JSONArray cord3D = new JSONArray();
    PVector basCord = visObjects.get(i);
    for (int j = 0; j < 3; j++) {
      float posY =  sin((TWO_PI/3)*j)*.1;
      float posX =  cos((TWO_PI/3)*j)*.1;
      JSONArray cord = new JSONArray();
      cord.setFloat(0, basCord.x+posX);
      cord.setFloat(1, basCord.y+posY);
      //  cord.setFloat(2, i > 6 ? 0 : 1);
      cord2D.setJSONArray(j, cord);
    }
    cord3D.setJSONArray(0, cord2D);
    polygon.setJSONArray("coordinates", cord3D);
    geometry.setJSONObject("geometry", polygon);
    features.setJSONObject(i, geometry);

    JSONObject properties = new JSONObject();
    properties.setFloat("height", random(100000,2000000));
    geometry.setJSONObject("properties", properties);
  }
  json = new JSONObject();
  //json.setString("type", "FeatureCollection");
  json.setString("type", "FeatureCollection");
  json.setJSONArray("features", features);
  saveJSONObject(json, "../Cesium_test/new.geojson");
}
