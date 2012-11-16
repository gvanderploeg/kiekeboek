package com.geertvanderploeg.kiekeboek.syncadapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import com.geertvanderploeg.kiekeboek.R;
import com.geertvanderploeg.kiekeboek.app.Notifications;
import com.geertvanderploeg.kiekeboek.client.User;
import com.geertvanderploeg.kiekeboek.syncadapter.images.ImageConnector;
import com.geertvanderploeg.kiekeboek.syncadapter.images.PrefilledImageConnector;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

public class JSONResourceUserConnector implements UserConnector {
    
    private static final int KIEKEBOEK_SEED = R.raw.kiekeboek_seed;
    private static final String TAG = "JSONResourceUserConnector";

  private ImageConnector imageConnector = new PrefilledImageConnector();
  private JSONPersonExtractor extractor = new JSONPersonExtractor();

  @Override
    public List<User> fetchUpdates(Context context, Account account, String authtoken, Date mLastUpdated) {
          String json = getStringFromRawResource(context, KIEKEBOEK_SEED);
          List<User> users = extractor.extract(json);
          if (users == null) {
            Notifications.addNotification(context, "Local resource user connector", "Could not parse person data.");
          } else {
            for (User u : users) {
              imageConnector.addPhoto(u, context);
            }
          }
        return users;
    }

  /**
   * This implementation simply ignores the last update time
   * @param context
   * @return
   */
  @Override
  public Date getLastSyncDate(Context context) {
    return new Date(0);
  }

  /**
   * See javadoc of {@link #getLastSyncDate(Context)}
   * @param context
   * @param date
   */
  @Override
  public void saveLastSyncDate(Context context, Date date) {
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
