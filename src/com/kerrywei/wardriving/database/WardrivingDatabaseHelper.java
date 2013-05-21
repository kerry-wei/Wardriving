package com.kerrywei.wardriving.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WardrivingDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "WAPInfo";

    private static final int DATABASE_VERSION = 1;
    
    private static final String CREATE_DATABASE = 
            "CREATE TABLE " + DATABASE_NAME 
            + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "macAddress TEXT NOT NULL, "
            + "networkName TEXT NOT NULL, "
            + "capabilities TEXT NOT NULL, "
            + "latitude REAL NOT NULL, "
            + "longitude REAL NOT NULL, "
            + "frequency INTEGER NOT NULL, "
            + "signal INTEGER NOT NULL);";
    
    public WardrivingDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase database) {
        try {
            database.execSQL(CREATE_DATABASE);
        } catch (SQLException e) {
            Log.d(WardrivingDatabaseHelper.class.getName(), "Failed to create database");
            Log.d(WardrivingDatabaseHelper.class.getName(), e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        String message = "Update " + DATABASE_NAME + " from version " + oldVersion + " to version" + newVersion;
        Log.v(WardrivingDatabaseHelper.class.getName(), message);
        
        database.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
        onCreate(database);
    }
}

