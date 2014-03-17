package com.alexmarse.namethatbird;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.alexmarse.namethatbird.helperclasses.ListViewSetup;

public class NTBSelectLesson extends Activity {

	GridView gridView;
	ListViewSetup gridSetup;
	
	JSONArray lessons = null;
	
	String playlistId;
	String playlistName;
	String lessonsString;
	
	public final static String EXTRA = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ntbselect_lesson);
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Get the Intent
		Intent intent = getIntent();
		Log.e("onCreate: ", "NTBSelectLesson Activity launched!");
		
		Bundle bundle = intent.getBundleExtra(NTBSelectPlaylist.EXTRA);
		playlistId = bundle.getString("playlist_id");
		playlistName = bundle.getString("playlist_name");
		lessonsString = bundle.getString("lessons");
		
		// Get the lessons
		try {
			lessons = new JSONArray(lessonsString);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Set up the grid view with the lessons
		setupGridView(lessons);
		
	}

	public void setupGridView(JSONArray lessons) {
		
		// Get the grid view from the layout
		gridView = (GridView)findViewById(R.id.gv_lessons);
		
		// Dummy data for now
		int numLessons = lessons.length();
		String[] lessonNames = new String[numLessons];
		for(int i = 0; i < numLessons; i++) {
			lessonNames[i] = String.valueOf(i+1);
		}
		
		gridSetup = new ListViewSetup(this, lessonNames, gridView);
		gridView = gridSetup.setupGrid();
		
		// Listen for list item clicks
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position,
					long id) {
				// Do something when item clicked
				Log.e("item clicked: ", Integer.toString((int)id));
				launchIntent((int)id);
			}
			
		});
		
	}
	
	// Intent launcher
	public void launchIntent(int id) {
		
		// Set up the Intent
		Intent intent = new Intent(this, NTBCompleteLesson.class);
		
		// Get the lesson number of the item that was clicked
		int lesson = 0;
		try {
			lesson = lessons.getInt(id);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Pack the data into a bundle
		Bundle bundle = new Bundle();
		bundle.putString("playlist_id", playlistId);
		bundle.putString("playlist_name", playlistName);
		bundle.putInt("lesson", lesson);
		intent.putExtra(EXTRA, bundle);
		
		// Start the activity
		startActivity(intent);
		
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ntbselect_lesson, menu);
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
