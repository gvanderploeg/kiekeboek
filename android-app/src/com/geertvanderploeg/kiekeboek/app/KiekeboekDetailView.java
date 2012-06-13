package com.geertvanderploeg.kiekeboek.app;

import com.geertvanderploeg.kiekeboek.R;
import com.geertvanderploeg.kiekeboek.client.User;
import com.geertvanderploeg.kiekeboek.platform.KiekeboekColumns;

import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class KiekeboekDetailView extends Activity {
  private static final String TAG = "KiekeboekDetailView";

  private static UserService userService = new LocalStoreUserService();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.v(TAG, "oncreate");
    setContentView(R.layout.kiekeboek_detail);

    final Uri contactUri = getIntent().getData();
    // FIXME: this does not work anymore when coming from list view. Apparently we have to differentiate between the
    // two?
    final ContentProviderClient contentProviderClient = getContentResolver().acquireContentProviderClient(contactUri);
    int userId = 0;
    try {
      final Cursor cursor = contentProviderClient.query(contactUri, new String[]{KiekeboekColumns.DATA_PID}, null, null, null);
      if (cursor.moveToFirst()) {
        userId = cursor.getInt(0);
      }
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    Log.v(TAG, "Starting activity for userId " + userId);

    User u = userService.getUser(userId, this);
    if (u != null) {
      ImageView image = (ImageView) findViewById(R.id.personPicture);
      image.setImageBitmap(u.getPhotoBitmap());

      TextView name = (TextView) findViewById(R.id.personName);
      name.setText(u.getDisplayName());

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
