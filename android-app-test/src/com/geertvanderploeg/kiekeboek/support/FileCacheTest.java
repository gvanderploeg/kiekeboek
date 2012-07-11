package com.geertvanderploeg.kiekeboek.support;

import java.io.IOException;
import java.util.Arrays;

import android.test.AndroidTestCase;

public class FileCacheTest extends AndroidTestCase {

  private FileCache fc;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    fc = new FileCache("mycachedir");
  }

  public void testGetWhenBlank() throws IOException {
    assertNull(fc.get("foobar", getContext()));
  }

  public void testPutGet() throws IOException {
    final byte[] data = {'a', 'b'};
    fc.put("boobaa", data, getContext());
    final byte[] result = fc.get("boobaa", getContext());
    assertTrue(Arrays.equals(data, result));
  }

  /**
   * Put twice, see that a subsequent get gets the last one.
   * @throws IOException
   */
  public void testPutPutGet() throws IOException {
    byte[] data = {'a', 'b'};
    fc.put("boobaa", data, getContext());
    byte[] data2 = new byte[]{'a', 'b', 'c'};
    fc.put("boobaa", data2, getContext());
    final byte[] result = fc.get("boobaa", getContext());
    assertTrue(Arrays.equals(data2, result));
  }
}
