/*
 * Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.geertvanderploeg.kiekeboek.client;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Represents a sample SyncAdapter user
 */
public class User {

  private final String userName;
  private final String firstName;
  private final String middleName;
  private final String lastName;
  private final String cellPhone;
  private final String officePhone;
  private final String homePhone;
  private final String email;
  private final boolean deleted;
  private final int userId;
  private byte[] photoData;
  private final String street;
  private final String postcode;
  private final String city;
  private Uri uri;

  public int getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getCellPhone() {
    return cellPhone;
  }

  public String getOfficePhone() {
    return officePhone;
  }

  public String getHomePhone() {
    return homePhone;
  }

  public String getEmail() {
    return email;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public User(String name, String firstName, String middleName, String lastName,
              String street, String postcode, String city, String cellPhone,
              String officePhone, String homePhone, String email,
              boolean deleted, Integer userId) {
    this.userName = name;
    this.firstName = firstName;
    this.middleName = middleName;
    this.lastName = lastName;

    this.street = street;
    this.postcode = postcode;
    this.city = city;

    this.email = normalizeEmail(email);
    this.deleted = deleted;
    this.userId = userId;
    this.cellPhone = normalizePhone(cellPhone);
    this.officePhone = normalizePhone(officePhone);
    this.homePhone = normalizePhone(homePhone);
  }


  public static String normalizeEmail(String email) {
    String ret = email;
    // no rules yet.
    return ret;
  }

  public static String normalizePhone(String p) {
    if (p == null) {
      return null;
    }
    String ret = p;
    ret = ret.replaceAll("[\\D]", ""); // strip non-numbers
    ret = ret.replaceFirst("^0([1-9])", "+31$1"); // 06 -> +316, 023 -> +3123
    ret = ret.replaceFirst("^([1-9])", "+3123$1"); // Assume local numbers to be Haarlem
    ret = ret.replaceFirst("^00", "+"); // International

    return ret;
  }

  /**
   * Creates and returns an instance of the user from the provided JSON data.
   *
   * @param user The JSONObject containing user data
   * @return user The new instance of Voiper user created from the JSON data.
   */
  public static User valueOf(JSONObject user) {
    try {
      final int userId = user.getInt("persoonid");
      final String firstName = user.has("roepnaam") ? user.getString("roepnaam") : null;
      final String middleName = user.has("tussenvoegsel") ? user.getString("tussenvoegsel") : null;
      final String lastName = user.has("achternaam") ? user.getString("achternaam") : null;
      final String street = user.has("straat") ? user.getString("straat") : null;
      final String postcode = user.has("postcode") ? user.getString("postcode") : null;
      final String city = user.has("plaats") ? user.getString("plaats") : null;
      final String userName = firstName + " " + lastName;
      final String cellPhone = user.has("mobiel") ? user.getString("mobiel") : null;
      final String officePhone = null;
      final String homePhone = user.has("telefoon") ? user.getString("telefoon") : null;
      final String email = user.has("emailadres") ? user.getString("emailadres") : null;
      final boolean deleted = false; // user.has("isDeleted") ? user.getBoolean("isDeleted") : false;
      return new User(userName, firstName, middleName, lastName, street, postcode, city, cellPhone,
          officePhone, homePhone, email, deleted, userId);
    } catch (final Exception ex) {
      Log.i("User", "Error parsing JSON user object" + ex.toString());

    }
    return null;
  }

  public String toString() {
    return new StringBuilder("User[")
        .append("id: ").append(userId).append(", ")
        .append("cellPhone: ").append(cellPhone).append(", ")
        .append("homePhone: ").append(homePhone).append(", ")
        .append("firstName: ").append(firstName).append(", ")
        .append("middleName: ").append(middleName).append(", ")
        .append("lastName: ").append(lastName).append(", ")
        .append("]")
        .toString();
  }

  public String getMiddleName() {
    return middleName;
  }


  public void setPhotoData(byte[] ray) {
    this.photoData = ray;
  }

  public byte[] getPhotoData() {
    return photoData;
  }

  /**
   * Returns null in case no bitmap is found
   *
   * @return
   */
  public Bitmap getPhotoBitmap() {
    if (photoData == null) {
      return null;
    } else {
      return BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
    }
  }

  public String getStreet() {
    return street;
  }

  public String getPostcode() {
    return postcode;
  }

  public String getCity() {
    return city;
  }

  public String getDisplayName() {
    return firstName + " " + (TextUtils.isEmpty(middleName) ? "" : middleName + " ") + lastName;
  }


  public Uri getUri() {
    return uri;
  }

  public void setUri(Uri uri) {
    this.uri = uri;
  }
}
