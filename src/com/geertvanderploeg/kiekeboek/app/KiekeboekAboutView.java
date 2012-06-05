package com.geertvanderploeg.kiekeboek.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.geertvanderploeg.kiekeboek.R;

public class KiekeboekAboutView extends Activity {

    private static final String TAG = "KiekeboekAboutView";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "oncreate");
        setContentView(R.layout.kiekeboek_about);
    }
}