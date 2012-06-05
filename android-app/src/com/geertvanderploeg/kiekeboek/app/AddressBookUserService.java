package com.geertvanderploeg.kiekeboek.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.geertvanderploeg.kiekeboek.Constants;
import com.geertvanderploeg.kiekeboek.client.User;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

public class AddressBookUserService implements UserService {
    private final static String TAG = "KiekeboekUserService";

    private final static String[] projection = new String[]{
            RawContacts._ID,
//            CommonDataKinds.StructuredName.GIVEN_NAME,
//            CommonDataKinds.StructuredName.MIDDLE_NAME,
//            CommonDataKinds.StructuredName.FAMILY_NAME
//            CommonDataKinds.StructuredName.DISPLAY_NAME
            "display_name",
            RawContacts.CONTACT_ID
    };


  @Override
    public List<User> getUsers(Context context) {
        ContentResolver cr = context.getContentResolver();
        Uri contactsUri = ContactsContract.Data.CONTENT_URI.buildUpon()
                .appendQueryParameter(RawContacts.ACCOUNT_NAME, "geertvanderploeg") // FIXME: inject from outside
                .appendQueryParameter(RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE)
                .build();
//
//        Uri contactsUri = RawContacts.CONTENT_URI.buildUpon()
//                .appendQueryParameter(RawContacts.ACCOUNT_NAME, "geertvanderploeg") // FIXME: inject from outside
//                .appendQueryParameter(RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE)
//                .build();

        Log.d(TAG, "Querying URI: " + contactsUri);
        
        // Make the query. 
        Cursor cursor = cr.query(contactsUri,
                null, // Which columns to return
                null,
                null,
                null
//                                String.format("%s ASC, %s ASC, %s ASC", // TODO: order by household instead of family name is probably more accurate.
//                                                CommonDataKinds.StructuredName.FAMILY_NAME,
//                                                CommonDataKinds.StructuredName.MIDDLE_NAME,
//                                                CommonDataKinds.StructuredName.GIVEN_NAME)
        );

        List<User> users = new ArrayList<User>();
        try {
            Log.d(TAG, "Retrieved contact data rows: " + cursor.getCount());
            if (cursor.moveToFirst()) {

                Log.d(TAG, "column names: " + Arrays.asList(cursor.getColumnNames()).toString());
                int ciRawContactId= cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID);
                int ciDataId= cursor.getColumnIndex(ContactsContract.Data._ID);
                int ciMimeType = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE);
                int ciGivenName = cursor.getColumnIndex(CommonDataKinds.StructuredName.GIVEN_NAME);
                int ciMiddleName = cursor.getColumnIndex(CommonDataKinds.StructuredName.MIDDLE_NAME);
                int ciFamilyName = cursor.getColumnIndex(CommonDataKinds.StructuredName.FAMILY_NAME);
                int ciPhoneType = cursor.getColumnIndex(CommonDataKinds.Phone.TYPE);


                User user = null;

                int dataId = 0;
                int rawContactIdPreviousRow = 0;
                String givenName = "", middleName = "", familyName = "";
                String homePhone = "", mobilePhone = "", officePhone = "";

                byte[] photoblob = new byte[0];
                do {
                    int rawContactId = cursor.getInt(ciRawContactId);

                    if (rawContactId != rawContactIdPreviousRow && rawContactIdPreviousRow != 0) {
                        // here the cursor already contains the row for the 'next' contact. So do not touch it anymore, only the cached values.

                        user = new User("", givenName, middleName, familyName, null, null, null, mobilePhone, officePhone, homePhone,  "", false, 0);
                        user.setUri(ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, dataId));
                        user.setPhotoData(photoblob);
                        users.add(user);
                        givenName = middleName = familyName = "";
                        homePhone = mobilePhone = officePhone = "";
                        photoblob = new byte[0];
                        dataId = 0;
                    }

                    dataId = cursor.getInt(ciDataId);
                    String mimeType = cursor.getString(ciMimeType);


                    if (mimeType.equals(CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
                        givenName = cursor.getString(ciGivenName);
                        middleName = cursor.getString(ciMiddleName);
                        familyName = cursor.getString(ciFamilyName);
//                        Log.d(TAG, "name: " + givenName + middleName + familyName);
                    } else if (mimeType.equals(CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                        // TODO
                    } else if (mimeType.equals(CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
                        photoblob = cursor.getBlob(cursor.getColumnIndex(CommonDataKinds.Photo.PHOTO));
                    } else if (mimeType.equals(CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                        switch (cursor.getInt(ciPhoneType)) {
                            case CommonDataKinds.Phone.TYPE_HOME:
                                homePhone = cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER));
                                break;
                            case CommonDataKinds.Phone.TYPE_MOBILE:
                                mobilePhone = cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER));
                                break;
                            default:
                                officePhone = cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER));
                                break;
                        }
                    }
