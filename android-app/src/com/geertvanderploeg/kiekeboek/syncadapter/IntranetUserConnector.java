package com.geertvanderploeg.kiekeboek.syncadapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.geertvanderploeg.kiekeboek.R;
import com.geertvanderploeg.kiekeboek.app.Notifications;
import com.geertvanderploeg.kiekeboek.client.NetworkUtilities;
import com.geertvanderploeg.kiekeboek.client.User;
import com.geertvanderploeg.kiekeboek.syncadapter.images.CombiningImageConnector;
import com.geertvanderploeg.kiekeboek.syncadapter.images.ImageConnector;
import com.geertvanderploeg.kiekeboek.syncadapter.intranet.IntranetSyncStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

public class IntranetUserConnector implements UserConnector {

  private static final String TAG = "IntranetUserConnector";

  private static final int notificationTitle = R.string.notificationtitle_intranetuserconnector;

  private ImageConnector imageConnector = new CombiningImageConnector();

  private JSONPersonExtractor extractor = new JSONPersonExtractor();

  @Override
  public List<User> fetchUpdates(Context context, Account account, String authtoken, Date lastUpdated) {
    List<User> users = new ArrayList<User>();

    final String jsonString = getJSONStringFromIntranet(account, authtoken, context, lastUpdated);
    if (jsonString == null) {
      Notifications.addNotification(context,  context.getString(notificationTitle), "downloaded JSON data is null");
    } else {
      users = extractor.extract(jsonString);
      if (users == null) {
        Notifications.addNotification(context,  context.getString(notificationTitle), "Could not parse person data.");
      } else {
        for (User user : users) {
          imageConnector.addPhoto(user, context);
        }
      }
    }
    return users;
  }

  @Override
  public Date getLastSyncDate(Context context) {
    return IntranetSyncStatus.getLastSyncDate(context);
  }

  @Override
  public void saveLastSyncDate(Context context, Date date) {
    IntranetSyncStatus.saveLastSyncDate(context, date);
  }

  private String getJSONStringFromIntranet(Account account, String authtoken, Context context, Date lastUpdated) {

    final Thread thread = NetworkUtilities.attemptAuth(account.name, authtoken, null, context);
    try {
      thread.join(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    HttpClient localHttpClient = NetworkUtilities.getHttpClient();
    HttpParams httpParameters = new BasicHttpParams();

    HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
    HttpConnectionParams.setSoTimeout(httpParameters, 30000);

    final HttpGet get = new HttpGet();
    get.setHeader("Accept", "application/json");

    String uri = context.getString(R.string.intranet_export_url);

    if (lastUpdated != null && lastUpdated.getTime() > 0) {
      long since = lastUpdated.getTime() / 1000;
      uri = uri + "?since=" + since;
    }
    Log.d(TAG, "About to get JSON string from intranet. URI: " + uri);
    get.setURI(URI.create(uri));
    Log.w(TAG, "Request headers: " + Arrays.asList(get.getAllHeaders()));
    try {
      Log.d(TAG, "Before execute GET...");
      final HttpResponse response = localHttpClient.execute(get);
      Log.d(TAG, "After execute GET...");

      if (response.getStatusLine().getStatusCode() == 200) {
        final InputStream stream = response.getEntity().getContent();
        final String body = new Scanner(stream).useDelimiter("\\A").next(); // Oneliner stream to string
        response.getEntity().consumeContent();
        Log.d(TAG, "Body from intranet-export request: " + body);
        return body;
      } else {
        Log.w(TAG, "Response is not a 200: " + response.getStatusLine().toString());
        Log.w(TAG, "Response headers: " + Arrays.asList(response.getAllHeaders()));
        Notifications.addNotification(context, "Error while downloading intranet-export", "Response is not a 200: " + response.getStatusLine().toString());
        return null;
      }
    } catch (IOException e) {
      Log.e(TAG, "While downloading intranet export: " + e.getMessage(), e);
      Notifications.addNotification(context, "Error while downloading intranet-export", e.getMessage());
      return null;
    } finally {
      localHttpClient.getConnectionManager().closeExpiredConnections();
    }
  }

}
