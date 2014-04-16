package com.alexmarse.namethatbird;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.alexmarse.namethatbird.helperclasses.RequestData;

public class LessonTest extends Activity implements OnClickListener {

	// Lesson number that was selected
	int lesson;
	
	// Base and query url for NTB API
	public final static String baseUrl = "http://namethatbird.org/";
	public final static String lessonUrl = baseUrl + "api/v1/lessons/";
	public final static String soundUrl = baseUrl + "api/v1/sounds/";
	public final static String truthUrl = baseUrl + "api/v1/truth/?sound=";
	public final static String mediaUrl = baseUrl + "media/";
	String queryUrl;
	
	// Base url for streaming audio from XC API
	public final static String baseUrlXc = "http://xeno-canto.org/";
	public final static String downloadUrlXc = "/download";
	
	// Holder for lesson and sound JSON objects
	JSONObject lessonJson = null;
	JSONObject soundJson = null;
	JSONObject truthJson = null;
	
	// Holders for the sounds and truths JSON arrays
	JSONArray sounds = null;
	JSONArray truths = null;
	
	// Holder for sound data
	int xcId;
	int speciesId;
	int fs;
	
	// Holder for ground truth onset_loc data
	double[] onsetLocs = null;
	
	// The number of ground truth onsets in the current sound
	int numOnsets;
	
	// Tags for accessing JSON data
	private static final String TAG_SOUNDS = "sounds";
	private static final String TAG_LESSON_ID = "id";
	private static final String TAG_PLAYLIST_ID = "playlist";
	private static final String TAG_XC_ID = "xc_id";
	private static final String TAG_SPECIES = "species";
	private static final String TAG_FS = "fs";
	private static final String TAG_ONSET_LOC = "onset_loc";
	private static final String TAG_RESULTS = "results";
	
	// Drawing stuff
	WaveformPanel wp;  
	FrameLayout frm;
	int screenWidth;
	int screenHeight;
	
	// Gesture stuff
	GestureDetector gestureDetector;
	
	// Keeps track of which sound we are on
	int currSnd;
	
	// MediaPlayer object
	MediaPlayer player = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Instantiate MediaPlayer object
		player = new MediaPlayer();
		
		// Get the Intent
		Intent intent = getIntent();
		Log.e("onCreate: ", "LessonTest Activity launched!");
		
		// Get the screen dimensions
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		setScreenWidth(size.x);
		setScreenHeight(size.y);
		
		// Get the bundle of data from the intent
		Bundle bundle = intent.getBundleExtra(NTBSelectLesson.EXTRA);
		lesson = bundle.getInt("ntb_lesson");
		
		// Query NTB API to get the ids for the sounds associated with the current lesson
		getLessonData();
		
		// Query NTB API to get the xcId, speciesId, and fs of the current sound
		currSnd = 0;
		getSoundData();
		
		// Query NTB API to get the onsetLocs (truth) of the current sound
		getTruthData();
		
		setContentView(R.layout.activity_lesson_test);

		// Show the Up button in the action bar.
		setupActionBar();
		
