package com.geertvanderploeg.kiekeboek.app;

import java.util.ArrayList;
import java.util.List;

import com.geertvanderploeg.kiekeboek.R;
import com.geertvanderploeg.kiekeboek.client.User;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class KiekeboekListView extends ListActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EfficientAdapter adapter = new EfficientAdapter(this);

      setContentView(R.layout.kiekeboek_list);

        setListAdapter(adapter);


      // Replaced with placeholder for list view.
      /*
        if (adapter.getCount() < 1) {
            Toast.makeText(this, R.string.list_empty, Toast.LENGTH_LONG).show();
        }
      */

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(view.getContext(), KiekeboekDetailView.class);
                intent.setAction(String.valueOf(((EfficientAdapter.ViewHolder) view.getTag()).id));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.kiekeboek_listview_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.sync_settings:
              startActivityForResult(new Intent(Settings.ACTION_SYNC_SETTINGS), 0);
                return true;
            case R.id.about:
                startActivity(new Intent(this, KiekeboekAboutView.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class PersonNamePicture {
        private final String name;
        private final Bitmap picture;
        private Uri uri;
        private int id;

      public PersonNamePicture(String name, Bitmap picture, Uri uri, int id) {
            this.name = name;
            this.picture = picture;
            this.uri = uri;
            this.id = id;
        }

        public Bitmap getPicture() {
            return picture;
        }

        public String getName() {
            return name;
        }

        public Uri getUri() {
            return uri;
        }

      public int getId() {
        return id;
      }
    }
    
    
    
    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private ArrayList<PersonNamePicture> pnps;

      UserService userService = new LocalStoreUserService();

        public EfficientAdapter(Context context) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);
            
            List<User> users = userService.getUsers(context);
            pnps = new ArrayList<PersonNamePicture>();
            for (User user : users) {
//                byte[] photoData = user.getPhotoData();
                pnps.add(new PersonNamePicture(user.getDisplayName(), null, user.getUri(), user.getUserId()));
            }
        }
        
        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         *
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return pnps.size();
        }
        
        /**
         * Since the data comes from an array, just returning the index is
         * sufficent to get at the data. If we were using a more complex data
         * structure, we would return whatever object represents one row in the
         * list.
         *
         * @see android.widget.ListAdapter#getItem(int)
         */
        public Object getItem(int position) {
            return position;
        }
        
        /**
         * Use the array index as a unique id.
         *
         * @see android.widget.ListAdapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }
        
        /**
         * Make a view to hold each row.
         *
         * @see android.widget.ListAdapter#getView(int, android.view.View,
         *      android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;
            
            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.kiekeboek_listitem, null);
                
                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }
            
            // Bind the data efficiently with the holder.
            holder.text.setText(pnps.get(position).getName());
            holder.icon.setImageBitmap(pnps.get(position).getPicture());
            holder.uri = pnps.get(position).getUri();
            holder.id = pnps.get(position).getId();


            return convertView;
        }


        public static class ViewHolder {
            TextView text;
            ImageView icon;
            Uri uri;
            int id;
        }
    }
}