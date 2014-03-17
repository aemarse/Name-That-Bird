package com.alexmarse.namethatbird;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.alexmarse.namethatbird.helperclasses.ListViewSetup;
import com.alexmarse.namethatbird.helperclasses.RequestData;

public class NTBSelectPlaylist extends Activity {

	ListView listView;
	ListViewSetup listSetup;
	
	public final static String baseUrl = "http://172.31.99.211:8000/api/v1/playlists/?playlist_type=";
	String queryUrl;
	
	JSONObject json = null;
	
	// JSON node names
	private static final String TAG_COUNT = "count";
	private static final String TAG_NEXT = "next";
	private static final String TAG_PREVIOUS = "previous";
	private static final String TAG_RESULTS = "results";
	private static final String TAG_ID = "id";
	private static final String TAG_PLAYLIST_NAME = "playlist_name";
	private static final String TAG_PLAYLIST_TYPE = "playlist_type";
	private static final String TAG_ADDED_DATE = "added_date";
	private static final String TAG_LESSONS = "lessons";
	
	JSONArray playlists = null;
	JSONObject playlist = null;
	String[] playlistIds;
	String[] playlistNames;
	String[] playlistTypes;
	
	public final static String EXTRA = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ntbselect_playlist);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Log.e("onCreate", "NTBSelectPlaylist Activity launched!");
		
		// Get the intent and playlist_type
		Intent intent = getIntent();
		int playlistType = intent.getIntExtra(NTBSelectPlaylistType.PLAYLIST_TYPE, 0);
		
		// Form the query url
		queryUrl = baseUrl + playlistType;
		
		// HttpRequest to get playlistData from database depending on playlistType
		new JSONParse().execute();
		
	}
	
	
	// Helper function that sets up the ListView
	public void setupListView() {
		
		listView = (ListView)findViewById(R.id.lv_locations);
		
		listSetup = new ListViewSetup(this, playlistNames, listView);
		listView = listSetup.setupList();
		
		// Listen for list item clicks
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position,
					long id) {
				launchIntent((int)id);
			}
			
		});
		
	}
	
	// Intent launcher
	public void launchIntent(int id) {
		
		// Set up the Intent
		Intent intent = new Intent(this, NTBSelectLesson.class);
		
		// Bundle the data for the selected playlist
		Bundle bundle = new Bundle();
		bundle.putString("playlist_id", playlistIds[id]);
		bundle.putString("playlist_name", playlistNames[id]);
		
		// Also get the JSON Array of lessons for the selected playlist
		try {
			JSONObject lessonsObject = playlists.getJSONObject(id);
			JSONArray lessons = lessonsObject.getJSONArray(TAG_LESSONS);
			bundle.putString(TAG_LESSONS, lessons.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Pass along the bundle of data
		intent.putExtra(EXTRA, bundle);
		
		// Start the new activity
		startActivity(intent);
		
	}
	
	// Decodes JSON String
	public void decodeJSON() {
		
		try {
			playlists = json.getJSONArray(TAG_RESULTS);
			
			playlistIds = new String[playlists.length()];
			playlistNames = new String[playlists.length()];
			playlistTypes = new String[playlists.length()];
			
			playlist = new JSONObject();
			
			for (int i = 0; i < playlists.length(); i++) {
				
				playlist = playlists.getJSONObject(i);
//				Log.e("playlist", playlist.toString());
				
				playlistIds[i] = playlist.getString(TAG_ID);
				playlistNames[i] = playlist.getString(TAG_PLAYLIST_NAME);
				playlistTypes[i] = playlist.getString(TAG_PLAYLIST_TYPE);
				
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	// Class for JSON parsing
	private class JSONParse extends AsyncTask<String, String, JSONObject> {

		private ProgressDialog pDialog;
		private String dMessage = "Getting playlists...";
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(NTBSelectPlaylist.this);
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
			decodeJSON();
			
			// Set up the list view of playlist names 
			setupListView();
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
		getMenuInflater().inflate(R.menu.ntbselect_location, menu);
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
