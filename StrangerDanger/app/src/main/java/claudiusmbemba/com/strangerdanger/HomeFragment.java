package claudiusmbemba.com.strangerdanger;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ClaudiusThaBeast on 4/10/15.
 */
public class HomeFragment extends Fragment implements
        View.OnClickListener,
        View.OnTouchListener {

    public HomeFragment() {
        //empty constructor
    }

    private Button notify;
    private Context context;
    private MediaPlayer siren;
    //    private MediaPlayer leedle;
    private SharedPreferences prefs;
    private ImageButton siren_btn;


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

        //set UserName
        TextView username = (TextView) this.getActivity().findViewById(R.id.userName);

        String uName = prefs.getString("UserName", "");
        if(uName.matches("")){
            username.setText("Add Name in Preferences");
        }else{
            username.setText(uName);
        }

        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_home, container, false);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        //determine if gps is enabled
        if (!((MainActivity)getActivity()).checkGPSEnabled()){
            ((MainActivity)getActivity()).gpsEnabledNotification();
            if(prefs.getBoolean("alerts", false)){
                Toast.makeText(this.getActivity(), "Enable GPS for location alerts", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
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
                ((MainActivity)getActivity()).SendICENotification();
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
        ((MainActivity)getActivity()).SendICENotification();

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


}
