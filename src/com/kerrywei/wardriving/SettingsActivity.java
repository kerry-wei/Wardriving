package com.kerrywei.wardriving;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;

// Referenced the following page: http://developer.android.com/reference/android/preference/PreferenceActivity.html

public class SettingsActivity extends PreferenceActivity {
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu from resources:
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();
        return super.onKeyDown(keyCode, event);
    }
    
}
