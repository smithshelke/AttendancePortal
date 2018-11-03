package com.example.smith.attendenceportal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

//Location of RachnaSansad 19.011559, 72.830512

public class GPS_Service extends Service {

    private LocationManager mLocationManager;
    private static String ACTION_CHECK_IN = "CHECK-IN";
    public static int NOTIFICATION_ID = 123;
    private static String ACTION_STOP_TIMER = "STOP_TIMER";
    private static String ACTION_PAUSE_TIMER = "PAUSE_TIMER";
    public static final String ACTION_CHECK_OUT = "CHECK_OUT";
    private static final String ACTION_RESUME_TIMER = "RESUME_TIMER";
    private static final String ACTION_CANCEL_TIMER = "CANCEL_TIMER";
    private static final String TAG = "GPS_Service";
    public static boolean isCheckedIn = false;


    public GPS_Service() {
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {

        FusedLocationProviderClient mFusedLocationClient;
        Log.d(TAG, "onCreate: Created");
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        getLastLocationNewMethod();


        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission not Granted", Toast.LENGTH_SHORT).show();
            return;
        }
        LocationCallback locationCallback = new LocationCallback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for(Location location: locationResult.getLocations() ){
                    Log.d(TAG, "onLocationResult List: "+getAddress(location));
                }
                //Log.d(TAG, "onLocationResult: " + getAddress(locationResult.getLocations().get(0)));

                if (!isCheckedIn){
                    buildNotification(getAddress(locationResult.getLocations().get(0)));
                }else{
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        buildNotification(getAddress(locationResult.getLocations().get(0)));
                    }
                }
            }
        };
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, null /* Looper */);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ends");
        Toast.makeText(this, "I am destroyed", Toast.LENGTH_SHORT).show();
        super.onDestroy();
