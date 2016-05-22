package com.sleepsafe.iot.devices.sleepsafe.helper;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.sql.Timestamp;
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
     * Adds a sample to the database given the Sample object.
     * @param sample The Sample object to store
     * @return true if the sample was successfully added to the database, false otherwise
     */
    public boolean insertSample(Sample sample, int currentSession) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("hr", sample.hr_val);
        contentValues.put("spo2", sample.spo2_val);
        contentValues.put("temp", sample.temp_val);
        contentValues.put("time", Calendar.getInstance().getTimeInMillis());
        contentValues.put("session", currentSession);
        long rowId = mSQLiteDatabase.insert(DB_TABLE, null, contentValues);
        return rowId != -1;
    }

    public int getNextSession() {
        Cursor c = mSQLiteDatabase.rawQuery("SELECT MAX(session) FROM " + DB_TABLE, null, null);
        c.moveToLast();
        int val = 0;
        if (c.getCount() > 0) val = c.getInt(0) + 1;
        return val;
    }

    /**
     * Closes the database.
     */
    public void closeDB() {
        mSQLiteDatabase.close();
    }

    public List<Sample> getSamples() {

        String[] columns = {
                "hr", "spo2", "temp", "time", "session"
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
        List<Sample> list = new ArrayList<>();
        for (int i=0; i<c.getCount(); i++) {
            Sample sample = new Sample(c.getInt(0), c.getInt(1), c.getInt(2), c.getLong(3), c.getInt(4));
            Log.v("DBHELPER", "Sample: " + sample.toString());
            list.add(sample);
            c.moveToNext();
        }

        return list;
    }

    public List<Sample> getCurrentSessionSamples() {

        String SQL = "SELECT * FROM " + DB_TABLE +
                " WHERE session=" + (getNextSession() - 1) +
                " ORDER BY time DESC";

        String[] columns = {
                "hr", "spo2", "temp", "time", "session"
        };

        Log.v("DBHELPER", "SQL: " + SQL);
        Cursor c = mSQLiteDatabase.rawQuery(SQL, null, null);
        c.moveToFirst();
        List<Sample> list = new ArrayList<>();
        for (int i=0; i<c.getCount(); i++) {
            Sample sample = new Sample(c.getInt(1), c.getInt(2), c.getInt(3), c.getLong(4), c.getInt(5));
            Log.v("DBHELPER", "Sample: " + sample.toString());
            list.add(sample);
            c.moveToNext();
        }
        c.close();

        return list;
    }


    /**
     * Delete all the data from the COURSE_TABLE
     */
    public void deleteSamples() {
        mHistoryDBHelper.clearDB(mSQLiteDatabase);
    }


    class HistoryDBHelper extends SQLiteOpenHelper {

        private final String CREATE_SAMPLE_SQL;

        private final String DROP_SAMPLE_SQL;

        public HistoryDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
            CREATE_SAMPLE_SQL = "CREATE TABLE IF NOT EXISTS SampleHistory " +
                    "   (id INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT, " +
                    "    hr INTEGER, " +
                    "    spo2 INTEGER, " +
                    "    temp INTEGER, " +
                    "    time LONG NOT NULL, " +
                    "    session INTEGER)";
                    //context.getString(R.string.CREATE_SAMPLE_SQL);
            DROP_SAMPLE_SQL = "DROP TABLE IF EXISTS SampleHistory";
            //context.getString(R.string.DROP_SAMPLE_SQL);

        }

        public void clearDB(SQLiteDatabase db) {
            db.execSQL(DROP_SAMPLE_SQL);
            onCreate(db);
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
