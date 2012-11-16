package com.geertvanderploeg.kiekeboek.syncadapter;

import java.util.ArrayList;
import java.util.List;

import com.geertvanderploeg.kiekeboek.client.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONPersonExtractor {

  private static final String TAG = "JSONPersonExtractor";

  public List<User> extract(String json) {
    ArrayList<User> users = new ArrayList<User>();

    try {
      JSONObject export = new JSONObject(json);
      JSONArray persons = export.getJSONArray("data");
      Log.d(TAG, String.format("JSON data is: version: %s, since: %s, nr of items: %d", export.get("version"),
        export.get("since"), persons.length()));

      for (int i = 0; i < persons.length(); i++) {
        JSONObject jsonObject = persons.getJSONObject(i);
        User parsedUser = User.valueOf(jsonObject);
        if (parsedUser == null) {
          Log.i(TAG, "Skipping user, cannot compose User-object from JSON string: " + jsonObject);
        } else {
          users.add(parsedUser);
        }
      }
      return users;

    } catch (JSONException e) {
      Log.e(TAG, "Exception occurred while parsing JSON from intranet export: " + e.getMessage(), e);
      return null;
    }
  }
}
