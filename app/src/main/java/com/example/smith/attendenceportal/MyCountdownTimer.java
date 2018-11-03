package com.example.smith.attendenceportal;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;

import java.util.concurrent.TimeUnit;

import static com.example.smith.attendenceportal.GPS_Service.NOTIFICATION_ID;
import static com.example.smith.attendenceportal.GPS_Service.NotificationActionService.builder;
import static com.example.smith.attendenceportal.GPS_Service.NotificationActionService.millis;


public abstract class MyCountdownTimer extends CountDownTimer{

    MyCountdownTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    public void pauseTimer(){
        onPauseTimer();
    }

    public void cancelTimer(){
        onCancel();
    }

    public void checkOut(){ onCheckOut(); }

    public abstract void onCancel();

    public abstract void onCheckOut();
//
//    public void onTick(long millisUntilFinished) {
//        millis = millisUntilFinished;
//        //Convert milliseconds into hour,minute and seconds
//        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis ), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
//        builder
//                .setSmallIcon(R.drawable.ic_location_on_black_24dp)
//                .setSubText("Checked in")
//                .setContentTitle("Time remaining: "+hms)
//                .setColor(Color.argb(0,55, 158, 94))
//                .setContentText("At "+location)
//                .setColor(Color.argb(0,55, 158, 94));
//
//        NotificationManager nManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
//        nManager.notify(NOTIFICATION_ID, builder.build());
//    }
//
    public abstract void onPauseTimer();
//
//    public void onFinish() {
//        builder
//                .setSmallIcon(R.drawable.ic_location_on_black_24dp)
//                .setContentTitle("You have checked in")
//                .setColor(Color.argb(0,55, 158, 94))
//                .setContentText("At "+location)
//                .setColor(Color.argb(0,55, 158, 94));
//
//        NotificationManager nManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
//        nManager.notify(NOTIFICATION_ID, builder.build());
//    }
//
}
