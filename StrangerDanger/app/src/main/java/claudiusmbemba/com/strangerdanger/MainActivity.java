package claudiusmbemba.com.strangerdanger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends ActionBarActivity implements SensorEventListener {

    //ACCELEROMETER RELATED CODE
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 2000;

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
//                    home.notifyAttack(prefs.getString("phones", ""),home.getLat(),home.getLng());
                    home.notifyAttack("7406410248",home.getLat(),home.getLng());
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
//        prefs.edit().putBoolean("alert_checked", true).apply();
    }
    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    class NavItem {
        String mTitle;
        String mSubtitle;
        int mIcon;

        public NavItem(String title, String subtitle, int icon) {
            mTitle = title;
            mSubtitle = subtitle;
            mIcon = icon;
        }
    }

    class DrawerListAdapter extends BaseAdapter {

        Context mContext;
        ArrayList<NavItem> mNavItems;

        public DrawerListAdapter(Context context, ArrayList<NavItem> navItems) {
            mContext = context;
            mNavItems = navItems;
        }

        @Override
        public int getCount() {
            return mNavItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mNavItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.drawer_item, null);
            }
            else {
                view = convertView;
            }

            TextView titleView = (TextView) view.findViewById(R.id.title);
            TextView subtitleView = (TextView) view.findViewById(R.id.subTitle);
            ImageView iconView = (ImageView) view.findViewById(R.id.icon);

            titleView.setText( mNavItems.get(position).mTitle );
            subtitleView.setText( mNavItems.get(position).mSubtitle );
            iconView.setImageResource(mNavItems.get(position).mIcon);

            return view;
        }
    }

    private static String TAG = MainActivity.class.getSimpleName();

    Context context;
    ListView mDrawerList;
    RelativeLayout mDrawerPane;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private Fragment fragment = null;
    private HomeFragment home;

    SharedPreferences prefs;
    ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();

    //camera intents
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;

    private File getOutputPhotoFile() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getPackageName());
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e(TAG, "Failed to create storage directory.");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.US).format(new java.util.Date());
        return new File(directory.getPath() + File.separator + "IMG_"
                + timeStamp + ".jpg");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri photoUri = null;
                if (data == null) {
                    // A known bug here! The image should have saved in fileUri
                    Toast.makeText(this, "Image saved successfully",
                            Toast.LENGTH_LONG).show();
//                    photoUri = fileUri;
                } else {
//                    photoUri = data.getData();
                    Toast.makeText(this, "Image saved successfully in: " + data.getData(),
                            Toast.LENGTH_LONG).show();
                }
                //showPhoto(photoUri);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Callout for image capture failed!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

//    private void showPhoto(Uri photoUri) {
//        File imageFile = new File(photoUri);
//        if (imageFile.exists()){
//            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
//            BitmapDrawable drawable = new BitmapDrawable(this.getResources(), bitmap);
//            photoImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
//            photoImage.setImageDrawable(drawable);
//        }
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // More info: http://codetheory.in/difference-between-setdisplayhomeasupenabled-sethomebuttonenabled-and-setdisplayshowhomeenabled/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;

        prefs = this.getSharedPreferences("claudiusmbemba.com.strangerdanger", MODE_PRIVATE);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        //set UserName
        TextView username = (TextView) this.findViewById(R.id.userName);

        String uName = prefs.getString("UserName", "");
        if(uName.matches("")){
            username.setText("Add Name in Preferences");
        }else{
            username.setText(uName);
        }

        mNavItems.add(new NavItem("Home", "Notify ICEs", R.drawable.home));
        mNavItems.add(new NavItem("Edit ICE Contacts", "Edit your ICE Contacts", R.drawable.contact));
        mNavItems.add(new NavItem("Police", "Contact nearest police", R.drawable.police));
        mNavItems.add(new NavItem("Camera", "Take photo evidence", R.drawable.camera));
        mNavItems.add(new NavItem("Preferences", "Change your preferences", R.drawable.settings));
        mNavItems.add(new NavItem("About", "About this app", R.drawable.menu));
        mNavItems.add(new NavItem("Logout", "Sign out", R.drawable.logout));

        // DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        // Populate the Navigtion Drawer with options
        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        mDrawerList = (ListView) findViewById(R.id.navList);

        DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);
        mDrawerList.setAdapter(adapter);

        // Drawer Item click listeners
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItemFromDrawer(position);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if(prefs.getBoolean("alert_checked", false)){
                    checkForLocationAlert();
                }

                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.d(TAG, "onDrawerClosed: " + getTitle());

                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        //set home fragment
