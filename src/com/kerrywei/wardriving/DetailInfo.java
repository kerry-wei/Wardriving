package com.kerrywei.wardriving;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.kerrywei.wardriving.database.DatabaseAdapter;

public class DetailInfo extends Activity {
    final String DEBUG = "Wardriving DEBUG";
    Long rowID;
    DatabaseAdapter databaseAdapter;
    TextView macAddress;
    TextView networkName;
    TextView capabilities;
    TextView location;
    TextView frequency;
    TextView signal;
    
    
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.detail_view);
        
        databaseAdapter = new DatabaseAdapter(this);
        databaseAdapter.open();
        
        macAddress = (TextView) findViewById(R.id.detailViewMacAddressValue);
        networkName = (TextView) findViewById(R.id.detailViewSSIDValue);
        capabilities = (TextView) findViewById(R.id.detailViewCapabilitiesValue);
        location = (TextView) findViewById(R.id.detailViewLocationValue);
        frequency = (TextView) findViewById(R.id.detailViewFrequencyValue);
        signal = (TextView) findViewById(R.id.detailViewSignalValue);
        
        Bundle extras = getIntent().getExtras();
        rowID = null;
        rowID = (bundle == null) ? null : (Long) bundle.getSerializable(DatabaseAdapter.ROWID);
        if (extras != null) {
            rowID = extras.getLong(DatabaseAdapter.ROWID);
        }
        fillData();
        
    }
    
    void fillData() {
        if (rowID != -1) {
            Cursor cursor = databaseAdapter.fetchOneEntry(rowID);
            if (cursor.moveToFirst()) {
                macAddress.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.MAC_ADDRESS)));
                networkName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.NETWORK_NAME)));
                capabilities.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.CAPABILITIES)));
                String locationInfo, currentLat, currentLog;
                currentLat = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.LATITUDE));
                currentLog = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.LONGITUDE));
                locationInfo = currentLat + ", " + currentLog;
                location.setText(locationInfo);
                frequency.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseAdapter.FREQUENCY))));
                signal.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseAdapter.SIGNAL))));
                cursor.close();
            } 
        } else {
            Log.d(DEBUG, "RowID = -1. Unable to fetch detailed info!");
        }
    }

}
