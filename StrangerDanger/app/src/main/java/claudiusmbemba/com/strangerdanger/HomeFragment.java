package claudiusmbemba.com.strangerdanger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

/**
 * Created by ClaudiusThaBeast on 4/10/15.
 */
public class HomeFragment extends Fragment implements
        View.OnClickListener,
        View.OnTouchListener,GooglePlayServicesClient.OnConnectionFailedListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener  {

    public HomeFragment() {
        //empty constructor
    }

    private Button notify;
    private Context context;
    private MediaPlayer siren;
    //    private MediaPlayer leedle;
    private SharedPreferences prefs;
    private ImageButton siren_btn;
    private String email;
    private String pass;

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
    private LocationClient locationClient;

    private LocationManager manager = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = this.getActivity();

        siren = MediaPlayer.create(context, R.raw.siren);
//        leedle = MediaPlayer.create(context, R.raw.leedle);

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        notify = (Button) view.findViewById(R.id.danger_button);

        notify.setOnClickListener(this);
        notify.requestFocus();

        siren_btn = (ImageButton) view.findViewById(R.id.siren_button);
        siren_btn.setOnClickListener(this);

        prefs = this.getActivity().getSharedPreferences("claudiusmbemba.com.strangerdanger", Context.MODE_PRIVATE);

        email = prefs.getString("emailAddress", "");
        pass = prefs.getString("emailPass", "");

        //set UserName
        TextView username = (TextView) this.getActivity().findViewById(R.id.userName);

        String uName = prefs.getString("UserName", "");
        if(uName.matches("")){
            username.setText("Add Name in Preferences");
        }else{
            username.setText(uName);
        }

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
        locationClient = new LocationClient(this.getActivity(), this, this);

        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
//        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//            // Get the current location
//            Toast.makeText(this.getActivity(), "Please turn GPS on", Toast.LENGTH_LONG).show();
//        }
        if (myLoc == null) {
//            Toast.makeText(this.getActivity(),
//                    "Trying to get your location.", Toast.LENGTH_LONG).show();
        }
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_home, container, false);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        locationClient.connect();
        Handler handler = new Handler();
        //determine if gps is enabled
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            gpsEnabledNotification();

            if(prefs.getBoolean("alert_checked", false)){
                ((MainActivity)getActivity()).checkForLocationAlert();
            }

        }
    }

    @Override
    public void onStop() {
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
        }

        locationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onConnected(Bundle bundle) {
//        Toast.makeText(this.getActivity(), "Connected", Toast.LENGTH_SHORT).show();
//        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//            // Get the current location
//            Toast.makeText(this.getActivity(), "Getting Location", Toast.LENGTH_LONG).show();
//        }
        currentLocation = getLocation();
        startPeriodicUpdates();
    }

    /*
    * Get the current location
    */
    private Location getLocation() {
        // If Google Play Services is available
        if (servicesConnected()) {
            // Get the current location
            return locationClient.getLastLocation();
        } else {
            return null;
        }
    }
    @Override
    public void onDisconnected() {
        Toast.makeText(this.getActivity(), "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    private void startPeriodicUpdates() {
        locationClient.requestLocationUpdates(locationRequest, (com.google.android.gms.location.LocationListener) this);
    }

    private void stopPeriodicUpdates() {
        locationClient.removeLocationUpdates((com.google.android.gms.location.LocationListener) this);
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this.getActivity());
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this.getActivity(),
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                Toast.makeText(this.getActivity(), "Google PLay Services Not available", Toast.LENGTH_LONG).show();
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

//    public void checkForLocationAlert() {
//
//        if(checkGPSenabled()){
////        if(true){
//            // call AsynTask to perform network operation on separate thread
////        new HttpAsyncTask().execute("https://maps.googleapis.com/maps/api/place/search/json?location=37.785835,-122.406418&rankby=distance&types=police&sensor=false&key=AIzaSyCU7rZMOqBsI87fpoZBSIxQPs0A9yLK6k0");
//            new HttpAsyncTask().execute("http://api.spotcrime.com/crimes.json?lat="+getLat()+"&lon="+getLng()+"&radius=0.050&callback=&key=MLC-restricted-key");
//            //0-10: Low crime
//            //11-30: Med crime
//            //31+: high crime
//        }else{
//            Toast.makeText(this.getActivity(), "Enable GPS for Location Alerts", Toast.LENGTH_LONG).show();
//        }
//    }

    @Override
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
            if (distance(lat1, lng1, lat2, lng2) > .1) { // if distance > 2 miles we take locations as equal
                //notify
                ((MainActivity)getActivity()).checkForLocationAlert();
                //update lastknow location to current
                lastLocation = location;
            }
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

//    public void LocationAlertNotification(String msg){
//
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
//
//        builder.setMessage(msg)
//                .setCancelable(true)
//                .setPositiveButton("I Got It", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .setTitle("Location Alert");
//        final AlertDialog alert = builder.create();
//        alert.show();
//    }

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

    public Boolean checkGPSenabled(){
//        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager != null){
        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && manager != null){
            return true;
        }else{
            return false;
        }
    }
