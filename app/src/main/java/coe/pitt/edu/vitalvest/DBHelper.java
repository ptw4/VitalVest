package coe.pitt.edu.vitalvest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "patientList.db";
    public static final String PATIENTS_TABLE_NAME = "patients";
    public static final String SESSIONS_TABLE_NAME = "sessions";
    public static final String ALL_KEY = "uniqueid";
    public static final String PATIENTS_COLUMN_NAME = "name";
    public static final String PATIENTS_COLUMN_DEVICE = "device";
    private static final String RECORDS    = "SESSION_RECORDS";
    private static final String SESSION    = "SESSION_ID";
    private static final String KEY        = "INDEX_KEY";
    private static final String TIME       = "TIME_STAMP";
    private static final String VALUE1     = "TEMPERATURE";
    private static final String VALUE2     = "PULSE_RATE";
    private int                 DB_VERSION = 1;
    private int                 DEVICE_ID;
    private int                 USER_ID;
    private int                 SESSION_ID;
    private int                 counter;
    Integer patientCount;
    Integer sessionCount = 1;

    public DBHelper(Context context)
    {
        // Creates a database
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        patientCount = 1;
        sessionCount = 1;
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + PATIENTS_TABLE_NAME + "("
                + ALL_KEY + " INTEGER , "
                + PATIENTS_COLUMN_NAME + " TEXT, "
                + PATIENTS_COLUMN_DEVICE + " INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SESSIONS_TABLE_NAME + " ("
                + ALL_KEY + " INTEGER , "
                + SESSION + " INTEGER , "
                + TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP , "
                + VALUE1 + " DOUBLE PRECISION , "
                + VALUE2 + " DOUBLE PRECISION" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS PATIENT_INFO");
        db.execSQL("DROP TABLE IF EXISTS PATIENT_RECORDS");
        db.execSQL("DROP TABLE IF EXISTS PATIENT_SESSIONS");
        onCreate(db);
    }

    public boolean insertPatient(String name, String device)
    {
        patientCount = numberOfPatients()+1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ALL_KEY, patientCount);
        contentValues.put("name", name);
        contentValues.put("device", device);
        db.insert("patients", null, contentValues);
        return true;
    }

    private String getDateTime() {
        //set up the date to be the SQL date format
        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault() );
        //get the time
        Date date = new Date();
        return dateFormat.format(date);
    }

    public int insertSession( Integer id, double value1, double value2 ) {
        long rowid;
        //get the database
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues  cv = new ContentValues();
        //set up an insertion
        cv.put(ALL_KEY, id);
        cv.put( SESSION, sessionCount );
        cv.put( TIME,    getDateTime() );
        cv.put( VALUE1,  value1 );
        cv.put( VALUE2,  value2 );
        //insert the values
        rowid = db.insert( SESSIONS_TABLE_NAME, null, cv );
        //check if an error occurred
        if( rowid == -1 ) {
            return 0;
        } else {
            //increment session counter on success
            sessionCount++;
            return 1;
        }
    }

    private Cursor fieldRetrieval( SQLiteDatabase db, String table, String value, int id ) {
        Cursor cursor;

        String query = "SELECT * FROM " + table + " " + value
                + "WHERE "         + ALL_KEY + "=" + id;
        cursor = db.rawQuery( query, null );
        cursor.moveToFirst();

        return cursor;
    }

    public Cursor getData(String table, int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from " + table + " where " + ALL_KEY + "="
                + id + "", null);
        return res;
    }

    public int numberOfPatients(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, PATIENTS_TABLE_NAME);
        return numRows;
    }

    public boolean updatePatient (Integer id, String name, String device)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("device", device);
        db.update("patients", contentValues, "uniqueid = ? ", new String[] { Integer.toString(id) });
        return true;
    }

    public Integer deletePatient (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("patients",
                "uniqueid = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getAllPatients()
    {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from patients", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(PATIENTS_COLUMN_NAME)));
            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList<String> getAllSessions(int id)
    {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + SESSIONS_TABLE_NAME
                + " WHERE " + ALL_KEY + " = " + id, null);
        res.moveToFirst();

        while(res.isAfterLast() == false){
            String rec = res.getString(res.getColumnIndex(TIME)) + ","
                    + res.getString(res.getColumnIndex(VALUE1)) + ","
                    + res.getString(res.getColumnIndex(VALUE2));
            array_list.add(rec);
            res.moveToNext();
        }
        return array_list;
    }
}