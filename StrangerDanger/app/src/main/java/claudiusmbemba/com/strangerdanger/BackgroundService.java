package claudiusmbemba.com.strangerdanger;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by ClaudiusThaBeast on 4/16/15.
 */
public class BackgroundService extends Service implements SensorEventListener{

    Context context;
    public static final  String TAG = BackgroundService.class.getSimpleName();
    public  static final int SCREEN_OFF_RECIEVER_DELAY = 500;

    private SensorManager sManager = null;
    private PowerManager.WakeLock wakeLock = null;

    //ACCELEROMETER RELATED DEF CODE
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 2500;
    /*
     * Register this as a sensor event listener.
     */
    private void registerListener(){
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
    }

    /*
    * Un-register this as a sensor event listener.
    */
    private void unregisterListener(){
        sManager.unregisterListener(this);
    }

    //List for system to broadcast screen off
    public BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.i(TAG, "RECEIVE("+intent+")");

            //Only list for screen off
            if(!intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
            {
                return;
            }

            //create a thread
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    unregisterListener();
                    registerListener();
                }
            };

            //run thead after delay
            new Handler().postDelayed(runnable, SCREEN_OFF_RECIEVER_DELAY);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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

                last_x = x;
                last_y = y;
                last_z = z;

                if (speed > SHAKE_THRESHOLD) {
//                    Log.d("BACKGROUND", "onSensorChanged");

                    Intent intent = new Intent("notify-attack");
                    intent.setClass(this, MainActivity.class);
                    // add data
                    intent.putExtra("notify", "attack");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        //init globals
        context = this;

        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        wakeLock = pManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        //register reciever as filter on SCREEN OFF
        registerReceiver(mReciever, new IntentFilter(Intent.ACTION_SCREEN_OFF));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReciever);
        unregisterListener();
        wakeLock.release();
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        //Bring notifcation to foreground
        startForeground(android.os.Process.myPid(), new Notification());
        registerListener();
        wakeLock.acquire();

        return START_STICKY;
    }

}
