package com.geertvanderploeg.kiekeboek.syncadapter.images;

import java.io.IOException;
import java.io.InputStream;

import com.geertvanderploeg.kiekeboek.client.User;

import android.content.Context;
import android.util.Log;

public class PrefilledImageConnector implements ImageConnector {

  private static final String TAG = "PrefilledImageConnector";
  private String imageDirectory = "kiekeboek_images";


  public void addPhoto(User user, Context context) {
    String filename = String.format("%s/%d.jpg", imageDirectory, user.getUserId());
    try {
      InputStream asset = context.getAssets().open(filename);
      byte[] ray = new byte[asset.available()];
      asset.read(ray);
      user.setPhotoData(ray);
    } catch (IOException e) {
      Log.i(TAG, String.format("Cannot load photo for user %s (photo file %s): %s", user.getUserName(), filename, e));
    }
  }

}
