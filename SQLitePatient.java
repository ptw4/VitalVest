package ptw4.test;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.*;

public class SQLitePatient extends SQLiteOpenHelper {
    private static final String RECORDS    = "SESSION_RECORDS";
    private static final String SESSION    = "SESSION_ID";
    private static final String KEY        = "INDEX_KEY";
    private static final String TIME       = "TIME_STAMP";
    private static final String VALUE1     = "TEMPERATURE";
    private int                 DB_VERSION = 1;
    private int                 DEVICE_ID;
    private int                 USER_ID;
    private int                 SESSION_ID;
    private int                 counter;


    public SQLitePatient( Context context, int device, int user, int session ) {
        super( context, RECORDS, null, 1 );
        DEVICE_ID  = device;
        USER_ID    = user;
        SESSION_ID = session;
        counter    = 0;
    }

    public void onCreate( SQLiteDatabase db ) {
        //Create the table
        //the "KEY" variable is set up to be an alias for ROWID for easier code comprehension
        db.execSQL( "CREATE TABLE IF NOT EXISTS" + RECORDS + " ("
                    + KEY     + " INTEGER PRIMARY KEY ASC , "
                    + SESSION + " INTEGER , "
                    + TIME    + " DATETIME DEFAULT CURRENT_TIMESTAMP , "
                    + VALUE1  + " DOUBLE PRECISION " + ")" );
        db.setVersion( DB_VERSION );
    }

    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
        if( DB_VERSION == oldVersion ) {
            //drop the old table
            db.execSQL("DROP TABLE IF EXISTS " + RECORDS);
            //call the creation again
            onCreate(db);
            //set the new version
            DB_VERSION = newVersion;
            db.setVersion( DB_VERSION );
        }
    }

    private String getDateTime() {
        //set up the date to be the SQL date format
        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss",
                                                            Locale.getDefault( ) );
        //get the time
        Date date = new Date();
        return dateFormat.format(date);
    }

    public int insertRecord( double value ) {
        long rowid;
        //get the database
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues  cv = new ContentValues();
        //set up an insertion
        cv.put( SESSION, SESSION_ID );
        cv.put( TIME,    getDateTime() );
        cv.put( VALUE1,  value );
        //insert the values
        rowid = db.insert( RECORDS, null, cv );
        //check if an error occurred
        if( rowid == -1 ) {
            return 0;
        } else {
            //increment session counter on success
            counter++;
            return 1;
        }
    }

    //time in this context is the time back from the current time
    //i.e. time = 0 is now, time = 1 is 1 second ago
    public String makePacket( int time ) {
        int rowid;
        String timestamp;
        double value;
        //set row id to 0 to prevent errors;
        rowid = 0;
        //check for an invalid request
        if( time > counter ) {
            return null;
        }
        //get a database to read from
        SQLiteDatabase db = this.getReadableDatabase();
        //set up a cursor to hold result sets
        Cursor cursor;
        //set up a query to grab the current time
        //the where clause is set up to ensure it is the current time from the current session
        String getMaxQuery = "SELECT * FROM " + RECORDS + " " + KEY + " ORDER BY DESC LIMIT 1 "
                           + "WHERE " + SESSION + "=" + SESSION_ID;
        //query the current database for the maximum
        cursor = db.rawQuery( getMaxQuery, null );
        //move the pointer to the first record in the result set
        cursor.moveToFirst( );
        //make sure aresult was returned
        if( cursor.getCount() > 0 ) {
            //get the resulting rowid
            rowid = cursor.getInt( 0 );
        }
        //set the rowid to the offset
        rowid -= time;
        //queries to get the desired row's information
        String getValQuery = "SELECT * FROM " + RECORDS + " " + TIME
                           + "WHERE " + SESSION + "=" + SESSION_ID
                           + "AND "   + KEY     + "=" + rowid;
        String getDatQuery = "SELECT * FROM " + RECORDS + " " + VALUE1
                           + "WHERE " + SESSION + "=" + SESSION_ID
                           + "AND "   + KEY     + "=" + rowid;

        cursor = db.rawQuery( getValQuery, null );
        cursor.moveToFirst( );
        if( cursor.getCount() > 0 ) {
            value = cursor.getDouble(0);
        }

        cursor = db.rawQuery( getDatQuery, null );
        cursor.moveToFirst( );
        if( cursor.getCount() > 0 ) {
            timestamp = cursor.getString( 0 );
        }

        //originally had "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\t"
        //on the front of this but left it off for multipacket concatination
        String XML = "\t<packet>\n"
                   + "\t\t<value1>" + value     + "</value1>\n"
                   + "\t\t<time>"   + timestamp + "</time>\n"
                   + "\t</packet>";

        return XML;
    }

    //creates a packet with current session information to top a group of packets with
    public String makeHeaderPacket( ) {
        String XML = "\t<sessioninfo>\n"
                   + "\t\t<sid>" + SESSION_ID + "</sid>\n"
                   + "\t\t<uid>" + USER_ID    + "</uid>\n"
                   + "\t\t<did>" + DEVICE_ID  + "</did>\n"
                   + "\t</sessioninfo>";
        return XML;
    }
}
