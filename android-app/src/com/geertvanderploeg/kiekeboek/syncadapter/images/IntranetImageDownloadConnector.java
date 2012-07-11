package com.geertvanderploeg.kiekeboek.syncadapter.images;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URI;

import com.geertvanderploeg.kiekeboek.R;
import com.geertvanderploeg.kiekeboek.client.NetworkUtilities;
import com.geertvanderploeg.kiekeboek.client.User;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.util.Log;

public class IntranetImageDownloadConnector implements ImageConnector {

  private HttpClient httpClient = NetworkUtilities.getHttpClient();
  private static final String TAG = "IntranetImageDownloadConnector";

  @Override
  public void addPhoto(User user, Context context) {
    final String uri = String.format(context.getString(R.string.intranet_image_url_pattern), user.getUserId());
    Log.d(TAG, "About to get photo from URI: " + uri);
    final byte[] bytes = downloadImageBytes(uri);
    if (bytes != null) {
      user.setPhotoData(bytes);
    }
  }

  private byte[] downloadImageBytes(String uri) {
    final HttpGet get = new HttpGet();

    get.setURI(URI.create(uri));
    try {
      final HttpResponse response = httpClient.execute(get);
      if (response.getStatusLine().getStatusCode() == 200) {
        final long contentLength = response.getEntity().getContentLength();
        byte[] ba = new byte[(int) contentLength];
        DataInputStream dis = new DataInputStream(response.getEntity().getContent());
        dis.readFully(ba);
        response.getEntity().consumeContent();
        Log.d(TAG, "content length from image request: " + contentLength);
        return ba;
      } else {
        Log.w(TAG, "Response is not a 200: " + response.getStatusLine().toString());
        return null;
      }
    } catch (IOException e) {
      Log.e(TAG, "While downloading image: " + e.getMessage(), e);
      return null;
    } finally {
      httpClient.getConnectionManager().closeExpiredConnections();
    }

  }
}