/*
                    // debug only for first 10 contacts
                    if (rawContactId < 10) {
                        Log.d(TAG, "mime type: " + mimeType);
                        Log.d(TAG, "display name: " + cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)));
                        Log.d(TAG, "data_id: " + cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID)));
                        Log.d(TAG, "data1: " + cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1)));
                        Log.d(TAG, "data2: " + cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA2)));
                        Log.d(TAG, "data3: " + cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA3)));
                        Log.d(TAG, "data4: " + cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA4)));
                    }
*/
                    rawContactIdPreviousRow = rawContactId;
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return users;
    }


/*
    private static User getUserByRawContactId(ContentResolver cr, int i) {
        Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, i);
        Uri entityUri = Uri.withAppendedPath(rawContactUri, RawContacts.Entity.CONTENT_DIRECTORY);

        Log.d(TAG, "entity uri: " + entityUri);
        Cursor c = cr.query(entityUri, null, null, null, null);

        int userId = 0;
        String homePhone = "";
        String mobilePhone = "";
        String officePhone = "";
        String firstName = "";
        String middleName = "";
        String lastName = "";
        String email = "";
        String contactProfileEntityId = "0";
        byte[] photoData = new byte[0];
        try {
            while (c.moveToNext()) {
                if (!c.isNull(1)) {
                    String mimeType = c.getString(c.getColumnIndex(RawContacts.Entity.MIMETYPE));

                    if (mimeType.equals(CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                        switch (c.getInt(c.getColumnIndex(CommonDataKinds.Phone.TYPE))) {
                            case CommonDataKinds.Phone.TYPE_HOME:
                                homePhone = c.getString(c.getColumnIndex(CommonDataKinds.Phone.NUMBER));
                                break;
                            case CommonDataKinds.Phone.TYPE_MOBILE:
                                mobilePhone = c.getString(c.getColumnIndex(CommonDataKinds.Phone.NUMBER));
                                break;
                            default:
                                officePhone = c.getString(c.getColumnIndex(CommonDataKinds.Phone.NUMBER));
                                break;
                        }
                    } else if (mimeType.equals(CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
                        firstName = c.getString(c.getColumnIndex(CommonDataKinds.StructuredName.GIVEN_NAME));
                        middleName = c.getString(c.getColumnIndex(CommonDataKinds.StructuredName.MIDDLE_NAME));
                        lastName = c.getString(c.getColumnIndex(CommonDataKinds.StructuredName.FAMILY_NAME));
                    } else if (mimeType.equals(CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                        email = c.getString(c.getColumnIndex(CommonDataKinds.Email.DATA1));
                    } else if (mimeType.equals(Constants.KIEKEBOEK_MIMETYPE)) {
                        contactProfileEntityId = c.getString(c.getColumnIndex(RawContacts.Entity._ID));
                        userId = c.getInt(c.getColumnIndex(KiekeboekColumns.DATA_PID));
                    } else if (mimeType.equals(CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
                        photoData = c.getBlob(c.getColumnIndex(CommonDataKinds.Photo.PHOTO));
                    }
                }
            }
        } finally {
            c.close();
        }
        User user = new User("", firstName, middleName, lastName, null, null, null, mobilePhone, officePhone, homePhone, email, false, userId);
        user.setPhotoData(photoData);
        user.setUri(ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, Long.valueOf(contactProfileEntityId)));
        return user;
    }
    */
}