//        Fragment fragment = new HomeFragment();
        home = new HomeFragment();

        FragmentManager fragmentManager = getFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.mainContent, home)
                .commit();
        setTitle("Home");

        if(prefs.getBoolean("alerts", false)) {
            prefs.edit().putBoolean("alert_checked", true).apply();
        }
    }


    /*
    * Called when a particular item from the navigation drawer
    * is selected.
    */
    private void selectItemFromDrawer(int position) {

        NavItem item = mNavItems.get(position);

        FragmentManager fragmentManager = getFragmentManager();

        switch (item.mTitle) {
            case "Home":
                fragment = new HomeFragment();
                setTitle(mNavItems.get(position).mTitle);
                break;
            case "About":
                fragment = new AboutFragment();
                setTitle(mNavItems.get(position).mTitle);
                break;
            case "Preferences":
                fragment = new PreferencesFragment();
                setTitle(mNavItems.get(position).mTitle);
                break;
            case "Police":
                //call Google place API
                getNearByPolice();
                break;
            case "Camera": //intent call to camera
                //camera: create Intent to take a picture and return control to the calling application
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = Uri.fromFile(getOutputPhotoFile());// create a file to save the image
                // fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
                // start the image capture Intent
                startActivityForResult(camera_intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                break;
            case "Edit ICE Contacts":
                fragment = new AddContactFragment();
                setTitle(mNavItems.get(position).mTitle);
                break;
            case "Logout":
                //launch Phone home intent
                Intent logout_intent = new Intent(Intent.ACTION_MAIN);
                logout_intent.addCategory(Intent.CATEGORY_HOME);
                logout_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(logout_intent);
                break;
            default:

        }

        if(fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.mainContent, fragment)
                    .commit();
            mDrawerList.setItemChecked(position, true);
        }

        // Close the drawer
        mDrawerLayout.closeDrawer(mDrawerPane);
    }

    private void getNearByPolice() {

        //getLocation

        String lat = null, lng = null;

        if(home.checkGPSenabled()){
            lat = home.getLat();
            lng = home.getLng();
//            Log.d("LAT", lat.toString());
//            Log.d("LONG", lng.toString());
            //push intent
            Uri gmmIntentUri = Uri.parse("geo:"+lat+","+lng+"?q=police");

            // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            // Make the Intent explicit by setting the Google Maps package
            mapIntent.setPackage("com.google.android.apps.maps");

            // Attempt to start an activity that can handle the Intent
            startActivity(mapIntent);
        }else{
//            Toast.makeText(this, "Could not get location. Please check GPS is on", Toast.LENGTH_LONG).show();
            home.gpsEnabledNotification();
        }
    }


    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }else {
            return false;
        }
    }


    // Called when invalidateOptionsMenu() is invoked
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerPane);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle
        // If it returns true, then it has handled
        // the nav drawer indicator touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle your other action bar items...
        //noinspection SimplifiableIfStatement
        int id = item.getItemId();

//        Menu menu = (Menu) item.getMenuInfo();
        if (id == R.id.action_settings) {
            return true;
//            boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerPane);
//            menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
//            return super.onPrepareOptionsMenu(menu);
        }

        return super.onOptionsItemSelected(item);
    }

    public void checkForLocationAlert() {
        if(prefs.getBoolean("alerts", false)) {
            if (home.checkGPSenabled()) {
//        if(true){
                // call AsynTask to perform network operation on separate thread
//        new HttpAsyncTask().execute("https://maps.googleapis.com/maps/api/place/search/json?location=37.785835,-122.406418&rankby=distance&types=police&sensor=false&key=AIzaSyCU7rZMOqBsI87fpoZBSIxQPs0A9yLK6k0");
                new HttpAsyncTask().execute("http://api.spotcrime.com/crimes.json?lat=" + home.getLat() + "&lon=" + home.getLng() + "&radius=0.050&callback=&key=MLC-restricted-key");
                //0-10: Low crime
                //11-30: Med crime
                //31+: high crime
            } else {
                Toast.makeText(this, "Enable GPS for Location Alerts", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void LocationAlertNotification(String msg){

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(msg)
                .setCancelable(true)
                .setPositiveButton("I Got It", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setTitle("Location Alert");
        final AlertDialog alert = builder.create();
        prefs.edit().putBoolean("alert_checked", false).apply();
        alert.show();
    }

    //HTTP GET CALL
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }
    //MAKE GET REQUESTS
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
//            try {
//                JSONObject json = new JSONObject(result);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

//            Toast.makeText(context, "Received!", Toast.LENGTH_LONG).show();
            Log.d("RESULT", result);

            int crimes_count = 0;
            try {
                JSONObject json = new JSONObject(result);
                JSONArray crimes = json.getJSONArray("crimes");
                crimes_count = crimes.length();

            } catch (JSONException e) {
                e.printStackTrace();
            }

            //0-10: Low crime
            //11-30: Med crime
            //31+: high crime
            if(crimes_count <= 10){
                LocationAlertNotification("Our records indicate:\nA LOW CRIME RATE Area.\nBut stay alert out there!");
//                Toast.makeText(context, "Our records indicate:\n Low Crime Rate Area", Toast.LENGTH_LONG).show();
            }else if(crimes_count <=30){
                LocationAlertNotification("Our records indicate:\nA MED CRIME RATE Area.\nEyes Peeled Please!");
//                Toast.makeText(context, "Our records indicate:\n Med Crime Rate Area", Toast.LENGTH_LONG).show();
            }else{
                LocationAlertNotification("Our records indicate:\nA HIGH CRIME RATE Area.\nExercise Extreme Caution at night!");
//                Toast.makeText(context, "Our records indicate:\n High Crime Rate Area", Toast.LENGTH_LONG).show();
            }
//            // Create a Uri from an intent string. Use the result to create an Intent.
////            Uri gmmIntentUri = Uri.parse("google.streetview:cbll="+slat+","+slng);
////            Uri gmmIntentUri = Uri.parse("geo:"+slat+","+slng);
//
//            Uri gmmIntentUri = Uri.parse("geo:37.783762,-122.412915?q=police");
//
//            // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
//            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//            // Make the Intent explicit by setting the Google Maps package
//            mapIntent.setPackage("com.google.android.apps.maps");
//
//            // Attempt to start an activity that can handle the Intent
//            startActivity(mapIntent);

        }
    }


}
