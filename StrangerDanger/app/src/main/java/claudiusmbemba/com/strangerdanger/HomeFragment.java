package claudiusmbemba.com.strangerdanger;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
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
        if (myLoc == null) {
            Toast.makeText(this.getActivity(),
                    "No Location yet.", Toast.LENGTH_LONG).show();
        }
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        locationClient.connect();
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
        Toast.makeText(this.getActivity(), "Connected", Toast.LENGTH_SHORT).show();

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
    @Override
    public void onClick(View v) {
        //api to notify ICEs
        Toast.makeText(this.getActivity(), "ICEs will be notified!", Toast.LENGTH_LONG).show();
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
