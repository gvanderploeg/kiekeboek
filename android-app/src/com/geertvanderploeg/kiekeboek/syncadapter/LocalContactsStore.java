package com.geertvanderploeg.kiekeboek.syncadapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.geertvanderploeg.kiekeboek.app.Notifications;
import com.geertvanderploeg.kiekeboek.client.User;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import static com.geertvanderploeg.kiekeboek.KiekeboekContentProvider.KiekeboekColumns;

public class LocalContactsStore {


  private static final String TAG = "LocalContactsStore";
  private static final String AUTHORITY = "com.geertvanderploeg.kiekeboek";
  private static String[] projection = null;
  private static String selection = null;

  public static List<User> getAllUsers(Context c) {
    ContentProviderClient contentProviderClient = c.getContentResolver().acquireContentProviderClient(AUTHORITY);

    Uri uri = Uri.withAppendedPath(KiekeboekColumns.CONTENT_URI, "contacts");
    String sort = KiekeboekColumns.LASTNAME;

    Cursor cursor = null;
    try {
      cursor = contentProviderClient.query(uri, projection, selection, null, sort);
      return getUsersFromCursor(cursor);
    } catch (RemoteException e) {
      Log.e(TAG, "while querying all users", e);
      Notifications.addNotification(c, "Error while querying local contacts store", e.getMessage());
    } finally {
      if (cursor != null) {
        cursor.close();
      }
      contentProviderClient.release();
    }
    return null;
  }

  private static List<User> getUsersFromCursor(Cursor cursor) {

    List<User> users = new ArrayList<User>();
    if (cursor != null && cursor.moveToFirst()) {
      int ciUsername = cursor.getColumnIndex(KiekeboekColumns.USERNAME);
      int ciFirstname = cursor.getColumnIndex(KiekeboekColumns.FIRSTNAME);
      int ciMiddlename = cursor.getColumnIndex(KiekeboekColumns.MIDDLENAME);
      int ciLastname = cursor.getColumnIndex(KiekeboekColumns.LASTNAME);
      int ciCellphone = cursor.getColumnIndex(KiekeboekColumns.CELLPHONE);
      int ciOfficephone = cursor.getColumnIndex(KiekeboekColumns.OFFICEPHONE);
      int ciHomephone = cursor.getColumnIndex(KiekeboekColumns.HOMEPHONE);
      int ciEmail = cursor.getColumnIndex(KiekeboekColumns.EMAIL);
      int ciDeleted = cursor.getColumnIndex(KiekeboekColumns.DELETED);
      int ciUserid = cursor.getColumnIndex(KiekeboekColumns.USERID);
      int ci_id = cursor.getColumnIndex(KiekeboekColumns._ID);
      int ciPhotodata = cursor.getColumnIndex(KiekeboekColumns.PHOTODATA);
      int ciStreet = cursor.getColumnIndex(KiekeboekColumns.STREET);
      int ciPostcode = cursor.getColumnIndex(KiekeboekColumns.POSTCODE);
      int ciCity = cursor.getColumnIndex(KiekeboekColumns.CITY);
      int ciArea = cursor.getColumnIndex(KiekeboekColumns.AREA);
      int ciHousegroup = cursor.getColumnIndex(KiekeboekColumns.HOUSEGROUP);
      int ciBirthdate = cursor.getColumnIndex(KiekeboekColumns.BIRTHDATE);

      Log.d(TAG, "column names: " + Arrays.asList(cursor.getColumnNames()).toString());
      Log.d(TAG, "Nr of rows queried: " + cursor.getCount());
      do {
        int _id = cursor.getInt(ci_id);
        int userid = cursor.getInt(ciUserid);
        String username = cursor.getString(ciUsername);
        String firstname = cursor.getString(ciFirstname);
        String middlename = cursor.getString(ciMiddlename);
        String lastname = cursor.getString(ciLastname);
        String cellphone = cursor.getString(ciCellphone);
        String officephone = cursor.getString(ciOfficephone);
        String homephone = cursor.getString(ciHomephone);
        String email = cursor.getString(ciEmail);
        byte[] photodata = cursor.getBlob(ciPhotodata);
        String street = cursor.getString(ciStreet);
        String postcode = cursor.getString(ciPostcode);
        String city = cursor.getString(ciCity);
        String area = cursor.getString(ciArea);
        String housegroup = cursor.getString(ciHousegroup);
        Date birthdate = new Date(cursor.getLong(ciBirthdate));
        User u = new User(username, firstname, middlename, lastname, street, postcode, city, cellphone,
            officephone, homephone, email, birthdate, area, housegroup, false, userid);
        u.setPhotoData(photodata);

        u.setUri(Uri.withAppendedPath(KiekeboekColumns.CONTENT_URI, "/contacts/" + _id));
        users.add(u);
      } while (cursor.moveToNext());
    }
    return users;
  }

  public static void syncContacts(Context ctx, List<User> users) {

    Log.d(TAG, String.format("About to sync %s contacts", users.size()));

    ContentResolver cr = ctx.getContentResolver();
    ContentProviderClient contentProviderClient = cr.acquireContentProviderClient(AUTHORITY);

    List<Integer> currentUserIds = getCurrentUserIds(ctx, contentProviderClient);
    for (User user : users) {
      if (user.isDeleted()) {
        Log.d(TAG, String.format("user %s is marked as deleted. Will remove from store.", user));
        deleteUser(ctx, contentProviderClient, user);
      } else if (currentUserIds.contains(user.getUserId())) {
        Log.d(TAG, String.format("user %s is stored already. Will update in store.", user));
        updateUser(ctx, contentProviderClient, user);
      } else {
        Log.d(TAG, String.format("user %s is not yet stored. Will add to  store.", user));
        insertUser(ctx, contentProviderClient, user);
      }
    }
    contentProviderClient.release();
    Log.d(TAG, "Store of contacts complete");
  }


