package coe.pitt.edu.vitalvest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "patientList.db";
    public static final String PATIENTS_TABLE_NAME = "patients";
    public static final String PATIENTS_COLUMN_ID = "id";
    public static final String PATIENTS_COLUMN_NAME = "name";
    public static final String PATIENTS_COLUMN_DEVICE = "device";

    public DBHelper(Context context)
    {
        // Creates a database
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table PATIENT_INFO " +
                        "(UNIQUE_KEY integer primary key, USER_ID integer, USER_NAME text, DEVICE_ID integer)"
        );
        db.execSQL(
                "create table PATIENT_RECORDS " +
                        "(SESSION_ID integer primary key, INDEX_KEY integer, " +
                        "TIME_STAMP integer, VALUE_1 integer, VALUE_2 integer, VALUE_3 integer)"
        );
        db.execSQL(
                "create table PATIENT_SESSIONS " +
                        "(UNIQUE_KEY integer primary key, SESSION_ID integer)"
        );
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
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("UNIQUE_KEY", name);
        contentValues.put("device", device);
        db.insert("patients", null, contentValues);
        return true;
    }

    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from contacts where id=" + id + "", null);
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, PATIENTS_TABLE_NAME);
        return numRows;
    }

    public boolean updateContact (Integer id, String name, String device)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("device", device);
        db.update("patients", contentValues, "id = ? ", new String[] { Integer.toString(id) });
        return true;
    }

    public Integer deleteContact (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("contacts",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getAllCotacts()
    {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(PATIENTS_COLUMN_NAME)));
            res.moveToNext();
        }
        return array_list;
    }
}