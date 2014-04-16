package com.alexmarse.namethatbird;

import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.alexmarse.namethatbird.adapter.TabsPagerAdapter;
import com.alexmarse.namethatbird.helperclasses.RequestData;

public class NTBCompleteLesson extends FragmentActivity implements ActionBar.TabListener {

	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
	private ActionBar actionBar;
	
	// Tab titles
	private String[] tabs = {"Practice", "Test"};
	
	// Lesson number that was selected
	int lesson;
	
	// Base and query url
	public final static String baseUrl = "http://namethatbird.org/api/v1/lessons/";
	String queryUrl;
	
	// JSON object for holding lesson content
	JSONObject json = null;
	
	// Initialize holders for the JSON components
	JSONArray sounds = null;
	JSONArray truths = null;
	
	// Tags for accessing JSON data
	private static final String TAG_SOUNDS = "sounds";
	private static final String TAG_LESSON_ID = "id";
	private static final String TAG_PLAYLIST_ID = "playlist";
	private static final String TAG_RESULTS = "results";
	
	// Bundle for passing data to fragments
	Bundle bundle;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ntbcomplete_lesson);
		// Show the Up button in the action bar.
//		setupActionBar();
		
		// Get the Intent
		Intent intent = getIntent();
		Log.e("onCreate: ", "NTBCompleteLesson Activity launched!");
		
		// Get the bundle of data from the intent
		bundle = intent.getBundleExtra(NTBSelectLesson.EXTRA);
		lesson = bundle.getInt("ntb_lesson");
		
		// Get the data for the selected lesson
		getLessonData();
		
		// Get the data for each sound in the selected lesson
//		getSoundTruthData();
		
		// Initialization
		viewPager = (ViewPager)findViewById(R.id.pager);
		actionBar = getActionBar();
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), bundle);
		
		viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// Add the tabs
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(this));
		}
		
		/**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
 
        @Override
        public void onPageSelected(int position) {
            // on changing the page
            // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }
 
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
 
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
		
	}
	
	// Lesson data
	public void getLessonData() {
		
		// Form the query url
		queryUrl = baseUrl + lesson;
		
		// HttpRequest to get playlistData from database depending on playlistType
		try {
			json = new JSONParse().execute().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		decodeLessonJSON();
		
	}
	
	// Sounds, ground truth data
	public void getSoundTruthData() {
		
		// Form the query url
		queryUrl = baseUrl + lesson;
		
		// HttpRequest to get playlistData from database depending on playlistType
		try {
			json = new JSONParse().execute().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// Decodes JSON String
	public void decodeLessonJSON() {
		
		try {
			sounds = json.getJSONArray(TAG_SOUNDS);
			bundle.putString(TAG_SOUNDS, sounds.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}	
	
	// Decodes JSON String
	public void decodeSoundTruthJSON() {
		
		try {
			truths = json.getJSONArray(TAG_RESULTS);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}	
	
	// Class for JSON parsing
	private class JSONParse extends AsyncTask<String, String, JSONObject> {

		private ProgressDialog pDialog;
		private String dMessage = "Setting up your lesson...";
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(NTBCompleteLesson.this);
			pDialog.setMessage(dMessage);
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		@Override
		protected JSONObject doInBackground(String... args) {
			
			// Instantiate a RequestData object
			RequestData reqData = new RequestData();
			
			// Get JSON from the url
			json = reqData.getJSONFromUrl(queryUrl);
			
			return json;
			
		}
		
		@Override
		protected void onPostExecute(JSONObject json) {
			pDialog.dismiss();
			
			// Decode the JSON Object into its elements
//			decodeJSON();
			
		}
		
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
		getMenuInflater().inflate(R.menu.ntbcomplete_lesson, menu);
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


	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}


	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// on tab selected, show respected fragment view
		viewPager.setCurrentItem(tab.getPosition());
	}


	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

}