//    @Override
//    public void onActivityResult(
//            int requestCode, int resultCode, Intent data) {
//        // Decide what to do based on the original request code
//        switch (requestCode) {
//
//            // If the request code matches the code sent in onConnectionFailed
//            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
//
//                switch (resultCode) {
//                    // If Google Play services resolved the problem
//                    case Activity.RESULT_OK:
//
//                        if (ParseApplication.APPDEBUG) {
//                            // Log the result
//                            Log.d(ParseApplication.APPTAG, "Connected to Google Play services");
//                        }
//
//                        break;
//
//                    // If any other result was returned by Google Play services
//                    default:
//                        if (ParseApplication.APPDEBUG) {
//                            // Log the result
//                            Log.d(ParseApplication.APPTAG, "Could not connect to Google Play services");
//                        }
//                        break;
//                }
//
//                // If any other request code was received
//            default:
//                if (ParseApplication.APPDEBUG) {
//                    // Report that this Activity received an unknown requestCode
//                    Log.d(ParseApplication.APPTAG, "Unknown request code received for the activity");
//                }
//                break;
//        }
//    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this.getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
//                Log.d(ParseApplication.APPTAG, "An error occurred when connecting to location services.", e);
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this.getActivity(), "Error: Connection failed: "+connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
//            showErrorDialog(connectionResult.getErrorCode());
        }

    }

    public void gpsEnabledNotification(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

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
    public void onClick(View v) {
        //api to notify ICEs
//        Toast.makeText(this.getActivity(), "ICEs will be notified!", Toast.LENGTH_LONG).show();

        if(v.getId() == R.id.siren_button){
            if(siren.isPlaying()) {
                // Pause the music player
                siren.pause();
                // If it's not playing
            }else {
                siren.start();
                SendICENotification();
                //delayed start
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    // Resume the music player
//                    siren.start();
//                }
//            }, 3000);
//        }
            }
        }
        SendICENotification();

    }

    private void SendICENotification() {
        //        Handler myHandler = new Handler();
//        myHandler.postDelayed(playSound, 3000);
        //delaySiren
        final Handler handler = new Handler();
        //Do something after 100ms

        Boolean smsPref = prefs.getBoolean("sms", false);
        Boolean emailPref = prefs.getBoolean("email", false);

        String phones = prefs.getString("phone_0", "") +","+ prefs.getString("phone_1", "") +","+ prefs.getString("phone_2", "") +","+ prefs.getString("phone_3", "") +","+ prefs.getString("phone_4", "");
        String recipients = prefs.getString("email_0", "") +","+ prefs.getString("email_1", "") +","+ prefs.getString("email_2", "") +","+ prefs.getString("email_3", "") +","+ prefs.getString("email_4", "");

        if(smsPref){
            if(!phones.matches("")) {
//                notifySMS(phones, getLat(), getLng());
                notifySMS("7406410248", getLat(), getLng());
            }
        }
        if(emailPref){
            if(!recipients.matches("")) {
                if(((MainActivity)getActivity()).isConnected()) {
//            notifyEmail(recipients, getLat(), getLng());
                    notifyEmail("mbemba.1@osu.edu", getLat(), getLng());
                }else{
                    Toast.makeText(this.getActivity(), "Cannot notify via email.\nNo network or wifi available.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //LEEDLE SOUND
    @Override
    public boolean onTouch(View v, MotionEvent event) {
//
//        //delaySiren
//        final Handler handler = new Handler();
//
//        switch (event.getAction() & MotionEvent.ACTION_MASK) {
//            case MotionEvent.ACTION_DOWN:
//                v.setPressed(true);
//                // Start action ...
//                if(leedle.isPlaying()) {
//                    // Pause the music player
//                    leedle.stop();
//                    // If it's not playing
//                }else {
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            // Resume the music player
//                            leedle.start();
//                        }
//                    }, 3000);
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_OUTSIDE:
//            case MotionEvent.ACTION_CANCEL:
//                v.setPressed(false);
//                // Stop action ...
//                if(leedle.isPlaying()) {
//                    // Pause the music player
//                    leedle.stop();
//                    // If it's not playing
//                }
//                break;
//            case MotionEvent.ACTION_POINTER_DOWN:
//                break;
//            case MotionEvent.ACTION_POINTER_UP:
//                break;
//            case MotionEvent.ACTION_MOVE:
//                break;
//        }
//
        return true;
    }

    //delayed mediaplayer
//    private Runnable playSound = new Runnable()
//    {
//        final MediaPlayer siren = MediaPlayer.create(getActivity(), R.raw.siren);
//
//        @Override
//        public void run()
//        {
//            //Change state here
//            // If the music is playing
//            if(siren.isPlaying()) {
//                // Pause the music player
//                siren.stop();
//                // If it's not playing
//            }else {
//                // Resume the music player
//                siren.start();
//            }
//        }
//    };

    //SMS related code
    private SmsManager smsManager = SmsManager.getDefault();

    public void notifySMS(String phone, String lat, String lng) {

        String geo ="http://maps.google.com/maps?daddr="+lat+","+lng;
        try {
//            SmsManager smsManager = SmsManager.getDefault();
            String[] separated = phone.split(",");

            //log phones
            Log.d("TEST PHONES:", String.valueOf(separated));

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

    public void notifyAttack(String phone, String lat, String lng) {

        String geo ="http://maps.google.com/maps?daddr="+lat+","+lng;
        try {
//            SmsManager smsManager = SmsManager.getDefault();
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
    public void notifyEmail(String recipients, String lat, String lng) {

        if(email.matches("") || pass.matches("")){
            Toast.makeText(this.getActivity(), "Please enter email info in Preferences", Toast.LENGTH_LONG).show();
        }else {

            GMailSender sender = new GMailSender(email, pass);
//            GMailSender sender = new GMailSender("mbembac@gmail.com", "C0nfirmoceanhornadmin!");

            new SendMailTask().execute(sender);

////            "mbembac@gmail.com,Matt.Faluotico@gmail.com,esh.derek@gmail.com,fenton.joshua4@gmail.com,trong.p.le.92@gmail.com,jlasuperman.new52@gmail.com"

        }
    }


//    //HTTP GET CALL
//    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
//        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
//        String line = "";
//        String result = "";
//        while((line = bufferedReader.readLine()) != null)
//            result += line;
//
//        inputStream.close();
//        return result;
//
//    }
//
//    public static String GET(String url){
//        InputStream inputStream = null;
//        String result = "";
//        try {
//
//            // create HttpClient
//            HttpClient httpclient = new DefaultHttpClient();
//
//            // make GET request to the given URL
//            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
//
//            // receive response as inputStream
//            inputStream = httpResponse.getEntity().getContent();
//
//            // convert inputstream to string
//            if(inputStream != null)
//                result = convertInputStreamToString(inputStream);
//            else
//                result = "Did not work!";
//
//        } catch (Exception e) {
//            Log.d("InputStream", e.getLocalizedMessage());
//        }
//
//        return result;
//    }
//    //MAKE GET REQUESTS
//    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
//        @Override
//        protected String doInBackground(String... urls) {
//
//            return GET(urls[0]);
//        }
//        // onPostExecute displays the results of the AsyncTask.
//        @Override
//        protected void onPostExecute(String result) {
////            try {
////                JSONObject json = new JSONObject(result);
////            } catch (JSONException e) {
////                e.printStackTrace();
////            }
//
////            Toast.makeText(context, "Received!", Toast.LENGTH_LONG).show();
//            Log.d("RESULT", result);
//
//            int crimes_count = 0;
//            try {
//                JSONObject json = new JSONObject(result);
//                JSONArray crimes = json.getJSONArray("crimes");
//                crimes_count = crimes.length();
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            //0-10: Low crime
//            //11-30: Med crime
//            //31+: high crime
//            if(crimes_count <= 10){
//                LocationAlertNotification("Our records indicate:\nA LOW CRIME RATE Area.\nBut stay alert out there!");
////                Toast.makeText(context, "Our records indicate:\n Low Crime Rate Area", Toast.LENGTH_LONG).show();
//            }else if(crimes_count <=30){
//                LocationAlertNotification("Our records indicate:\nA MED CRIME RATE Area.\nEyes Peeled Please!");
////                Toast.makeText(context, "Our records indicate:\n Med Crime Rate Area", Toast.LENGTH_LONG).show();
//            }else{
//                LocationAlertNotification("Our records indicate:\nA HIGH CRIME RATE Area.\nExercise Extreme Caution at night!");
////                Toast.makeText(context, "Our records indicate:\n High Crime Rate Area", Toast.LENGTH_LONG).show();
//            }
////            // Create a Uri from an intent string. Use the result to create an Intent.
//////            Uri gmmIntentUri = Uri.parse("google.streetview:cbll="+slat+","+slng);
//////            Uri gmmIntentUri = Uri.parse("geo:"+slat+","+slng);
////
////            Uri gmmIntentUri = Uri.parse("geo:37.783762,-122.412915?q=police");
////
////            // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
////            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
////            // Make the Intent explicit by setting the Google Maps package
////            mapIntent.setPackage("com.google.android.apps.maps");
////
////            // Attempt to start an activity that can handle the Intent
////            startActivity(mapIntent);
//
//        }
//    }

    //SENDMAIL ASYNC
    private class SendMailTask extends AsyncTask<GMailSender, Void, Void> {
        private ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "Sending Emails", "Please wait...", true, false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            Toast.makeText(context, "Emails sent!", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(GMailSender... params) {

            String recipients = prefs.getString("emails", "");
            String geo = "http://maps.google.com/maps?daddr=" + getLat() + "," + getLng();

            String title = "Help Me!";
            String msg = "Hey this is Claudius\n I'm currently at " + Uri.parse(geo) + " . I fear for my life." +
                    "Please send help! \n" +
                    "\n" +
                    "- Sent from StrangerDanger App";
            try {

                // add attachements
                // params[0].addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                params[0].sendMail(title, msg, "mbembac@gmail.com", "mbemba.1@osu.edu");
//                params[0].sendMail(title, msg, email, recipients);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends android.support.v4.app.DialogFragment {
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
