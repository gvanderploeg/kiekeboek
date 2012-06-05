package com.geertvanderploeg.kiekeboek.syncadapter.images;

import com.geertvanderploeg.kiekeboek.client.User;

import android.content.Context;

/**
 * Strategy for fetching images. Possible implementations:
 *  - preprovide images in the app itself
 *  - get from intranet
 *  - get from intranet and cache heavily
 */
public interface ImageConnector {
  /**
   * Add a photo to the given user
   * @param user the user
   * @param context Context
   */
  void addPhoto(User user, Context context);
}
