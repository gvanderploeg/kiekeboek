package com.geertvanderploeg.kiekeboek;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.LiveFolders;
import android.text.TextUtils;
import android.util.Log;

public class KiekeboekContentProvider extends ContentProvider {

    private static final String TAG = "KiekeboekContentProvider";

    private static final String DATABASE_NAME = "kiekeboek.db";
    private static final int DATABASE_VERSION = 4;
    private static final String CONTACTS_TABLE_NAME = "contacts";

//    private static HashMap<String, String> sNotesProjectionMap;
    private static HashMap<String, String> sLiveFolderProjectionMap;

    private static final int CONTACTS = 1;
    private static final int CONTACT_ID = 2;
    private static final int LIVE_FOLDER_CONTACTS = 3;

    private static final UriMatcher sUriMatcher;

    private static final String AUTHORITY = "com.geertvanderploeg.kiekeboek";

    /**
     * table
     */
    public static final class KiekeboekColumns implements BaseColumns {
        // This class cannot be instantiated
        private KiekeboekColumns() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

      public static final String USERNAME = "userName";
      public static final String FIRSTNAME = "firstName";
      public static final String MIDDLENAME = "middleName";
      public static final String LASTNAME = "lastName";
      public static final String CELLPHONE = "cellPhone";
      public static final String OFFICEPHONE = "officePhone";
      public static final String HOMEPHONE = "homePhone";
      public static final String EMAIL = "email";
      public static final String DELETED = "deleted";
      public static final String USERID = "userId";
      public static final String PHOTODATA = "photoData";
      public static final String STREET = "street";
      public static final String POSTCODE = "postcode";
      public static final String CITY = "city";

        /**
         * The timestamp for when the row was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the row was last modified
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String MODIFIED_DATE = "modified";
    }



    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + CONTACTS_TABLE_NAME + " ("
                    + KiekeboekColumns._ID + " INTEGER PRIMARY KEY,"
                    + KiekeboekColumns.USERID + " INTEGER,"
                    + KiekeboekColumns.USERNAME + " TEXT,"
                    + KiekeboekColumns.FIRSTNAME + " TEXT,"
                    + KiekeboekColumns.MIDDLENAME + " TEXT,"
                    + KiekeboekColumns.LASTNAME + " TEXT,"
                    + KiekeboekColumns.CELLPHONE + " TEXT,"
                    + KiekeboekColumns.HOMEPHONE+ " TEXT,"
                    + KiekeboekColumns.OFFICEPHONE + " TEXT,"
                    + KiekeboekColumns.STREET + " TEXT,"
                    + KiekeboekColumns.CITY + " TEXT,"
                    + KiekeboekColumns.EMAIL + " TEXT,"
                    + KiekeboekColumns.POSTCODE + " TEXT,"
                    + KiekeboekColumns.PHOTODATA + " BLOB,"
                    + KiekeboekColumns.CREATED_DATE + " INTEGER,"
                    + KiekeboekColumns.MODIFIED_DATE + " INTEGER"
                    + ");");
          // TODO: add other fields
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE_NAME);
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;


    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(CONTACTS_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
        case CONTACTS:
//            qb.setProjectionMap(sNotesProjectionMap);
            break;

        case CONTACT_ID:
//            qb.setProjectionMap(sNotesProjectionMap);
            qb.appendWhere(KiekeboekColumns._ID + "=" + uri.getPathSegments().get(1));
            break;

        case LIVE_FOLDER_CONTACTS:
            qb.setProjectionMap(sLiveFolderProjectionMap);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = KiekeboekColumns.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case CONTACTS:
        case LIVE_FOLDER_CONTACTS:
            return KiekeboekColumns.CONTENT_TYPE;

        case CONTACT_ID:
            return KiekeboekColumns.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != CONTACTS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = System.currentTimeMillis();

        // Make sure that the fields are all set
        if (!values.containsKey(KiekeboekColumns.CREATED_DATE)) {
            values.put(KiekeboekColumns.CREATED_DATE, now);
        }

        if (!values.containsKey(KiekeboekColumns.MODIFIED_DATE)) {
            values.put(KiekeboekColumns.MODIFIED_DATE, now);
        }
/*
        if (values.containsKey(KiekeboekColumns.TITLE) == false) {
            Resources r = Resources.getSystem();
            values.put(KiekeboekColumns.TITLE, r.getString(android.R.string.untitled));
        }

        if (values.containsKey(KiekeboekColumns.NOTE) == false) {
            values.put(KiekeboekColumns.NOTE, "");
        }
*/
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(CONTACTS_TABLE_NAME, KiekeboekColumns.USERID, values);
        if (rowId > 0) {
            Uri contactUri = ContentUris.withAppendedId(KiekeboekColumns.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(contactUri, null);
            return contactUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case CONTACTS:
            count = db.delete(CONTACTS_TABLE_NAME, where, whereArgs);
            break;

        case CONTACT_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.delete(CONTACTS_TABLE_NAME, KiekeboekColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case CONTACTS:
            count = db.update(CONTACTS_TABLE_NAME, values, where, whereArgs);
            break;

        case CONTACT_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.update(CONTACTS_TABLE_NAME, values, KiekeboekColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "contacts", CONTACTS);
        sUriMatcher.addURI(AUTHORITY, "contacts/#", CONTACT_ID);
        sUriMatcher.addURI(AUTHORITY, "live_folders/contacts", LIVE_FOLDER_CONTACTS);

//        sNotesProjectionMap = new HashMap<String, String>();
//        sNotesProjectionMap.put(KiekeboekColumns._ID, KiekeboekColumns._ID);
//        sNotesProjectionMap.put(KiekeboekColumns.USERID, KiekeboekColumns.USERID);
//        sNotesProjectionMap.put(KiekeboekColumns.USERNAME, KiekeboekColumns.USERNAME);
//        sNotesProjectionMap.put(KiekeboekColumns.FIRSTNAME, KiekeboekColumns.FIRSTNAME);
//        sNotesProjectionMap.put(KiekeboekColumns.LASTNAME, KiekeboekColumns.LASTNAME);
//        sNotesProjectionMap.put(KiekeboekColumns.CREATED_DATE, KiekeboekColumns.CREATED_DATE);
//        sNotesProjectionMap.put(KiekeboekColumns.MODIFIED_DATE, KiekeboekColumns.MODIFIED_DATE);
//
        // Support for Live Folders.
        sLiveFolderProjectionMap = new HashMap<String, String>();
        sLiveFolderProjectionMap.put(LiveFolders._ID, KiekeboekColumns._ID + " AS " +
                LiveFolders._ID);
        sLiveFolderProjectionMap.put(LiveFolders.NAME, KiekeboekColumns.USERNAME + " AS " +
                LiveFolders.NAME);
        // Add more columns here for more robust Live Folders.
    }
}
