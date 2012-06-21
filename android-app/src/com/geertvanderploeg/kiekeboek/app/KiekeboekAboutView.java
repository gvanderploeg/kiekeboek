package com.geertvanderploeg.kiekeboek.app;

import com.geertvanderploeg.kiekeboek.R;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class KiekeboekAboutView extends Activity {

  private static final String TAG = "KiekeboekAboutView";

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.v(TAG, "oncreate");
    setContentView(R.layout.kiekeboek_about);
    try {
      final String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
      ((TextView) findViewById(R.id.aboutversion)).setText("Applicatieversie: " + versionName);
    } catch (PackageManager.NameNotFoundException ignored) {
    }
  }
}