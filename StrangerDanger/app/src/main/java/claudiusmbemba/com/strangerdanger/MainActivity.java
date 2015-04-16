package claudiusmbemba.com.strangerdanger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.telephony.SmsManager;
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

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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

//import com.google.android.gms.location.LocationClient;

public class MainActivity extends ActionBarActivity implements SensorEventListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener, GpsStatus.Listener {

    //LOCATION RELATED DEF CODE
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // A fast interval ceiling
    private static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * FAST_CEILING_IN_SECONDS;

    /*
     * Constants for handling location results
     */
    // Conversion from feet to meters
    private static final float METERS_PER_FEET = 0.3048f;

    // Conversion from kilometers to meters
    private static final int METERS_PER_KILOMETER = 1000;

    // Initial offset for calculating the map bounds
    private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;

    // Accuracy for calculating the map bounds
    private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;

    // Maximum results returned from a Parse query
    private static final int MAX_POST_SEARCH_RESULTS = 20;

    // Maximum post search radius for map in kilometers
    private static final int MAX_POST_SEARCH_DISTANCE = 100;

    private Location lastLocation;
    private Location currentLocation;

    // A request to connect to Location Services
    private LocationRequest locationRequest;

    // Stores the current instantiation of the location client in this object
    private GoogleApiClient locationClient;

    private LocationManager manager = null;


    //ACCELEROMETER RELATED DEF CODE
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 5000;

    //ACCELEROMETER RELATED METHODS
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
                    notifyAttack();
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
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("notify-attack"));

//        prefs.edit().putBoolean("alert_checked", true).apply();
    }
    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        //start bacground service
        startService(new Intent(this, BackgroundService.class));
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    protected class NavItem {
        String mTitle;
        String mSubtitle;
        int mIcon;

        public NavItem(String title, String subtitle, int icon) {
            mTitle = title;
            mSubtitle = subtitle;
            mIcon = icon;
        }
    }

    protected class DrawerListAdapter extends BaseAdapter {

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

    private Context context;
    private ListView mDrawerList;
    private RelativeLayout mDrawerPane;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private Fragment fragment = null;
    private HomeFragment home;
    private String rate;
    private String email;
    private String pass;


    private SharedPreferences prefs;
    private ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();

    //camera intents
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;

    private File getOutputPhotoFile() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getPackageName());
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
//                Log.e(TAG, "Failed to create storage directory.");
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
    public void onStart() {
        super.onStart();
        locationClient.connect();
        //Get an Analytics tracker to report app starts and uncaught exceptions etc.
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onStop() {
        //Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
        }
        locationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
//        Toast.makeText(this.getActivity(), "Connected", Toast.LENGTH_SHORT).show();
//        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//            // Get the current location
//            Toast.makeText(this.getActivity(), "Getting Location", Toast.LENGTH_LONG).show();
//        }
        currentLocation = getLocation();
        lastLocation = getLocation();
        startPeriodicUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onGpsStatusChanged(int event) {
        //empty code
    }

    /*
    * Get the current location
    */
    private Location getLocation() {
        // If Google Play Services is available
        if (servicesConnected()) {
            // Get the current location
            return LocationServices.FusedLocationApi.getLastLocation(locationClient);
        } else {
            return null;
        }
    }

    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    private void startPeriodicUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(locationClient, locationRequest, this);
//        locationClient.requestLocationUpdates(locationRequest, (com.google.android.gms.location.LocationListener) this);
    }

    private void stopPeriodicUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(locationClient,this);