		// Get the media player set up
		ImageButton pButt = (ImageButton) findViewById(R.id.b_play);
//		pButt.setOnClickListener(this);
		player.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
//				Log.e("onPrepared: ", "player prepped");
			}
		});
		preparePlayer();
		
	}
	
	private void preparePlayer() {
	    if (player == null) {
	        player = new MediaPlayer();
	    }
	    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
	    
	    try {
			player.setDataSource(baseUrlXc + xcId + downloadUrlXc);
			player.prepareAsync();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// next
	public void onClickNext(View v) {
		if (currSnd < sounds.length() - 1) {
			
			// Increment sound counter
			currSnd ++;
			
			// Get the sound data for the next sound
			getSoundData();
			
			// Get the truth data for the next sound
			getTruthData();
			
			player.reset();
			
			try {
				player.setDataSource(baseUrlXc + xcId + downloadUrlXc);
				player.prepareAsync();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	// next
	public void onClickPrev(View v) {
		if (currSnd > 0) {
			
			// Decrement sound counter
			currSnd --;
			
			// Get the sound data for the previous sound
			getSoundData();
			
			// Get the truth data for the previous sound
			getTruthData();
			
			player.reset();
			
			try {
				player.setDataSource(baseUrlXc + xcId + downloadUrlXc);
				player.prepareAsync();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	// play
	public void onClickPlay(View v) {
//		Log.e("play", "play please");
		if (player != null) {
			if (!(player.isPlaying())) {
				player.start();
			}
		}
	}

	// play
	public void onClickPause(View v) {
		
		if (player != null) {
			if (player.isPlaying()) {
				player.pause();
//				Log.e("isPlaying: ", String.valueOf(player.isPlaying()));
			}
		}
	}
	
	// BUTTON HANDLERS
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
	
	// Query NTB API to get the ids of the sounds for the selected lesson
	public void getLessonData() {
		
		// Form the query url
		queryUrl = lessonUrl + lesson;
		
		// HttpRequest to get lesson data from NTB API
		try {
			lessonJson = new JSONParse().execute().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Decode the JSON
		decodeLessonJSON();
		
	}
	
	// Query NTB API to get the xc_id, fs, and species of the current sound
	public void getSoundData() {
		
		// Form the query url
		int currSndId;
		try {
			currSndId = sounds.getInt(currSnd);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			currSndId = 0;
			e1.printStackTrace();
		}
		queryUrl = soundUrl + currSndId;
		
		// HttpRequest to get sound data from NTB API
		try {
			soundJson = new JSONParse().execute().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Decode the JSON
		decodeSoundJSON();
		
	}
	
	// Query NTB API to get the truth (onset_locs) of the current sound
	public void getTruthData() {
		
		// Form the query url
		int currSndId;
		try {
			currSndId = sounds.getInt(currSnd);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			currSndId = 0;
			e1.printStackTrace();
		}
		
		queryUrl = truthUrl + currSndId;
		
		// HttpRequest to get truth data from NTB API
		try {
			truthJson = new JSONParse().execute().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Decode the JSON
		decodeTruthJSON();
		
	}
	
	// Decodes JSON String
	public void decodeLessonJSON() {
		
		try {
			sounds = lessonJson.getJSONArray(TAG_SOUNDS);
			Log.e("sounds", sounds.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	// Decodes JSON String
	public void decodeSoundJSON() {
		
		try {
			xcId = soundJson.getInt(TAG_XC_ID);
			speciesId = soundJson.getInt(TAG_SPECIES);
			fs = soundJson.getInt(TAG_FS);
			Log.e("xcId", String.valueOf(xcId));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	// Decodes JSON String
	public void decodeTruthJSON() {
		
		try {
			JSONArray truths = truthJson.getJSONArray(TAG_RESULTS);
			JSONObject truth = new JSONObject();
			
			onsetLocs = new double[truths.length()];
			
			for (int i = 0; i < truths.length(); i++) {
				truth = truths.getJSONObject(i);
				onsetLocs[i] = truth.getDouble(TAG_ONSET_LOC);
//				Log.e("onsetLocs: ", String.valueOf(onsetLocs[i]));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	 
	
	// GETTER AND SETTER METHODS
	
	public int getScreenWidth() {
		return screenWidth;
	}
	
	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}
	
	public int getScreenHeight() {
		return screenHeight;
	}
	
	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}
	
	// Class for JSON parsing
	private class JSONParse extends AsyncTask<String, String, JSONObject> {

		private ProgressDialog pDialog;
		private String dMessage = "Setting up your first sound...";
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(LessonTest.this);
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
			JSONObject json = null;
			json = reqData.getJSONFromUrl(queryUrl);
			
			return json;
			
		}
		
		@Override
		protected void onPostExecute(JSONObject json) {
			pDialog.dismiss();
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
		getMenuInflater().inflate(R.menu.lesson_test, menu);
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
