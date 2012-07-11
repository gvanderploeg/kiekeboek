package com.geertvanderploeg.kiekeboek.app;

import java.text.DateFormat;

import com.geertvanderploeg.kiekeboek.R;
import com.geertvanderploeg.kiekeboek.client.User;
import com.geertvanderploeg.kiekeboek.platform.KiekeboekColumns;

import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class KiekeboekDetailView extends Activity {
  private static final String TAG = "KiekeboekDetailView";

  private static UserService userService = new LocalStoreUserService();

  private static final DateFormat birthdateFormat = DateFormat.getDateInstance();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.v(TAG, "oncreate");
    setContentView(R.layout.kiekeboek_detail);

    final Uri contactUri = getIntent().getData();
    Log.v(TAG, "uri is: " + contactUri);

    int userId = 0;
    if (contactUri == null) { // coming from list view
      userId = Integer.parseInt(getIntent().getAction());
    } else { // coming from address book's contact details
      userId = getUserIdFromContact(contactUri);
    }

    Log.v(TAG, "Starting activity for userId " + userId);

    User u = userService.getUser(userId, this);
    if (u != null) {
      populateDetailView(u);
    } else {
      Log.v(TAG, "no data");
    }

    // click on 'alle personen'-button to get to list view.
    Button allPeopleButton = (Button) findViewById(R.id.alle_personen_button);
    allPeopleButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent myIntent = new Intent(view.getContext(), KiekeboekListView.class);
        startActivity(myIntent);
      }
    });
  }

  private void populateDetailView(User u) {
    TextView name = (TextView) findViewById(R.id.detailNameAndPicture);
    final Bitmap photoBitmap = u.getPhotoBitmap();
    if (photoBitmap != null) {
      final BitmapDrawable photo = new BitmapDrawable(photoBitmap);
      photo.setBounds(0, 0, photoBitmap.getWidth(), photoBitmap.getHeight());
      name.setCompoundDrawables(photo, null, null, null);
    }
    name.setText(u.getDisplayName());


    TextView birthdateView = (TextView) findViewById(R.id.detailBirthdate);
    birthdateView.setText(birthdateFormat.format(u.getBirthdate()));

    TextView addressView = (TextView) findViewById(R.id.detailAddress);
    addressView.setText(u.getStreet() + "\n" + u.getPostcode() + " " + u
        .getCity());


    TextView areaView = (TextView) findViewById(R.id.detailArea);
    areaView.setText("Wijk " + u.getArea() + "\nKleine groep " + u.getHousegroup());

  }

  private int getUserIdFromContact(Uri contactUri) {
    final ContentProviderClient contentProviderClient = getContentResolver().acquireContentProviderClient(contactUri);
    int userId = 0;
    try {
      final Cursor cursor = contentProviderClient.query(contactUri, new String[]{KiekeboekColumns.DATA_PID}, null, null, null);
      if (cursor.moveToFirst()) {
        return cursor.getInt(0);
      }
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.kiekeboek_detailview_menu, menu);
    return true;
  }

  private void logData(Cursor cursor) {
    StringBuilder strb = new StringBuilder();
    for (int i = 0; i < cursor.getColumnCount(); i++) {
      strb.append(i)
          .append(" : ")
          .append(cursor.getColumnName(i))
          .append(" : ")
          .append(cursor.getString(i))
          .append("\n");
    }
    Log.v(TAG, "Data gotten: " + strb.toString());
  }
}
