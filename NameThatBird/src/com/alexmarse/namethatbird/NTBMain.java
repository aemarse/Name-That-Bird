package com.alexmarse.namethatbird;

import com.alexmarse.namethatbird.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class NTBMain extends Activity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ntbmain);
    }

    // BUTTON CLICK HANDLER
    public void onButtonClick(View v) {
    	
    	// Figure out which button was clicked and launch the proper activity
    	switch(v.getId()) {
    	case R.id.b_listen:
    		
    		// Set up the Intent
    		Intent intent = new Intent(this, NTBSelectPlaylistType.class);
    		
    		// Start the SelectPlaylistType Activity
        	startActivity(intent);
        	
    		break;
    	case R.id.b_record:
    		
    		// Toast message saying this is future functionality
    		
    		
    		break;
    	default:
    		break;
    	}
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ntbmain, menu);
        return true;
    }
    
}