//        if (mLocationManager != null) {
//            mLocationManager.removeUpdates(mLocationListener);
//        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void buildNotification(String _Location) {

        Intent action1Intent = new Intent(this, NotificationActionService.class)
                .setAction(ACTION_CHECK_IN)
                .putExtra("LOCATION", _Location);

        PendingIntent action1PendingIntent = PendingIntent.getService(this, 6,
                action1Intent, PendingIntent.FLAG_CANCEL_CURRENT);
        int importance = NotificationManager.IMPORTANCE_LOW;

        String CHANNEL_ID = "my_channel_01";// The id of the channel.
        CharSequence name = "Attendence";

        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), "hello")
                        .setSmallIcon(R.drawable.ic_location_on_black_24dp)
                        .setColor(Color.argb(0, 55, 158, 94))
                        .setContentTitle("You are here")
                        .setChannelId(CHANNEL_ID)
                        .setStyle(new NotificationCompat.BigTextStyle())
                        .setContentText(_Location)
                        .setContentInfo(_Location)
                        .setTicker("Time elapsed")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .addAction(android.R.drawable.ic_menu_compass, "Check In", action1PendingIntent);
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.createNotificationChannel(mChannel);
        startForeground(NOTIFICATION_ID + 2,builder.build());
        nManager.notify(NOTIFICATION_ID + 2, builder.build());
    }

    public String getAddress(Location location) {

        List<String> providerList = mLocationManager.getAllProviders();
        String _Location = "";
        if (null != location && null != providerList && providerList.size() > 0) {
            // double longitude = locations.getLongitude();
            //double latitude = locations.getLatitude();
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (null != listAddresses && listAddresses.size() > 0) {
                    _Location = listAddresses.get(0).getAddressLine(0);
                    //  Toast.makeText(getApplicationContext(),_Location,Toast.LENGTH_SHORT).show();
                    return _Location;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    @SuppressLint("MissingPermission")
    private void getLastLocationNewMethod() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            Log.d(TAG, "onSuccess: " + getAddress(location));
                            Log.d(TAG, "Lastknown Location: " + getAddress(location));
                            buildNotification(getAddress(location));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    //=======================================================================================================

    public static class NotificationActionService extends Service {
        public static boolean hasStarted = false;
        public static Date checkin_date;
        private static MyCountdownTimer countDownTimer;
        public static long millis;
        public static NotificationCompat.Builder builder;
        private static final String TAG = "NotificationActionService";
        private static  boolean foregroundModeActivated=false;

        @Override
        public void onCreate() {
            super.onCreate();

            Log.d(TAG, "onCreate: Started");
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            String location="";
            String action="";
            if(intent!=null){
                action = intent.getAction();
                location = intent.getExtras().getString("LOCATION");
            }
            Log.d(TAG, "location " + location);

            if (ACTION_CHECK_IN.equals(action)) {
                NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nManager.cancel(NOTIFICATION_ID+2);
                Log.d(TAG, "onHandleIntent: Action triggered");
                if (!hasStarted) {
                    hasStarted = true;
                    checkin_date = new Date();
                    isCheckedIn = true;
                    startTimer(1000 * 60* 60* 7, location);
                }
            } else if (ACTION_PAUSE_TIMER.equals(action)) {
                Log.d(TAG, "onHandleIntent: timer paused");
                countDownTimer.pauseTimer();
            } else if (ACTION_RESUME_TIMER.equals(action)) {
                Log.d(TAG, "onHandleIntent: timer resumed");
                startTimer(millis, location);
            } else if (ACTION_CHECK_OUT.equals(action)) {
                Log.d(TAG, "onHandleIntent: checked out");
                hasStarted = false;
                isCheckedIn = false;
                countDownTimer.checkOut();
            } else if (ACTION_CANCEL_TIMER.equals(action)){
                Log.d(TAG, "onHandleIntent: timer canceled");
                isCheckedIn = false;
                countDownTimer.cancelTimer();
                addDataToDatabase(location);
            }
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.d(TAG, "onDestroy: destroyed");
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        private void startTimer(final long noOfMinutes, final String location) {
            Intent intent = new Intent(this, NotificationActionService.class);
            intent.putExtra("LOCATION", location);
            intent.setAction(ACTION_STOP_TIMER);
            final PendingIntent stopTimerIntent = PendingIntent.getService(getApplicationContext(), 0,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);
            intent.setAction(ACTION_PAUSE_TIMER);
            final PendingIntent pauseTimerIntent = PendingIntent.getService(getApplicationContext(), 1,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);
            intent.setAction(ACTION_RESUME_TIMER);
            final PendingIntent resumeTimerIntent = PendingIntent.getService(getApplicationContext(), 3,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);
            intent.setAction(ACTION_CANCEL_TIMER);
            final PendingIntent cancelTimerIntent = PendingIntent.getService(getApplicationContext(), 4,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);
            intent.setAction(ACTION_CHECK_OUT);
            final PendingIntent checkOutTimerIntent = PendingIntent.getService(getApplicationContext(), 5,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);


            /*-------------------------------------------------------------------------------------*/
            
            /*-------------------------------------------------------------------------------------*/
            final HandlerThread thread = new HandlerThread("Handler thread");
            thread.start();
            Handler mHandler = new Handler(thread.getLooper());
            mHandler.post(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    String CHANNEL_ID = "ONGOING_NOTIFICATION";// The id of the channel.
                    CharSequence name = "Attendence";
                    int importance = NotificationManager.IMPORTANCE_LOW;
                    NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                    NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nManager.createNotificationChannel(mChannel);
                    builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
                    builder.setOngoing(true);
                    builder.setChannelId(CHANNEL_ID);
                    builder.setPriority(NotificationCompat.PRIORITY_MAX);
                    builder.addAction(android.R.drawable.ic_menu_compass, "Pause", pauseTimerIntent);
                    builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Check out", checkOutTimerIntent);
                    if(!foregroundModeActivated){
                        foregroundModeActivated=true;
                        startForeground(NOTIFICATION_ID,builder.build());
                    }
                   // startForeground(NOTIFICATION_ID,builder.build());
                    countDownTimer = new MyCountdownTimer(noOfMinutes, 1000) {

                        @Override
                        public void onCancel() {
                            hasStarted = false;
                            countDownTimer.cancel();
                            NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            nManager.cancel(NOTIFICATION_ID);
                            //addDataToDatabase(location);
                            stopForeground(true);
                            foregroundModeActivated=false;
                            thread.quitSafely();
                        }

                        @Override
                        public void onCheckOut() {
                            countDownTimer.cancel();
                            builder.mActions.clear();
                            builder.setOngoing(false);
                            builder
                                    .setSmallIcon(R.drawable.ic_location_on_black_24dp)
                                    .setContentTitle("Checked Out")
                                    .setSubText("Checked Out")
                                    .setColor(Color.argb(0, 55, 158, 94))
                                    .setContentText("From " + location)
                                    .setColor(Color.argb(0, 55, 158, 94));
                            NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            nManager.notify(NOTIFICATION_ID, builder.build());
                            addDataToDatabase(location);
                            stopForeground(false);
                            foregroundModeActivated=false;
                            thread.quitSafely();
                        }

                        @Override
                        public void onTick(long millisUntilFinished) {
                            millis = millisUntilFinished;
                            //Convert milliseconds into hour,minute and seconds
                            @SuppressLint("DefaultLocale")
                            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
                            builder
                                    .setSmallIcon(R.drawable.ic_location_on_black_24dp)
                                    .setSubText("Checked in")
                                    .setContentTitle("Time remaining: " + hms)
                                    .setColor(Color.argb(0, 55, 158, 94))
                                    .setContentText("At " + location)
                                    .setColor(Color.argb(0, 55, 158, 94));
                                NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                nManager.notify(NOTIFICATION_ID, builder.build());
                        }

                        @Override
                        public void onPauseTimer() {
                            builder.mActions.clear();
                            builder.setSubText("Checked in (Paused)");
                            builder.addAction(android.R.drawable.ic_menu_compass, "Resume", resumeTimerIntent);
                            builder.addAction(android.R.drawable.ic_menu_compass, "Cancel", cancelTimerIntent);
                            NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            nManager.notify(NOTIFICATION_ID, builder.build());
                            countDownTimer.cancel();
                        }

                        @Override
                        public void onFinish() {
                            builder.mActions.clear();
                            builder.setOngoing(false);
                            builder
                                    .setSmallIcon(R.drawable.ic_location_on_black_24dp)
                                    .setContentTitle("Duration complete")
                                    .setColor(Color.argb(0, 55, 158, 94))
                                    .setContentText("At " + location)
                                    .setColor(Color.argb(0, 55, 158, 94));
                            hasStarted = false;
                            NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            nManager.notify(NOTIFICATION_ID, builder.build());
                            Log.d(TAG, "onFinish: called");
                            addDataToDatabase(location);
                            Log.d(TAG, "onFinish: adding data to database");
                            stopForeground(false);
                            foregroundModeActivated=false;
                            thread.quitSafely();
                        }
                    };
                    countDownTimer.start();
                }
            });


        }

      /*  @Override
        protected void onHandleIntent(Intent intent) {
            Log.d(TAG, "onHandleIntent: HAndleIntent Called");

            String action = intent.getAction();
            final String location = intent.getExtras().getString("LOCATION");
            Log.d(TAG, "location " + location);

            if (ACTION_CHECK_IN.equals(action)) {
                Log.d(TAG, "onHandleIntent: Action triggered");
                if (!hasStarted) {
                    hasStarted = true;
                    checkin_date = new Date();
                    startTimer(1000 * 60 * 60 * 7, location);
                }
            } else if (ACTION_STOP_TIMER.equals(action)) {
                Log.d(TAG, "onHandleIntent: stop initiated");
                countDownTimer.cancel();
            } else if (ACTION_PAUSE_TIMER.equals(action)) {
                Log.d(TAG, "onHandleIntent: timer paused");
                countDownTimer.pauseTimer();
            } else if (ACTION_RESUME_TIMER.equals(action)) {
                Log.d(TAG, "onHandleIntent: timer resumed");
                startTimer(millis, location);
            } else if (ACTION_CHECK_OUT.equals(action)) {
                Log.d(TAG, "onHandleIntent: checked out");
                hasStarted = false;
                countDownTimer.checkOut();
            } else {
                Log.d(TAG, "onHandleIntent: timer canceled");
                countDownTimer.cancelTimer();
                addDataToDatabase(location);
            }

        }
*/
        private void addDataToDatabase(String location) {
            Log.d(TAG, "addDataToDatabase: Adding date to database");
            DatabaseHelper mDbHelper = DatabaseHelper.getInstance(getApplicationContext());
            mDbHelper.addData(new Date(), checkin_date, new Date(), location);
        }
    }
}

