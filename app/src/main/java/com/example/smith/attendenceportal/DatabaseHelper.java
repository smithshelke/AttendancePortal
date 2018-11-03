package com.example.smith.attendenceportal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "Details.db";
    private static final int DATABASE_VERSION = 1;
    private static DatabaseHelper instance = null;

    private DatabaseHelper(Context context){
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
    }

    /**
     * Get instance of the database helper object
     * @param context the content of providers context
     * @return a SQLite database helper object
     */
    static DatabaseHelper getInstance(Context context){
        if (instance==null){
            Log.d(TAG, "getInstance: creating new instance");
            instance = new DatabaseHelper(context);
        }
        return instance;    
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: OnCreate starts");
        String createTable = "CREATE TABLE "+ContractClass.TABLE_NAME+ "( "
                +ContractClass.Columns.ID +" INTEGER , "
                + ContractClass.Columns.DATE+" TEXT PRIMARY KEY, "
                +ContractClass.Columns.CHECK_IN+" TEXT, "
                +ContractClass.Columns.CHECK_OUT+" TEXT, "
                +ContractClass.Columns.MONTH+" INTEGER, "
                +ContractClass.Columns.YEAR+" INTEGER, "
                +ContractClass.Columns.LOCAL+" TEXT);";
        db.execSQL(createTable);
        Log.d(TAG, "onCreate: ends");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ ContractClass.TABLE_NAME);
        onCreate(db);
        Log.d(TAG, "onUpgrade: starts");
        switch(oldVersion){
            case 1:
                //Upgrade logic from version 1
                break;
            default:
                throw new IllegalStateException("onUpgrade() with unknown newVersion: "+newVersion);
        }
    }

    void addData(Date checkInDate, Date checkInTime, Date checkOutTime, String local){
        DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String check_in_date = dateFormat.format(checkInDate);
        String check_in_time = timeFormat.format(checkInTime);
        String check_out_time = timeFormat.format(checkOutTime);
        Calendar cal = Calendar.getInstance();
        cal.setTime(checkInDate);
        Log.d(TAG, "addData: adding data...");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContractClass.Columns.DATE,check_in_date);
        contentValues.put(ContractClass.Columns.CHECK_IN,check_in_time);
        contentValues.put(ContractClass.Columns.CHECK_OUT,check_out_time);
        contentValues.put(ContractClass.Columns.YEAR,cal.get(Calendar.YEAR));
        contentValues.put(ContractClass.Columns.MONTH,cal.get(Calendar.MONTH));

        contentValues.put(ContractClass.Columns.LOCAL,local);
        //long result = db.update(ContractClass.TABLE_NAME,contentValues,ContractClass.Columns.DATE+"="+date,null);
        long result = db.replace(ContractClass.TABLE_NAME,null,contentValues);
        Log.d(TAG, "addData: Complete, Status"+result);
    }

    void updateDate(Date checkInDate, Date checkInTime, Date checkOutTime){
        DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String check_in_date = dateFormat.format(checkInDate);
        String check_in_time = timeFormat.format(checkInTime);
        String check_out_time = timeFormat.format(checkOutTime);
        Log.d(TAG, "addData: adding data...");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContractClass.Columns.DATE,check_in_date);
        contentValues.put(ContractClass.Columns.CHECK_IN,check_in_time);
        contentValues.put(ContractClass.Columns.CHECK_OUT,check_out_time);
        //long result = db.update(ContractClass.TABLE_NAME,contentValues,ContractClass.Columns.DATE+"="+date,null);
        long result = db.update(ContractClass.TABLE_NAME,contentValues,ContractClass.Columns.DATE+"=?",new String[]{check_in_date});
        Log.d(TAG, "addData: Complete, Status"+result);
    }

    Cursor queryDate(String date){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM "+ ContractClass.TABLE_NAME
                +" WHERE "+ContractClass.Columns.DATE+"="+"'"+date+"'";
        Cursor data = db.rawQuery(query,null);
        return data;
    }

    public Cursor getData(){
    SQLiteDatabase db = this.getWritableDatabase();
    String query = "SELECT * FROM " + ContractClass.TABLE_NAME;
    Cursor data = db.rawQuery(query,null);
    return data;
    }
}
