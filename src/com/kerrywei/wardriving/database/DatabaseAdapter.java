package com.kerrywei.wardriving.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseAdapter {

    // database fields
    public static final String ROWID = "_id";
    public static final String MAC_ADDRESS = "macAddress";
    public static final String NETWORK_NAME = "networkName";
    public static final String CAPABILITIES = "capabilities";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String FREQUENCY = "frequency";
    public static final String SIGNAL = "signal";
    
    
    private static final String DATABASE_TABLE = "WAPInfo";
    
    
    private Context context;
    private SQLiteDatabase database;
    private WardrivingDatabaseHelper wardrivingDBHelper;

    public DatabaseAdapter(Context _context) {
        context = _context;
    }

    public DatabaseAdapter open() throws SQLException {
        wardrivingDBHelper = new WardrivingDatabaseHelper(context);
        database = wardrivingDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        wardrivingDBHelper.close();
    }

    
    /*
     * insert one entry into the database
     */
    public long insertEntry(String macAddr, String latitude, String longitude, 
            String networkName, String capabilities, int frequency, int signal) {
        /*
        String sql = "INSERT INTO " + DATABASE_TABLE
                + " (macAddress, latitude, longitude, frequency, signal) "
                + "VALUES (" + macAddr + ", "+ latitude + ", " + longitude + ", " + frequency + ", " + strength + ");";

        database.execSQL(sql);
        */
        ContentValues values = createContentValues(macAddr, latitude, longitude, networkName, capabilities, frequency, signal);

        long ans = database.insert(DATABASE_TABLE, null, values);
        return ans;
    }

    
    /**
     * Return a Cursor over the list of all todo in the database
     * 
     * @return Cursor over all notes
     */

    public Cursor fetchAllEntries() {
        /*
        return database.query(
                DATABASE_TABLE, 
                new String[] { ROWID, MAC_ADDRESS, LATITUDE, LONGITUDE, FREQUENCY, SIGNAL}, 
                null,
                null, 
                null,
                null, 
                null);
         */
        
        // sql must NOT be ';' terminated
        String sql = "SELECT "+ ROWID + ", " + MAC_ADDRESS + ", " + LATITUDE + ", " + LONGITUDE + ", " 
                + NETWORK_NAME + ", " + CAPABILITIES + ", " + FREQUENCY + ", " + SIGNAL
                + " FROM " + DATABASE_TABLE;
        return database.rawQuery(sql, null);
    }
    
    public boolean hasEntry(String macAddr, String latitude, String longitude, int frequency, int signal) {
        String sql = "SELECT "+ ROWID + ", " + MAC_ADDRESS + ", " + LATITUDE + ", " + LONGITUDE + ", " 
                + NETWORK_NAME + ", " + CAPABILITIES + ", "+ FREQUENCY + ", " + SIGNAL + " "
                + "FROM " + DATABASE_TABLE + " "
                + "WHERE " + MAC_ADDRESS + " = ? AND "
                + LATITUDE + " = ? AND "
                + LONGITUDE + " = ? AND "
                + FREQUENCY + " = ? AND "
                + SIGNAL + " = ? ";
        String args[] = {macAddr, latitude, longitude, String.valueOf(frequency), String.valueOf(signal)};
        Cursor cursor = database.rawQuery(sql, args);
        if (cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }
    

    
    /**
     * Return a Cursor positioned at the defined todo
     */

    public Cursor fetchOneEntry(long rowId) throws SQLException {
        /*
        Cursor cursor = database.query(
                true, 
                DATABASE_TABLE, 
                new String[] {ROWID, LATITUDE, LONGITUDE, FREQUENCY, SIGNAL },
                ROWID + "=" + rowId, 
                null, 
                null, 
                null, 
                null, 
                null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
        */
        
        // sql must NOT be ';' terminated
        String sql = "SELECT "+ ROWID + ", " + MAC_ADDRESS + ", " + LATITUDE + ", " + LONGITUDE + ", " 
                + NETWORK_NAME + ", " + CAPABILITIES + ", " + FREQUENCY + ", " + SIGNAL + " "
                + "FROM " + DATABASE_TABLE + " "
                + "WHERE " + ROWID + " = " + rowId;
        return database.rawQuery(sql, null);
    }

    private ContentValues createContentValues(String macAddr, String latitude, String longitude, 
            String networkName, String capabilities, int frequency, int signal) {
        ContentValues values = new ContentValues();
        values.put(MAC_ADDRESS, macAddr);
        values.put(LATITUDE, latitude);
        values.put(LONGITUDE, longitude);
        values.put(NETWORK_NAME, networkName);
        values.put(CAPABILITIES, capabilities);
        values.put(FREQUENCY, frequency);
        values.put(SIGNAL, signal);
        return values;
    }
}
