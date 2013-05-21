package com.kerrywei.wardriving;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kerrywei.wardriving.database.DatabaseAdapter;

public class WardrivingActivity extends Activity {
    static final int LIST_VIEW = 0;
    static final int SETTINGS = 1;
    static final int DELETE_FILE = 2;
    
    
    final String DEBUG = "Wardriving DEBUG";
    LocationManager locationManager;
    LocationListener locationListener;
    
    BroadcastReceiver broadcastReceiver;
    
    DatabaseAdapter databaseAdapter;
    
    WifiManager wifiManager;
    Button uploadLog;
    Button startOrStopApp;
    boolean appStarted = false;
    boolean isSleep = false;
    
    boolean shouldWakeUp = true;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                updateLocation(location);
                
                // update current location:
                updateSharedLocationInfo(location);
            }
            
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            
            public void onProviderEnabled(String provider) {}
            
            public void onProviderDisabled(String provider) {}
        };
        
        // setup wifi manager and get currently available wifi connections:
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        
        
        
        // setup buttons:
        uploadLog = (Button)findViewById(R.id.button1);
        uploadLog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //((Button)v).setText("hello");
                uploadLogFile();
            }
        });
        
        databaseAdapter = new DatabaseAdapter(this);
        databaseAdapter.open();
        broadcastReceiver = new WapMonitor(wifiManager, this);
        
        startOrStopApp = (Button) findViewById(R.id.button2);
        startOrStopApp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startOrStopApp();
            }
        });
        
        startApp();
    }
    
    public void updateDatabase(String macAddr, String latitude, String longitude, 
            String networkName, String capabilities, int frequency, int signal) {
        if (databaseAdapter.hasEntry(macAddr, latitude, longitude, frequency, signal)) {
            Log.d(DEBUG, "Skip duplicate.");
        } else {
            long rowID = databaseAdapter.insertEntry(macAddr, latitude, longitude, networkName, capabilities, frequency, signal);
            if (rowID == -1) {
                Log.v(DEBUG, "Failed to insert " + macAddr + " into the database.");
            } 
        }
        
    }
    
    /*
    @Override
    public void onStop() {
        unregisterReceiver(receiver);
    }
    */
    
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
        case DELETE_FILE:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to delete the log file?")
                   .setCancelable(false)
                   .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                            Log.v(DEBUG, "Yes clicked");
                            deleteLogFile();
                       }
                   })
                   .setNegativeButton("No", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                       }
                   });
            dialog = builder.create();
            return dialog;
        default:
            return null;
        }
    }
    
    
    
    private void setupWapMonitor() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }
    
    private boolean updateLocation(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String newLocation = "Latitude: " + latitude + ", \nLongitude: " + longitude;
            
            TextView locationTextView = (TextView) findViewById(R.id.main_location);
            locationTextView.setText(newLocation);
            
            updateSharedLocationInfo(location);
            
            return true;
        } else {
            return false;
        }
    }
    
    private void updateSharedLocationInfo(Location location) {
        SharedPreferences sharedMacAddrList = getSharedPreferences(getResources().getString(R.string.currentLocSharedPref), 
                Context.MODE_WORLD_READABLE);
        Editor prefsEditor = sharedMacAddrList.edit();
        
        String currentLatitude = String.valueOf(location.getLatitude());
        String currentLongitude = String.valueOf(location.getLongitude());
        
        prefsEditor.putString(getResources().getString(R.string.currentLatitude), currentLatitude);
        prefsEditor.putString(getResources().getString(R.string.currentLongitude), currentLongitude);
        prefsEditor.commit();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate a menu from menu resources:
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuItem_main:
                //Toast.makeText(this, "You pressed the main menu item!", Toast.LENGTH_LONG).show();
                
                return true;
            case R.id.menuItem_lastScan:
                //Toast.makeText(this, "You pressed the last scan menu item!", Toast.LENGTH_LONG).show();
                
                Intent listViewIntent = new Intent(getApplicationContext(), ListViewActivity.class);
                startActivityForResult(listViewIntent, LIST_VIEW);
                //startActivity(listViewIntent);
                return true;
            case R.id.menuItem_settings:
                //Toast.makeText(this, "You pressed the settings menu item!", Toast.LENGTH_LONG).show();
                
                Intent myIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(myIntent, SETTINGS);
                //startActivity(myIntent);
                
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LIST_VIEW:
                Log.v(DEBUG, "returned from list view.");
                return;
            case SETTINGS:
                Log.v(DEBUG, "returned from settings.");
                return;
            default:
                return;
        }
    }
    
    public void updateConnectionInfo(String connection) {
        TextView connectionTextView = (TextView)findViewById(R.id.main_connection_status);
        connectionTextView.setText(connection);
    }
    
    public void updateLastScan(String lastTimeScan) {
        TextView lastScan = (TextView)findViewById(R.id.main_last_time_scan);
        lastScan.setText(lastTimeScan);
    }
    
    private String getLogFileContent() {
        String content = "";
        File file = new File(getLogPath());
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
            BufferedReader buf = new BufferedReader(new InputStreamReader(fileInputStream));
            String readString = new String();
            
            readString = buf.readLine();
            
            while (readString != null) {
                content += readString + "\n";
                readString = buf.readLine();
            }
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return content;
    }
    
    private void uploadLogFile() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        
        // check connection before upload the log:
        if (networkInfo.isConnected()) {
            HttpClient httpclient = new DefaultHttpClient();   
            HttpPost httppost = new HttpPost("http://blow.cs.uwaterloo.ca/cgi-bin/cs456_a1_submit.py"); 
            try {
                /*
                File file = new File(getLogPath());
                FileInputStream fileInputStream;
                fileInputStream = new FileInputStream(file);
                InputStreamEntity reqEntity = new InputStreamEntity(fileInputStream, file.length());
                httppost.setEntity(reqEntity);
                */
                
                SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
                String userID = app_preferences.getString("settings_userid", getResources().getString(R.string.defaultUserId));
                String content = getLogFileContent();
                
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);   
                nameValuePairs.add(new BasicNameValuePair("uid", userID));
                nameValuePairs.add(new BasicNameValuePair("trace", content));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        
                HttpResponse response = httpclient.execute(httppost);
                Log.d(DEBUG, "response from HttpClient: " + response.getEntity());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            showDialog(DELETE_FILE);
        } else {
            Toast.makeText(this, "No network available. Please try again later.", Toast.LENGTH_LONG).show();
        }
        
        
    }
    
    private String getLogPath() {
        String logPath = Environment.getExternalStorageDirectory() 
                + "/" 
                + getResources().getString(R.string.logName);
        return logPath;
    }
    
    private void deleteLogFile() {
        File file = new File(getLogPath());
        if (file.delete()) {
            Toast.makeText(this, "Log file is successfully deleted.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to delete the log file. Please try again later.", Toast.LENGTH_LONG).show();
        }
    }
    
    private void startApp() {
        appStarted = true;
        startOrStopApp.setText(R.string.main_stop_scan);
        
        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        
        // display the last known location and setup WapMonitor:
        if (!updateLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))) {
            if (updateLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))) {
                setupWapMonitor();
            } else {
                Log.v(DEBUG, "No provider available");
            }
        } else {
            setupWapMonitor();
        }
    }
    
    private void stopApp() {
        appStarted = false;
        startOrStopApp.setText(R.string.main_start_scan);
        
        // stop WapMonitor and location updates:
        stopWapMonitorAndLocationUpdate();
    }
    
    public void stopWapMonitorAndLocationUpdate() {
        locationManager.removeUpdates(locationListener);
        if (!isSleep) {
            unregisterReceiver(broadcastReceiver);
        }
    }
    
    private void startOrStopApp() {
        if (appStarted) {
            stopApp();
        } else {
            startApp();
        }
    }
    
    public void startWapMonitorAndLocationUpdate() { 
        if (appStarted) {
            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            
            if (!updateLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))) {
                if (updateLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))) {
                    setupWapMonitor();
                } else {
                    Log.v(DEBUG, "No provider available");
                }
            } else {
                setupWapMonitor();
            }
        }
    }
    
    public void setSleep(boolean status) {
        isSleep = status;
    }
    
    public boolean isSleep() {
        return isSleep;
    }
    
    public void setWakeUp(boolean newStatus) {
        shouldWakeUp = newStatus;
    }
    
    public boolean shouldWakeUp() {
        return shouldWakeUp;
    }
}


