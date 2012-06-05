package com.geertvanderploeg.kiekeboek.syncadapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.geertvanderploeg.kiekeboek.R;
import com.geertvanderploeg.kiekeboek.client.User;
import com.geertvanderploeg.kiekeboek.syncadapter.images.ImageConnector;
import com.geertvanderploeg.kiekeboek.syncadapter.images.PrefilledImageConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

public class JSONResourceUserConnector implements UserConnector {
    
    private static final int KIEKEBOEK_SEED = R.raw.kiekeboek_seed;
    private static final String TAG = "JSONResourceUserConnector";

  private ImageConnector imageConnector = new PrefilledImageConnector();

  @Override
    public List<User> fetchUpdates(Context context, Account account, String authtoken, Date mLastUpdated) {
        ArrayList<User> l = new ArrayList<User>();
        try {
            JSONArray persons = new JSONArray(getStringFromRawResource(context, KIEKEBOEK_SEED));

            for (int i = 0; i < persons.length(); i++) {
                JSONObject jsonObject = persons.getJSONObject(i);
                User parsedUser = User.valueOf(jsonObject);
                if (parsedUser == null) {
                    Log.w(TAG, "Skipping user, cannot compose User-object from JSON string: " + jsonObject);
                } else {
                  imageConnector.addPhoto(parsedUser, context);
                    l.add(parsedUser);
                }
            }
            return l;
        } catch (JSONException e) {
            Log.e(TAG, "Exception occurred while parsing JSON from seed: " + e.getMessage(), e);
        }
        return l;
    }

    protected final String getStringFromRawResource(Context context, int resourceId) {
        InputStream is = context.getResources().openRawResource(resourceId);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (Exception e) {
            throw new RuntimeException("While reading string from resource " + resourceId, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception occurred: " + e.getMessage(), e);
            }
        }
        return writer.toString();
    }

}
