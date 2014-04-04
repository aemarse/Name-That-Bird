package com.alexmarse.namethatbird;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class NTBSelectPlaylistType extends Activity {
	
	public final static int LOCATION = 1;
	public final static int SPECIES = 2;
	
	public final static String PLAYLIST_TYPE = "";
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ntbselect_playlist_type);
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Get the Intent
		Intent intent = getIntent();
		Log.e("onCreate", "NTBSelectPlaylist Activity launched!");
		
	}

	// BUTTON CLICK HANDLER
	public void onButtonClick(View v) {
    	
    	// Figure out which button was clicked and launch the proper activity
    	switch(v.getId()) {
    	case R.id.b_location:
    		
    		// Set up the Intent
    		Intent intent = new Intent(this, NTBSelectPlaylist.class);
    		
    		// Add the selected playlist_type id to the intent
    		intent.putExtra(PLAYLIST_TYPE, LOCATION);
    		
    		// Start the SelectLocation Activity
        	startActivity(intent);
        	
    		break;
    		
    	case R.id.b_species:
    		
    		// Toast pop up message saying that this is future functionality
    		
    		
    		break;
    		
    	default:
    		break;
    	}
    }
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ntbselect_playlist_type, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
