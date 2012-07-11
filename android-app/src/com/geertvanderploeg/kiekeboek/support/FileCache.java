package com.geertvanderploeg.kiekeboek.support;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.util.Log;


/**
 * Simple file system based cache.
 *
 */
public class FileCache {

  final String cacheDirectory;
  private static final String TAG = "FileCache";

  public FileCache(String cacheDirectory) {
    this.cacheDirectory = cacheDirectory;
  }

  public byte[] get(String filename, Context context) throws IOException {
    File kiekeboekCacheDirectory = null;
    kiekeboekCacheDirectory = getOrCreateCacheDirectory(context);
    File cachefile = new File(kiekeboekCacheDirectory, filename);
    final byte[] fileBytes;
    try {
      fileBytes = getFileBytes(cachefile);
      return fileBytes;
    } catch (IOException e) {
      Log.d(TAG, "While getting filebytes: " + e.getMessage());
    }
    return null;
  }

  public void put(String filename, byte[] data, Context context) throws IOException {
    File kiekeboekCacheDirectory = getOrCreateCacheDirectory(context);
    final File cachefile = new File(kiekeboekCacheDirectory, filename);
    final FileOutputStream fileOutputStream = new FileOutputStream(cachefile);
    try {
      fileOutputStream.write(data);
    } finally {
      fileOutputStream.close();
    }
  }

  private File getOrCreateCacheDirectory(Context context) throws IOException {
    File directory = new File(context.getCacheDir(), cacheDirectory);
    if (!directory.isDirectory()) {
      if (!directory.mkdir()) {
        throw new IOException("Cannot create cache directory " + directory.getAbsolutePath());
      }
    }
    return directory;
  }

  /*
  * From http://stackoverflow.com/a/9431216
  */
  private byte[] getFileBytes(File file) throws IOException {
    ByteArrayOutputStream ous = null;
    InputStream ios = null;
    try {
      byte[] buffer = new byte[4096];
      ous = new ByteArrayOutputStream();
      ios = new FileInputStream(file);
      int read = 0;
      while ((read = ios.read(buffer)) != -1)
        ous.write(buffer, 0, read);
    } finally {
      try {
        if (ous != null)
          ous.close();
      } catch (IOException e) {
        // swallow, since not that important
      }
      try {
        if (ios != null)
          ios.close();
      } catch (IOException e) {
        // swallow, since not that important
      }
    }
    return ous.toByteArray();
  }
}
