package com.geertvanderploeg.kiekeboek.app;

import com.geertvanderploeg.kiekeboek.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class HomeView extends Activity {

  private final HomeGridItem[] items = new HomeGridItem[]{
      new HomeGridItem(0, R.drawable.social_group, "Kleine groepen"),
      new HomeGridItem(1, R.drawable.location_map, "Kaart"),
      new HomeGridItem(2, R.drawable.collections_go_to_today, "Verjaardagen")
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.kiekeboek_home);

    GridView gridview = (GridView) findViewById(R.id.homegridview);
    gridview.setAdapter(new HomeGridImageAdapter(this));

    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        HomeGridItem item = items[position];
      }
    });
  }


  private class HomeGridImageAdapter extends BaseAdapter {
    private Context mContext;

    public HomeGridImageAdapter(Context c) {
      mContext = c;
    }

    public int getCount() {
      return items.length;
    }

    public Object getItem(int position) {
      return null;
    }

    public long getItemId(int position) {
      return 0;
    }


    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
      View grid;
      HomeGridItem item = items[position];

      if (convertView == null) {  // if it's not recycled, initialize some attributes
        grid = (View) getLayoutInflater().inflate(R.layout.kiekeboek_griditem, parent, false);
      } else {
        grid = convertView;
      }
      TextView itemView = (TextView) grid.findViewById(R.id.griditem);
      itemView.setBackgroundResource(item.drawable);
      itemView.setText(item.title);
      return grid;
    }

  }
}
