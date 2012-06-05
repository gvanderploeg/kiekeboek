package com.geertvanderploeg.kiekeboek.app;

import com.geertvanderploeg.kiekeboek.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class NotificationMessageView extends Activity {


  private static final String TAG = "NotificationMessageView";

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.v(TAG, "oncreate");

    // TODO: dismiss notification here?

    String data = getIntent().getDataString();
    setContentView(R.layout.kiekeboek_notificationmessage);
    ((TextView) findViewById(R.id.notificationTitle)).setText("Notification title (to be resolved)");
    ((TextView) findViewById(R.id.notificationText)).setText(data);
  }
}
