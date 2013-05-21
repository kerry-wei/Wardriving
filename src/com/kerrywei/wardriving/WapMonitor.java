package com.kerrywei.wardriving;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

// rename this class to 'ConnectionMonitor' or 'WapMonitor'

public class WapMonitor extends BroadcastReceiver {
    WifiManager wifiManager;
    WardrivingActivity wardrivingActivity;
    
    //DatabaseAdapter databaseAdapter;
    
    final String DEBUG = "Wardriving DEBUG";
    
    public WapMonitor(WifiManager _wifiManager, WardrivingActivity _wardrivingActivity) {
        super();
        wifiManager = _wifiManager;
        wardrivingActivity = _wardrivingActivity;
        //databaseAdapter = _databaseAdapter;
        
        //databaseAdapter = new DatabaseAdapter(wardrivingActivity); 
        //databaseAdapter.open();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String currentLatitude = "unknown", currentLongitude = "unknown";
        String action = intent.getAction();
        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                wardrivingActivity.updateConnectionInfo("CONNECTED");
            } else if (networkInfo.getState().equals(NetworkInfo.State.CONNECTING)) {
                wardrivingActivity.updateConnectionInfo("CONNECTING...");
            } else if (networkInfo.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                wardrivingActivity.updateConnectionInfo("DISCONNECTED");
            } else if (networkInfo.getState().equals(NetworkInfo.State.DISCONNECTING)) {
                wardrivingActivity.updateConnectionInfo("DISCONNECTING");
            } else if (networkInfo.getState().equals(NetworkInfo.State.SUSPENDED)) {
                wardrivingActivity.updateConnectionInfo("SUSPENDED");
            } else {
                wardrivingActivity.updateConnectionInfo("UNKNOWN");
            }
        } else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            List<ScanResult> scanResults = wifiManager.getScanResults();
            for (ScanResult result : scanResults) {
                //prefsEditor.putString(String.valueOf(System.currentTimeMillis()), result.BSSID);
                writeToFile(context, result.BSSID, result.frequency, result.level);
                currentLatitude = getCurrentLatitude(context);
                currentLongitude = getCurrentLongitude(context);
                wardrivingActivity.updateDatabase(result.BSSID, currentLatitude, currentLongitude, 
                        result.SSID, result.capabilities, result.frequency, result.level);
            }
            //prefsEditor.commit();
            
            // update last scan in the main page:
            Date now = new Date();
            String lastTimeScan = DateFormat.getDateTimeInstance().format(now);
            wardrivingActivity.updateLastScan(lastTimeScan);
            
            wardrivingActivity.stopWapMonitorAndLocationUpdate();
            wardrivingActivity.setSleep(true);
            
            boolean shouldWakeUp = wardrivingActivity.shouldWakeUp();
            if (shouldWakeUp) {
                
                
                SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
                long sleepInterval = 0;
                
                try {
                    sleepInterval = Long.valueOf(app_preferences.getString("settings_sleepInterval", "30000")) * 1000;
                } catch (NumberFormatException e) {
                    Editor sleepIntervalPref = app_preferences.edit();
                    sleepIntervalPref.putString("settings_sleepInterval", "180");
                    sleepIntervalPref.commit();
                }
                
                Handler handler = new Handler(); 
                handler.postDelayed(new Runnable() {
                     public void run() { 
                          wardrivingActivity.startWapMonitorAndLocationUpdate();
                          wardrivingActivity.setWakeUp(true);
                     } 
                }, sleepInterval);
                
                wardrivingActivity.setWakeUp(false);
            }
            
        } else {
            Log.v(DEBUG, "EORROR. Check WapMonitor.");
        }
        
        
    }
    
    /*
    private void updateDatabase(String macAddr, String latitude, String longitude, long frequency, long signal) {
        long rowID = databaseAdapter.insertEntry(macAddr, latitude, longitude, frequency, signal);
        if (rowID == -1) {
            Log.v(DEBUG, "Failed to insert " + macAddr + " into the database.");
        }
    }
    */
    
    
    private void writeToFile(Context context, String macAddr, int frequency, int signal) {
        try {
            File root = Environment.getExternalStorageDirectory();
            if (root.canWrite()){
                File file = new File(root, context.getResources().getString(R.string.logName));
                FileWriter fileWriter = new FileWriter(file, true);
                BufferedWriter out = new BufferedWriter(fileWriter);
                
                // write to the log file:
                String logEntry = "";
                
                logEntry += String.valueOf(System.currentTimeMillis() / 1000) + ": ";
                logEntry += "WiFi, ";
                logEntry += getCurrentLocation(context) + ", ";
                logEntry += macAddr + ", ";
                logEntry += String.valueOf(frequency) + ", ";
                logEntry += String.valueOf(signal) + "\n";
                
                out.write(logEntry);
                out.close();
            }
        } catch (IOException e) {
            Log.v(DEBUG, "Could not write file " + e.getMessage());
            Toast.makeText(context, "Failed to write to the log. Please try again later.", Toast.LENGTH_LONG).show();
        }
    }
    
    private String getCurrentLatitude(Context context) {
        String currentLatitude = "Unknown";
        String prefName = context.getResources().getString(R.string.currentLocSharedPref);
        SharedPreferences sharedMacAddrList = context.getSharedPreferences(prefName, Context.MODE_WORLD_READABLE);
        
        String latitudeKey = context.getResources().getString(R.string.currentLatitude);
        currentLatitude = sharedMacAddrList.getString(latitudeKey, "Unknown");
        
        return currentLatitude;
    }
    
    private String getCurrentLongitude(Context context) {
        String currentLongitude = "Unknown";
        String prefName = context.getResources().getString(R.string.currentLocSharedPref);
        SharedPreferences sharedMacAddrList = context.getSharedPreferences(prefName, Context.MODE_WORLD_READABLE);
        
        String longitudeKey = context.getResources().getString(R.string.currentLongitude);
        currentLongitude = sharedMacAddrList.getString(longitudeKey, "Unknown");
        
        return currentLongitude;
    }
    
    private String getCurrentLocation(Context context) {
        String currentLoc = getCurrentLatitude(context) + ", " + getCurrentLongitude(context);
        return currentLoc;
    }
    
    

}
