package com.example.smith.attendenceportal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.chip.Chip;
import android.support.design.widget.BottomSheetBehavior;
import android.support.transition.ChangeBounds;
import android.support.transition.Fade;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateLongClickListener;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

//Location of RachnaSansad 19.011559, 72.830512
public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_FINE = 1;
    private static final int MY_PERMISSIONS_REQUEST_COARSE = 2;
    private Intent intent;
    private BroadcastReceiver mBroadcastReceiver;
    private MaterialCalendarView mCalendarView;
    private TextView dateText, daysPresent, daysAbsent, daysLeftEarly, location;
    private Chip tv_check_in, tv_check_out;
    private static final String TAG = "MainActivity";
    AbsentDecorator absentDecorator;
    EventDecorator eventDecorator;
    boolean COARSE_ACCESS_GRANTED = false;
    boolean FINE_ACCESS_GRANTED = false;
    private FrameLayout container;
    private View summaryViewGroup, editViewGroup;
    private BottomSheetBehavior bottomSheetBehavior;
    boolean click = false;
    List<Integer> attendanceDetails = new ArrayList<>();
    List<EachDayInfo> result;
    /*-----------------------------------------------------------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constraint);
        setTitle("Attendance Portal");
        dateText = findViewById(R.id.dateText);

        tv_check_in = findViewById(R.id.tv_check_in);

        tv_check_out = findViewById(R.id.tv_check_out);

        location = findViewById(R.id.tv_location);
        container = findViewById(R.id.bottom_sheet_ns);
        inflateSummaryView(true);
        //container.addView(summaryViewGroup);
        setupBottomSheet();
        setUpCalender();
        dateText.setText(DateFormat.getDateInstance(DateFormat.FULL).format(new Date()));
        setupDetails(new Date());
        intent = new Intent(this, GPS_Service.class);
        permissionStuff();
    }

    public void inflateSummaryView(boolean firstTime) {
        Transition transition = new TransitionSet()
                .addTransition(new ChangeBounds())
                .addTransition(new Fade());
        TransitionManager.beginDelayedTransition(container, transition);
        container.removeAllViews();
        summaryViewGroup = getLayoutInflater().inflate(R.layout.layout_summary, container);
        daysAbsent = findViewById(R.id.daysAbsent);
        daysPresent = findViewById(R.id.daysPresent);
        daysLeftEarly = findViewById(R.id.daysLeftEarly);
        if(attendanceDetails.size()!=0) {
            if (!firstTime) {
                daysLeftEarly.setText("" + attendanceDetails.get(1));
                daysAbsent.setText("" + attendanceDetails.get(2));
                daysPresent.setText("" + attendanceDetails.get(0));
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupBottomSheet() {
        FrameLayout ns = findViewById(R.id.bottom_sheet_ns);
        ns.setOutlineProvider(new MyOutlineProvider(0, getResources()));
        ns.setClipToOutline(true);
        ns.invalidateOutline();
        bottomSheetBehavior = BottomSheetBehavior.from(ns);

    }

    public List<Date> setupDetails(final Date date) {
      /*  new Thread(new Runnable(){
            @Override
            public void run(){*/
        DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String d = dateFormat1.format(date);
        Log.d(TAG, "run: " + d);
        Cursor mCursor = db.queryDate(d);
        List<Date> time = new ArrayList<>();
        if (mCursor.getCount() == 0) {
            tv_check_in.setText("Not checked in.");
            tv_check_out.setText("Not checked out.");
            return time;
        }

        try {
            mCursor.moveToFirst();
            Log.d(TAG, "setupDetails: column count" + mCursor.getCount());
            d = mCursor.getString(mCursor.getColumnIndex(ContractClass.Columns.CHECK_IN));
            String local = mCursor.getString(mCursor.getColumnIndex(ContractClass.Columns.LOCAL));
            location.setText(local);
            DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH);
            DateFormat withoutMillis = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH);
            try {
                Date date1 = timeFormat.parse(d);
                Log.d(TAG, "run: check_in time: " + DateFormat.getTimeInstance().format(date1));
                tv_check_in.setText("Check in: " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date1));
                time.add(date1);
            } catch (ParseException e) {
                e.printStackTrace();
                Date date1 = withoutMillis.parse(d);
                Log.d(TAG, "run: check_in time: " + DateFormat.getTimeInstance().format(date1));
                tv_check_in.setText("Check in: " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date1));
            }
            d = mCursor.getString(mCursor.getColumnIndex(ContractClass.Columns.CHECK_OUT));
            try {
                Date date2 = timeFormat.parse(d);
                Log.d(TAG, "run: check_in time: " + DateFormat.getTimeInstance().format(date2));
                tv_check_out.setText("Check out: " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date2));
                time.add(date2);

            } catch (ParseException e) {
                e.printStackTrace();
                Date date2 = withoutMillis.parse(d);
                Log.d(TAG, "run: check_in time: " + DateFormat.getTimeInstance().format(date2));
                tv_check_out.setText("Check out: " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date2));
                time.add(date2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return time;
    }

    public int getNumberOfWeekDays(Calendar myCal){
        int maxDays = myCal.getActualMaximum(Calendar.DATE);
        myCal.set(myCal.get(Calendar.YEAR),myCal.get(Calendar.MONTH),1);
        int noOfSaturdays = 0;
        int noOfSundays = 0;
        for(int i=0;i<maxDays;i++){
            Log.d(TAG, "Day Of Week: "+myCal.get(Calendar.DAY_OF_WEEK));
            if(myCal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY){
                noOfSaturdays++;
            }
            if(myCal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY){
                noOfSundays++;
            }
            myCal.add(Calendar.DATE,1);

        }
        Log.d(TAG, "Days: "+maxDays);
        Log.d(TAG, "Saturdays "+noOfSaturdays);
        Log.d(TAG, "Sundays "+noOfSundays);
        return maxDays-(noOfSaturdays+noOfSundays);
    }

    public void setUpCalender() {

        mCalendarView = findViewById(R.id.calendarView);
        mCalendarView.setSelectedDate(new Date());
        SelectDecorator selectDecorator = new SelectDecorator(this);
        mCalendarView.addDecorator(selectDecorator);

        mCalendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView materialCalendarView, CalendarDay calendarDay) {
                int month = calendarDay.getMonth();
                int noOfdaysAbsent=0;
                int noOfdaysEarly=0;
                int noOfdaysPresent=0;
                Calendar cal = Calendar.getInstance();
                cal.set(calendarDay.getYear(),calendarDay.getMonth(),calendarDay.getDay());
                int noOfWeekDays = getNumberOfWeekDays(cal);
                if(result!=null) {
                    for (EachDayInfo e : result) {
                        cal.setTime(e.getDate());
                        if (cal.get(Calendar.MONTH) == month) {
                            if (e.isLessThan7hours()) {
                                noOfdaysEarly++;
                            } else {
                                noOfdaysPresent++;
                            }
                        }
                    }
                    noOfdaysAbsent = noOfWeekDays - (noOfdaysPresent + noOfdaysEarly);
                    Log.d(TAG, "onMonthChanged: daysAbsent: " + noOfdaysAbsent);
                    try {
                        daysLeftEarly.setText("" + noOfdaysEarly);
                        daysAbsent.setText("" + noOfdaysAbsent);
                        daysPresent.setText("" + noOfdaysPresent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        mCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {

            @Override
            public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {
                Date date = calendarDay.getDate();
                Log.d(TAG, "onDateSelected: " + date);
                dateText.setText(DateFormat.getDateInstance(DateFormat.FULL).format(date));
                setupDetails(calendarDay.getDate());
            }
        });
        mCalendarView.setOnDateLongClickListener(new OnDateLongClickListener() {
            @Override
            public void onDateLongClick(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay) {
                materialCalendarView.setSelectedDate(calendarDay);
                Date date = calendarDay.getDate();
                Log.d(TAG, "onDateSelected: " + date);
                dateText.setText(DateFormat.getDateInstance(DateFormat.FULL).format(date));
                List<Date> time = setupDetails(calendarDay.getDate());
                DateFormat df = new SimpleDateFormat("HH", Locale.ENGLISH);
                int checkin_hour = 0;
                int checkout_hour = 0;
                int checkin_min = 0;
                int checkout_min = 0;
                String checkin = "Set check in time";
                String checkout = "Set check out time";
                if (time.size() != 0) {
                    checkin = DateFormat.getTimeInstance(DateFormat.SHORT).format(time.get(0));
                    checkout = DateFormat.getTimeInstance(DateFormat.SHORT).format(time.get(1));
                    checkin_hour = Integer.parseInt(df.format(time.get(0)));
                    checkout_hour = Integer.parseInt(df.format(time.get(1)));
                    df = new SimpleDateFormat("mm", Locale.ENGLISH);
                    checkin_min = Integer.parseInt(df.format(time.get(0)));
                    checkout_min = Integer.parseInt(df.format(time.get(1)));
                }
                editData(checkin_hour, checkin_min, checkout_hour, checkout_min, checkin, checkout, calendarDay);
            }
        });
        getDatafromDatabase();
    }

    int new_checkin_hour;
    int new_checkin_min;
    int new_checkout_hour;
    int new_checkout_min;

    private void editData(final int checkin_hour, final int checkin_min, final int checkout_hour, final int checkout_min, String checkIn, String checkout, final CalendarDay calendarDay) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        final EditText check_in_ed, check_out_ed;
        new_checkin_hour = checkin_hour;
        new_checkin_min = checkin_min;
        new_checkout_hour = checkout_hour;
        new_checkout_min = checkout_min;

        Button saveAndUpdate;
        ImageView close;
        Transition transition = new TransitionSet()
                .addTransition(new ChangeBounds())
                .addTransition(new Fade());
        TransitionManager.beginDelayedTransition(container, transition);
        container.removeAllViews();
        editViewGroup = getLayoutInflater().inflate(R.layout.layout_edit, null);
        container.addView(editViewGroup);
        close = findViewById(R.id.close_info);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inflateSummaryView(false);
            }
        });
        check_in_ed = findViewById(R.id.check_in_ed);
        check_in_ed.setText(checkIn);
        check_out_ed = findViewById(R.id.check_out_ed);
        check_out_ed.setText(checkout);
        check_out_ed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        new_checkout_hour = hourOfDay;
                        new_checkout_min = minute;
                        String extraZero = "";
                        if (minute < 10)
                            extraZero = "0";
                        if (hourOfDay > 12)
                            check_out_ed.setText((hourOfDay - 12) + ":" + extraZero + minute + " PM");
                        else if (hourOfDay == 12)
                            check_out_ed.setText((hourOfDay) + ":" + extraZero + minute + " PM");
                        else if (hourOfDay == 0)
                            check_out_ed.setText(hourOfDay + 12 + ":" + extraZero + minute + " AM");
                        else
                            check_out_ed.setText(hourOfDay + ":" + extraZero + minute + " AM");

                    }
                }, checkout_hour, checkout_min, false).show();
            }
        });
        check_in_ed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        new_checkin_hour = hourOfDay;
                        new_checkin_min = minute;
                        String extraZero = "";
                        if (minute < 10)
                            extraZero = "0";
                        if (hourOfDay > 12)
                            check_in_ed.setText((hourOfDay - 12) + ":" + extraZero + minute + " PM");
                        else if (hourOfDay == 12)
                            check_in_ed.setText((hourOfDay) + ":" + extraZero + minute + " PM");
                        else if (hourOfDay == 0)
                            check_in_ed.setText(hourOfDay + 12 + ":" + extraZero + minute + " AM");
                        else
                            check_in_ed.setText(hourOfDay + ":" + extraZero + minute + " AM");

                    }
                }, checkin_hour, checkin_min, false).show();
            }
        });
        saveAndUpdate = findViewById(R.id.save);
        saveAndUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                calendar.set(calendarDay.getYear(), calendarDay.getMonth(), calendarDay.getDay(), new_checkin_hour, new_checkin_min);
                Date checkInTime = calendar.getTime();
                calendar.set(calendarDay.getYear(), calendarDay.getMonth(), calendarDay.getDay(), new_checkout_hour, new_checkout_min);
                Date checkOutTime = calendar.getTime();
                Date checkInDate = calendarDay.getDate();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                inflateSummaryView(false);
                updateDatabase(checkInDate, checkInTime, checkOutTime);
                getDatafromDatabase();
            }
        });

    }

    private void addDataToDatabase(Date checkInDate, Date checkInTime, Date checkOutTime) {
        Log.d(TAG, "addDataToDatabase: Adding date to database");
        Log.d(TAG, "addDataToDatabase: checkinDate " + checkInDate);
        Log.d(TAG, "addDataToDatabase: checkinTime " + checkInTime);
        Log.d(TAG, "addDataToDatabase: checkoutTime " + checkOutTime);
        DatabaseHelper mDbHelper = DatabaseHelper.getInstance(getApplicationContext());
        mDbHelper.addData(checkInDate, checkInTime, checkOutTime, "No location found");
        setupDetails(checkInDate);
    }

    private void updateDatabase(Date checkInDate, Date checkInTime, Date checkOutTime) {
        Log.d(TAG, "updateDatabase: updating dates to database");
        Log.d(TAG, "updateDatabase: checkinDate " + checkInDate);
        Log.d(TAG, "updateDatabase: checkinTime " + checkInTime);
        Log.d(TAG, "updateDatabase: checkoutTime " + checkOutTime);
        DatabaseHelper mDbHelper = DatabaseHelper.getInstance(getApplicationContext());
        mDbHelper.updateDate(checkInDate, checkInTime, checkOutTime);
        setupDetails(checkInDate);

    }

    public void getDatafromDatabase() {
        AsyncDataXtracter asyncDataXtracter = new AsyncDataXtracter();
        asyncDataXtracter.execute();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void permissionStuff() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE);
        } else {
            if (!isMyServiceRunning(GPS_Service.class)) {
                Toast.makeText(this, "service started", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        Log.d(TAG, "onRequestPermissionsResult: INITIATING FOREGROUND SERVICE");
                        startForegroundService(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.d(TAG, "onRequestPermissionsResult: API NOT HIGHER THAN O");
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_COARSE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!

                    COARSE_ACCESS_GRANTED = true;
                }
                break;
            case MY_PERMISSIONS_REQUEST_FINE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSIONS_REQUEST_COARSE);
                    // permission was granted, yay!
                    FINE_ACCESS_GRANTED = true;
                }
                break;
        }
        if (FINE_ACCESS_GRANTED && COARSE_ACCESS_GRANTED) {
            Log.d(TAG, "onRequestPermissionsResult: NOTIFICATIONS GRANTED");
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            if (!isMyServiceRunning(GPS_Service.class)) {
                Toast.makeText(this, "service started", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        Log.d(TAG, "onRequestPermissionsResult: INITIATING FOREGROUND SERVICE");
                        startForegroundService(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    //startService(intent);
                }
            }
        }
    }


    private class AsyncDataXtracter extends AsyncTask<Void, Void, List<Integer>> {
        List<CalendarDay> allDates;
        List<CalendarDay> resultDates;
        List datesLT7hrs;
        List datesGT7hrs;

        @Override
        protected List<Integer> doInBackground(Void... voids) {
            DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
            Cursor cursor = db.getData();
            Log.d(TAG, "doInBackground: number of entries: " + cursor.getCount());
            if (cursor.getCount() > 0) {
                result = new ArrayList<>();
                Date checkInTime, checkOutTime;
                cursor.moveToNext();
                Date start = convertToDate(cursor.getString(cursor.getColumnIndex(ContractClass.Columns.DATE)));
                Calendar d = Calendar.getInstance();
                d.setTime(start);
                d.set(Calendar.DAY_OF_MONTH, 1);
                Date last;
                cursor.moveToPrevious();
                while (cursor.moveToNext()) {
                    EachDayInfo eachDayInfo = new EachDayInfo();
                    eachDayInfo.setDate(convertToDate(cursor.getString(cursor.getColumnIndex(ContractClass.Columns.DATE))));
                    checkInTime = convertToTime(cursor.getString(cursor.getColumnIndex(ContractClass.Columns.CHECK_IN)));
                    checkOutTime = convertToTime(cursor.getString(cursor.getColumnIndex(ContractClass.Columns.CHECK_OUT)));
                    eachDayInfo.setCheckIn(checkInTime);
                    eachDayInfo.setCheckOut(checkOutTime);
                    long duration = checkOutTime.getTime() - checkInTime.getTime();
                    long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);
                    eachDayInfo.setDiffInHours(diffInHours);
                    long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration - diffInHours * 60 * 60 * 1000);
                    Log.d(TAG, "doInBackground: Difference in time: " + diffInHours);
                    eachDayInfo.setDiffInMinutes(diffInMinutes);
                    if (diffInHours < 7 && diffInMinutes < 59) {
                        eachDayInfo.setLessThan7hours(true);
                    }
                    eachDayInfo.setLocation(cursor.getString(cursor.getColumnIndex(ContractClass.Columns.LOCAL)));
                    result.add(eachDayInfo);
                    Log.d(TAG, "doInBackground: hours " + diffInHours + " minutes: " + diffInMinutes);
                }
                last = new Date();
                resultDates = new ArrayList<>();
                allDates = new ArrayList<>();
                for (EachDayInfo e : result) {
                    CalendarDay calendarDay = CalendarDay.from(e.getDate());
                    Log.d(TAG, "doInBackground: Day" + Calendar.DAY_OF_WEEK);
                    resultDates.add(calendarDay);
                }
                Calendar calendar = Calendar.getInstance();
                while (!start.after(last)) {
                    int day = calendar.get(Calendar.DAY_OF_WEEK);
                    if (day != Calendar.SUNDAY && day != Calendar.SATURDAY) {
                        allDates.add(CalendarDay.from(start));
                    }
                    calendar.setTime(start);
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    start = calendar.getTime();
                }
                //allDates.add(CalendarDay.from(start));
                allDates.removeAll(resultDates);
                Log.d(TAG, "doInBackground: ALL dates" + allDates);
                datesLT7hrs = new ArrayList();
                datesGT7hrs = new ArrayList();
                for (EachDayInfo e : result) {
                    if (e.isLessThan7hours()) {
                        datesLT7hrs.add(CalendarDay.from(e.getDate()));
                    } else {
                        datesGT7hrs.add(CalendarDay.from(e.getDate()));
                    }
                }

                List<Integer> info = new ArrayList<>();
                info.add(allDates.size());
                info.add(datesLT7hrs.size());
                info.add(datesGT7hrs.size());
                return info;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Integer> resultList) {
            if(allDates!=null) {
                absentDecorator = new AbsentDecorator(Color.parseColor("#FF4081"), allDates, getApplicationContext());
                mCalendarView.addDecorator(absentDecorator);
                eventDecorator = new EventDecorator(Color.parseColor("#FFCA28"), datesLT7hrs, getApplicationContext());
                mCalendarView.addDecorator(eventDecorator);
                eventDecorator = new EventDecorator(Color.parseColor("#00E676"), datesGT7hrs, getApplicationContext());
                mCalendarView.addDecorator(eventDecorator);
                mCalendarView.invalidateDecorators();

                attendanceDetails = resultList;

                daysAbsent.setText("" + resultList.get(0));
                daysLeftEarly.setText("" + resultList.get(1));
                daysPresent.setText("" + resultList.get(2));
            }
        }

        private Date convertToDate(String s) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            try {
                Date date = simpleDateFormat.parse(s);
                return date;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        private Date convertToTime(String s) {
            DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US);
            try {
                Date date = simpleDateFormat.parse(s);
                Log.d(TAG, "convertToTime: " + date);
                return date;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;

        }
    }

    private class EachDayInfo {
        private Date date;
        private Date checkIn;
        private Date checkOut;
        private String location;
        private boolean lessThan7hours = false;
        private long diffInHours;
        private long diffInMinutes;

        public long getDiffInMinutes() {
            return diffInMinutes;
        }

        public void setDiffInMinutes(long diffInMinutes) {
            this.diffInMinutes = diffInMinutes;
        }

        public long getDiffInHours() {
            return diffInHours;
        }

        public void setDiffInHours(long diffInHours) {
            this.diffInHours = diffInHours;
        }

        @Override
        public String toString() {
            return "EachDayInfo{" +
                    "date=" + date +
                    ", checkIn=" + checkIn +
                    ", checkOut=" + checkOut +
                    ", location='" + location + '\'' +
                    ", lessThan7hours=" + lessThan7hours +
                    ", diffInHours=" + diffInHours +
                    ", diffInMinutes=" + diffInMinutes +
                    '}';
        }

        public EachDayInfo() {

        }

        public boolean isLessThan7hours() {
            return lessThan7hours;
        }

        public void setLessThan7hours(boolean lessThan7hours) {
            this.lessThan7hours = lessThan7hours;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public void setCheckIn(Date checkIn) {
            this.checkIn = checkIn;
        }

        public void setCheckOut(Date checkOut) {
            this.checkOut = checkOut;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public Date getDate() {
            return date;
        }

        public Date getCheckIn() {
            return checkIn;
        }

        public Date getCheckOut() {
            return checkOut;
        }

        public String getLocation() {
            return location;
        }
    }
}