//        locationClient.removeLocationUpdates((com.google.android.gms.location.LocationListener) this);
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
//            Log.d("Location Updates",
//                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                Toast.makeText(this, "Google PLay Services Not available", Toast.LENGTH_LONG).show();
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
//                errorFragment.show(this.getActivity().getFragmentManager(), "Location Updates");
            }
            return false;
        }
    }

    @Override
    //OnLocationChanged is never called until the gps is fixed.
    public void onLocationChanged(Location location) {

        currentLocation = location;

//        String msg = "Update location: " + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude());
//        Toast.makeText(this.getActivity(), msg, Toast.LENGTH_LONG).show();
        if(lastLocation != null){
            double lat1 = lastLocation.getLatitude();
            double lng1 = lastLocation.getLongitude();

            double lat2 = location.getLatitude();
            double lng2 = location.getLongitude();

            // lat1 and lng1 are the values of a previously stored location
            // if distance > 2 miles
            // if (distance(lat1, lng1, lat2, lng2) > 0.10) {
            if (distance(lat1, lng1, lat2, lng2) > 2) {
                //notify
                checkForLocationAlert();
                //update lastknow location to current
                lastLocation = location;
            }
        }

        if(prefs.getBoolean("alert_checked", false)){
            checkForLocationAlert();
        }
    }

    /** calculates the distance between two locations in MILES */
    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometer output

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist; // output distance, in MILES
    }

    public String getLat(){
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            return null;
        }else{
            Double loc = currentLocation.getLatitude();
            return loc.toString();
        }
    }

    public String getLng(){
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            return null;
        }else {
            Double loc = currentLocation.getLongitude();
            return loc.toString();
        }
    }

    public Boolean checkGPSEnabled(){
//        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager != null){
        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && manager != null){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
//                Log.d(ParseApplication.APPTAG, "An error occurred when connecting to location services.", e);
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Error: Connection failed: "+connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
//            showErrorDialog(connectionResult.getErrorCode());
        }

    }

    public void gpsEnabledNotification(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("For accurate reporting please enable GPS")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // More info: http://codetheory.in/difference-between-setdisplayhomeasupenabled-sethomebuttonenabled-and-setdisplayshowhomeenabled/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;

        prefs = this.getSharedPreferences("claudiusmbemba.com.strangerdanger", MODE_PRIVATE);

        email = prefs.getString("emailAddress", "");
        pass = prefs.getString("emailPass", "");

        //ACCELEROMETER
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        //LOCATION
        manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE );
        //create global location object
        locationRequest = LocationRequest.create();
        //set update interval
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        //use high accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //set interval ceiling to 1 min
        locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        //create new location client
        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

//        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
////        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
////            // Get the current location
////            Toast.makeText(this.getActivity(), "Please turn GPS on", Toast.LENGTH_LONG).show();
////        }
////        if (myLoc == null) {
////            Toast.makeText(this.getActivity(),
////                    "Trying to get your location.", Toast.LENGTH_LONG).show();
////        }
//        //set UserName
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
//        mNavItems.add(new NavItem("About", "About this app", R.drawable.menu));
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

                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
