package com.kerrywei.wardriving;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.kerrywei.wardriving.database.DatabaseAdapter;

public class ListViewActivity extends ListActivity {
    final String DEBUG = "Wardriving DEBUG";
    static final int VIEW_DETAIL = 0;
    Cursor cursor;
    DatabaseAdapter databaseAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mac_address_list);
        this.getListView().setDividerHeight(2);
        databaseAdapter = new DatabaseAdapter(this);
        databaseAdapter.open();
        fillData();
        
    }
    
    private void fillData() {
        cursor = databaseAdapter.fetchAllEntries();
        startManagingCursor(cursor);

        String[] from = new String[] { DatabaseAdapter.MAC_ADDRESS };
        int[] to = new int[] { R.id.listViewEntry };

        // Now create an array adapter and set it to display using our row
        
        // DISCOURAGE! Use CursorManager and CursorLoader instead
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.listview_item, 
                cursor, 
                from, 
                to);
        setListAdapter(cursorAdapter);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, DetailInfo.class);
        i.putExtra(DatabaseAdapter.ROWID, id);
        // Activity returns an result if called with startActivityForResult
        
        startActivityForResult(i, VIEW_DETAIL);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }

    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseAdapter != null) {
            databaseAdapter.close();
        }
    }
    
}
