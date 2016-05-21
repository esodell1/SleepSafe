package com.sleepsafe.iot.devices.sleepsafe.helper;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * This class implements the historical database storage for the app. This class will instantiate
 * and interact with the SQLite database internal to Android to maintain the persistant storage
 * requirements of this app.
 *
 * @author Eric Odell
 * @author Ihar Lavor
 * @version 1.0
 */
public class HistoryDBProvider {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "SleepSafeHistory.db";
    private static final String DB_TABLE = "SampleHistory";

    private HistoryDBHelper mHistoryDBHelper;
    private SQLiteDatabase mSQLiteDatabase;

    public HistoryDBProvider(Context context) {
        mHistoryDBHelper = new HistoryDBHelper(
                context, DB_NAME, null, DB_VERSION);
        mSQLiteDatabase = mHistoryDBHelper.getWritableDatabase();
    }

    /**
     * Adds a sample to the database given the individual parameters.
     * @param hr The HR value
     * @param spo2 The SpO2 value
     * @param temp The temperature value
     * @return true if the sample was successfully added to the database, false otherwise
     */
    public boolean insertSample(int hr, int spo2, int temp) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("hr", hr);
        contentValues.put("spo2", spo2);
        contentValues.put("temp", temp);
        contentValues.put("time", Calendar.getInstance().toString());

        long rowId = mSQLiteDatabase.insert(DB_TABLE, null, contentValues);
        return rowId != -1;
    }

    /**
     * Adds a sample to the database given the Sample object.
     * @param sample The Sample object to store
     * @return true if the sample was successfully added to the database, false otherwise
     */
    public boolean insertSample(Sample sample) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("hr", sample.hr_val);
        contentValues.put("spo2", sample.spo2_val);
        contentValues.put("temp", sample.temp_val);
        contentValues.put("time", Calendar.getInstance().getTimeInMillis());

        long rowId = mSQLiteDatabase.insert(DB_TABLE, null, contentValues);
        return rowId != -1;
    }

    /**
     * Closes the database.
     */
    public void closeDB() {
        mSQLiteDatabase.close();
    }

    /**
     * Returns the list of courses from the local Course table.
     * @return list
     */
    public List<Sample> getSamples() {

        String[] columns = {
                "hr", "spo2", "temp", "time"
        };

        Cursor c = mSQLiteDatabase.query(
                DB_TABLE,  // The table to query
                columns,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        c.moveToFirst();
        List<Sample> list = new ArrayList<Sample>();
        for (int i=0; i<c.getCount(); i++) {
            int hr = c.getInt(0);
            int spo2 = c.getInt(1);
            int temp = c.getInt(2);
            String time = c.getString(3);
            Sample sample = new Sample(hr, spo2, temp, time);
            list.add(sample);
            c.moveToNext();
        }

        return list;
    }

    public List<Entry> getHRSamples() {

        String[] columns = {
                "hr", "time"
        };

        Cursor c = mSQLiteDatabase.query(
                DB_TABLE,  // The table to query
                columns,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                "time"                                 // The sort order
        );
        c.moveToFirst();
        List<Entry> list = new ArrayList<>();
        for (int i=0; i<c.getCount(); i++) {
            int hr = c.getInt(0);
            long time = c.getLong(1);
            Entry sample = new Entry(hr, (int)time);
            list.add(sample);
            c.moveToNext();
        }

        return list;
    }


    /**
     * Delete all the data from the COURSE_TABLE
     */
    public void deleteSamples() {
        mSQLiteDatabase.delete(DB_TABLE, null, null);
    }


    class HistoryDBHelper extends SQLiteOpenHelper {

        private final String CREATE_SAMPLE_SQL;

        private final String DROP_SAMPLE_SQL;

        public HistoryDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
            CREATE_SAMPLE_SQL = "CREATE TABLE IF NOT EXISTS SampleHistory\n" +
                    "   (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "    hr INTEGER, spo2 INTEGER,\n" +
                    "    temp INTEGER, time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)";
                    //context.getString(R.string.CREATE_SAMPLE_SQL);
            DROP_SAMPLE_SQL = "DROP TABLE IF EXISTS SampleHistory";
            //context.getString(R.string.DROP_SAMPLE_SQL);

        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_SAMPLE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL(DROP_SAMPLE_SQL);
            onCreate(sqLiteDatabase);
        }
    }
}
