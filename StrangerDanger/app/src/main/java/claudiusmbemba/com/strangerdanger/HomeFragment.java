package claudiusmbemba.com.strangerdanger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    Button notify;
    Context context;
    MediaPlayer siren;
    MediaPlayer leedle;
    SharedPreferences prefs;

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
        leedle = MediaPlayer.create(context, R.raw.leedle);

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        notify = (Button) view.findViewById(R.id.danger_button);

        notify.setOnClickListener(this);
        notify.requestFocus();

        prefs = this.getActivity().getSharedPreferences("claudiusmbemba.com.strangerdanger", Context.MODE_PRIVATE);

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

        //determine if gps is enabled
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            gpsEnabledNotification();
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
    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        String msg = "Update location: " + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude());
//        Toast.makeText(this.getActivity(), msg, Toast.LENGTH_LONG).show();
        lastLocation = location;
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

    public Boolean checkGPSenabled(){
        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
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

//        Handler myHandler = new Handler();
//        myHandler.postDelayed(playSound, 3000);
        //delaySiren
        final Handler handler = new Handler();
        //Do something after 100ms
        if(siren.isPlaying()) {
            // Pause the music player
            siren.pause();
            // If it's not playing
        }else {

            Boolean smsPref = prefs.getBoolean("sms", false);
            Boolean emailPref = prefs.getBoolean("email", false);

            String phones = prefs.getString("phones", "");
            String recipients = prefs.getString("emails", "");

            if(smsPref){
                notifySMS("7406410248", getLat(), getLng());
            }
            if(emailPref){
                notifyEmial(recipients, getLat(), getLng());
            }
            siren.start();
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
    SmsManager smsManager = SmsManager.getDefault();

    public void notifySMS(String phone, String lat, String lng) {

        String geo ="http://maps.google.com/maps?daddr="+lat+","+lng;
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String[] separated = phone.split(",");
            for (String num : separated) {
                smsManager.sendTextMessage(num, null, "Help! I fear for my life!\n My location is \n "+ Uri.parse(geo)+ " \n Please send help!\n -Claudius \n\n- Sent from StrangerDanger App", null, null);
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
    public void notifyEmial(String recipients, String lat, String lng) {

        String email = prefs.getString("emailAddress", "");
        String pass = prefs.getString("emailPass", "");

        String geo ="http://maps.google.com/maps?daddr="+lat+","+lng;

        String title = "Help Me!";
        String msg = "Hey this is Claudius\n I'm currently at "+Uri.parse(geo)+" . I fear for my life." +
                "Please send help! \n" +
                "\n" +
                "- Sent from StrangerDanger App";
        if(email.matches("") || pass.matches("")){
            Toast.makeText(this.getActivity(), "Please enter email info in Prefernences", Toast.LENGTH_LONG).show();
        }else {
            try {
                GMailSender sender = new GMailSender(email, pass);
                sender.sendMail(title,
                        msg,
                        email,
                        recipients);
//            "mbembac@gmail.com,Matt.Faluotico@gmail.com,esh.derek@gmail.com,fenton.joshua4@gmail.com,trong.p.le.92@gmail.com,jlasuperman.new52@gmail.com"
                Toast.makeText(context,
                        "Emails sent!",
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context,
                        "Email failed, please verify email address",
                        Toast.LENGTH_LONG).show();
//            Log.e("SendMail", e.getMessage(), e);
            }
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
