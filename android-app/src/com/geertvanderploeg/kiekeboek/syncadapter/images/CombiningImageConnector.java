package com.geertvanderploeg.kiekeboek.syncadapter.images;

import java.io.IOException;

import com.geertvanderploeg.kiekeboek.client.User;
import com.geertvanderploeg.kiekeboek.support.FileCache;

import android.content.Context;
import android.util.Log;

/**
 * First look in the cache directory.
 * Then in the prefilled assets directory.
 * Then using HTTP in the intranet.
 * <p/>
 * Saves found files in the cache directory, for later retrieval.
 */
public class CombiningImageConnector implements ImageConnector {

  private static final String TAG = CombiningImageConnector.class.getName();

  private ImageConnector prefilledConnector = new PrefilledImageConnector();
  private ImageConnector intranetImageDownloadConnector = new IntranetImageDownloadConnector();

  /**
   * This directory is within the cache directory (Context.getCacheDir())
   */
  private final static String cacheDirectory = "kiekeboek";
  private FileCache cache = new FileCache(cacheDirectory);

  @Override
  public void addPhoto(User user, Context context) {
    String cacheFilename = String.valueOf(user.getUserId());

    byte[] bytes = null;
    try {
      bytes = cache.get(cacheFilename, context);
    } catch (IOException e) {
      Log.w(TAG, "While trying to get from cache (will skip this). Message: " + e.getMessage());
    }
    if (bytes != null) {
      Log.d(TAG, "Cache contained file for user " + user.getUserId() + ": " + bytes.length + " bytes");
      user.setPhotoData(bytes);
      return;
    }

    prefilledConnector.addPhoto(user, context);
    if (user.getPhotoData() == null) {
      intranetImageDownloadConnector.addPhoto(user, context);
    }

    if (user.getPhotoData() != null) {
      try {
        cache.put(cacheFilename, user.getPhotoData(), context);
      } catch (IOException e) {
        Log.w(TAG, "Cannot create cache file, will skip this. Message: " + e.getMessage());
      }
    }
  }

}