//                Log.d(TAG, "onDrawerClosed: " + getTitle());

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

        //G-ANALYTICS
        //Get a Tracker (should auto-report)
        ((SDApplication) getApplication()).getTracker(SDApplication.TrackerName.APP_TRACKER);

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
//            case "About":
//                fragment = new AboutFragment();
//                setTitle(mNavItems.get(position).mTitle);
//                break;
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

        if(checkGPSEnabled()){
            lat = getLat();
            lng = getLng();
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
            gpsEnabledNotification();
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
            if (servicesConnected()) {
                if(isConnected()){
                    //        if(true){
                    // call AsynTask to perform network operation on separate thread
//        new HttpAsyncTask().execute("https://maps.googleapis.com/maps/api/place/search/json?location=37.785835,-122.406418&rankby=distance&types=police&sensor=false&key=AIzaSyCU7rZMOqBsI87fpoZBSIxQPs0A9yLK6k0");
                    new HttpAsyncTask().execute("http://api.spotcrime.com/crimes.json?lat=" + getLat() + "&lon=" + getLng() + "&radius=0.050&callback=&key=MLC-restricted-key");

                    StatusBarNotify();

                }else{
                    Toast.makeText(this, "No network or wifi available.\nPlease enable.", Toast.LENGTH_LONG).show();
                }
                //0-10: Low crime
                //11-30: Med crime
                //31+: high crime
            } else {
                Toast.makeText(this, "Enable GPS for Location Alerts", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void StatusBarNotify() {
        int notifyID = 1;

        //create Status Bar notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Location Alert")
                        .setContentText(rate);

// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, context.getClass());
// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(context.getClass());
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(notifyID, mBuilder.build());
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

    //SMS related code
    private SmsManager smsManager = SmsManager.getDefault();

    public void notifySMS() {

        String phone = prefs.getString("phone_0", "") +","+ prefs.getString("phone_1", "") +","+ prefs.getString("phone_2", "") +","+ prefs.getString("phone_3", "") +","+ prefs.getString("phone_4", "");

        String geo ="http://maps.google.com/maps?daddr="+getLat()+","+getLng();
        try {
            String[] separated = phone.split(",");

            for (String num : separated) {
                smsManager.sendTextMessage(num, null, "Help! I fear for my life!\n My location is \n "+ Uri.parse(geo)+ " \n Send help!\n -" + prefs.getString("UserName", "")+"\n\n- Sent from StrangerDanger App", null, null);
            }
            Toast.makeText(context, "SMS Sent!",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context,
                    "SMS failed, please try again later!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void notifyAttack() {
        String phone = prefs.getString("phone_0", "") +","+ prefs.getString("phone_1", "") +","+ prefs.getString("phone_2", "") +","+ prefs.getString("phone_3", "") +","+ prefs.getString("phone_4", "");

        String geo ="http://maps.google.com/maps?daddr="+getLat()+","+getLng();
        try {
            String[] separated = phone.split(",");
            for (String num : separated) {
                smsManager.sendTextMessage(num, null, "HELP!!! I'm being attacked! Call 9-1-1.\n My location is \n "+ Uri.parse(geo)+ "\n -"+prefs.getString("UserName", "")+" \n\n- Sent from StrangerDanger App", null, null);
            }
            Toast.makeText(context, "SMS Sent!",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context,
                    "SMS failed, please try again later!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    //EMAIL related code
    public void notifyEmail() {

        if(email.matches("") || pass.matches("")){
            Toast.makeText(context, "Please enter email info in Preferences", Toast.LENGTH_LONG).show();
        }else {

            GMailSender sender = new GMailSender(email, pass);

            new SendMailTask().execute(sender);

        }
    }

    public void SendICENotification() {

        Boolean smsPref = prefs.getBoolean("sms", false);
        Boolean emailPref = prefs.getBoolean("email", false);

        String phones = prefs.getString("phone_0", "");
        String recipients = prefs.getString("email_0", "");


        if(smsPref){
            if(!phones.matches("")) {
                notifySMS();
            }else{
                Toast.makeText(this, "Please add First Contact", Toast.LENGTH_LONG).show();
            }
        }
        if(emailPref){
            if(!recipients.matches("")) {
                if(isConnected()) {
                    notifyEmail();
                }else{
                    Toast.makeText(context, "Cannot notify via email.\nNo network or wifi available.", Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(this, "Please add First Contact", Toast.LENGTH_LONG).show();
            }
        }
        if(!smsPref && !emailPref){
            Toast.makeText(this, "Enable Notification options in Preferences", Toast.LENGTH_LONG).show();
        }
    }

    // handler for received Intents for the "notify-attack" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            String message = intent.getStringExtra("notify");
//            Log.d("receiver", "Got message: " + message);
            if(message.matches("attack")){
                notifyAttack();
            }
        }
    };

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
//            Log.d("InputStream", e.getLocalizedMessage());
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
//            Log.d("RESULT", result);

            int crimes_count = 0;
            try {
                JSONObject json = new JSONObject(result);
                JSONArray crimes = json.getJSONArray("crimes");
                crimes_count = crimes.length();

            } catch (JSONException e) {
                e.printStackTrace();
            }

            String intro = "Our records indicate:\n";
            String msg;

            //0-10: Low crime
            //11-30: Med crime
            //31+: high crime
            if(crimes_count <= 10){
                rate = "A LOW CRIME RATE Area.\nBut stay alert out there!";
                msg = intro + rate;
                LocationAlertNotification(msg);
//                Toast.makeText(context, "Our records indicate:\n Low Crime Rate Area", Toast.LENGTH_LONG).show();
            }else if(crimes_count <=30){
                rate = "A MED CRIME RATE Area.\nEyes Peeled Please!";
                msg = intro + rate;
                LocationAlertNotification(msg);
//                Toast.makeText(context, "Our records indicate:\n Med Crime Rate Area", Toast.LENGTH_LONG).show();
            }else{
                rate = "A HIGH CRIME RATE Area.\nExercise Extreme Caution at night!";
                msg = intro + rate;
                LocationAlertNotification(msg);
//                Toast.makeText(context, "Our records indicate:\n High Crime Rate Area", Toast.LENGTH_LONG).show();
            }
        }
    }

    //SENDMAIL ASYNC
    private class SendMailTask extends AsyncTask<GMailSender, Void, Void> {
        private ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, "Sending Emails", "Please wait...", true, false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            Toast.makeText(context, "Emails sent!", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(GMailSender... params) {

            String recipients = prefs.getString("email_0", "") +","+ prefs.getString("email_1", "") +","+ prefs.getString("email_2", "") +","+ prefs.getString("email_3", "") +","+ prefs.getString("email_4", "");
            String geo = "http://maps.google.com/maps?daddr=" + getLat() + "," + getLng();

            String title = "I NEED HELP!!";
            String msg = "Hey this is "+prefs.getString("UserName", "")+"\nI'm currently at " + Uri.parse(geo) + " and I fear for my life. Please send help! \n" +
                    "\n" +
                    "- Sent from StrangerDanger App";
            try {

                // add attachements
                // params[0].addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                params[0].sendMail(title, msg, email, recipients);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }

    }
}