  public static List<Integer> getCurrentUserIds(Context ctx, ContentProviderClient contentProviderClient) {
    List<Integer> userids = new ArrayList<Integer>();
    Cursor cursor = null;
    try {
      cursor = contentProviderClient.query(Uri.withAppendedPath(KiekeboekColumns.CONTENT_URI, "contacts"),
          new String[]{KiekeboekColumns.USERID}, null, null, null);
    } catch (RemoteException e) {
      Log.e(TAG, "while getting current users", e);
      Notifications.addNotification(ctx, "Cannot insert new entry", e.getMessage());
    }
    if (cursor != null && cursor.moveToFirst()) {
      int ciUserid = cursor.getColumnIndex(KiekeboekColumns.USERID);
      do {
        userids.add(cursor.getInt(ciUserid));
      } while (cursor.moveToNext());
      cursor.close();
    }
    Log.d(TAG, "current users in store: " + userids);
    return userids;
  }

  private static void updateUser(Context ctx, ContentProviderClient contentProviderClient, User user) {
    ContentValues values = buildContentValues(user);
    try {
      contentProviderClient.update(Uri.withAppendedPath(KiekeboekColumns.CONTENT_URI, "contacts"), values,
          KiekeboekColumns.USERID + "=?", new String[]{String.valueOf(user.getUserId())});
    } catch (RemoteException e) {
      Log.e(TAG, "while updating current user " + user, e);
      Notifications.addNotification(ctx, "Cannot insert new entry", e.getMessage());
    }
  }

  private static void insertUser(Context ctx, ContentProviderClient contentProviderClient, User user) {
    ContentValues values = buildContentValues(user);
    try {
      contentProviderClient.insert(Uri.withAppendedPath(KiekeboekColumns.CONTENT_URI, "contacts"), values);
    } catch (RemoteException e) {
      Log.e(TAG, "while inserting user " + user, e);
      Notifications.addNotification(ctx, "Cannot insert new entry", e.getMessage());
    }
  }

  private static void deleteUser(Context c, ContentProviderClient cpc, User user) {
    try {
      cpc.delete(Uri.withAppendedPath(KiekeboekColumns.CONTENT_URI, "contacts"), KiekeboekColumns.USERID + " =?",
          new String[]{String.valueOf(user.getUserId())});
    } catch (RemoteException e) {
      Log.e(TAG, "while deleting user " + user, e);
      Notifications.addNotification(c, String.format("Cannot delete user %s", user.getUserId()), e.getMessage());
    }

  }

  private static ContentValues buildContentValues(User user) {
    ContentValues values = new ContentValues();
    values.put(KiekeboekColumns.USERID, user.getUserId());
    values.put(KiekeboekColumns.USERNAME, user.getUserName());
    values.put(KiekeboekColumns.FIRSTNAME, user.getFirstName());
    values.put(KiekeboekColumns.MIDDLENAME, user.getMiddleName());
    values.put(KiekeboekColumns.LASTNAME, user.getLastName());
    values.put(KiekeboekColumns.CELLPHONE, user.getCellPhone());
    values.put(KiekeboekColumns.HOMEPHONE, user.getHomePhone());
    values.put(KiekeboekColumns.STREET, user.getStreet());
    values.put(KiekeboekColumns.CITY, user.getCity());
    values.put(KiekeboekColumns.EMAIL, user.getEmail());
    values.put(KiekeboekColumns.CELLPHONE, user.getCellPhone());
    values.put(KiekeboekColumns.POSTCODE, user.getPostcode());
    values.put(KiekeboekColumns.OFFICEPHONE, user.getOfficePhone());
    values.put(KiekeboekColumns.PHOTODATA, user.getPhotoData());
    values.put(KiekeboekColumns.AREA, user.getArea());
    values.put(KiekeboekColumns.HOUSEGROUP, user.getHousegroup());
    values.put(KiekeboekColumns.BIRTHDATE, user.getBirthdate().getTime());
    return values;
  }

  public static User getUser(Context c, int userId) {
    ContentProviderClient contentProviderClient = c.getContentResolver().acquireContentProviderClient(AUTHORITY);

    Uri uri = Uri.withAppendedPath(KiekeboekColumns.CONTENT_URI, "contacts");
    Cursor cursor = null;
    try {
      cursor = contentProviderClient.query(uri, projection, KiekeboekColumns.USERID + "=?",
          new String[] {String.valueOf(userId)}, null);
      final List<User> users = getUsersFromCursor(cursor);
      if (users.size() == 1) {
        return users.get(0);
      } else {
        return null;
      }
    } catch (RemoteException e) {
      Log.e(TAG, "while querying by userid " + userId, e);
      Notifications.addNotification(c, "Error while querying local contacts store", e.getMessage());
    } finally {
      if (cursor != null) {
        cursor.close();
      }
      contentProviderClient.release();
    }
    return null;
  }
}