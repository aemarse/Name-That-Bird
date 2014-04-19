package com.alexmarse.namethatbird;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
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
import android.os.CountDownTimer;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;

import com.alexmarse.namethatbird.helperclasses.ListViewSetup;
import com.alexmarse.namethatbird.helperclasses.RequestData;

public class LessonTest extends Activity implements OnClickListener {

	GridView gridView;
	ListViewSetup gridSetup;
	
	// Lesson number that was selected
	int lesson;
	
	// The current sound (in NTB API terms)
	int currSndId;
	
	// The user, for now
	int USER = 1;
	
	// Base and query url for NTB API
	public final static String baseUrl = "http://namethatbird.org/";
	public final static String lessonUrl = baseUrl + "api/v1/lessons/";
	public final static String soundUrl = baseUrl + "api/v1/sounds/";
	public final static String truthUrl = baseUrl + "api/v1/truth/?sound=";
	public final static String speciesUrl = baseUrl + "api/v1/species/";
	public final static String annotationsUrl = baseUrl + "api/v1/annotations/";
	public final static String mediaUrl = baseUrl + "media/";
	public final static String datExt = ".dat";
	String queryUrl;
	
	// Base url for streaming audio from XC API
	public final static String baseUrlXc = "http://xeno-canto.org/";
	public final static String downloadUrlXc = "/download";
	
	// Holder for dat file url
	String datUrl;
	
	// Holder for lesson and sound JSON objects
	JSONObject lessonJson = null;
	JSONObject soundJson = null;
	JSONObject truthJson = null;
	JSONObject speciesJson = null;
	JSONObject lessonSpeciesJson = null;
	
	// Holders for the sounds and truths JSON arrays
	JSONArray sounds = null;
	JSONArray truths = null;
	JSONArray lessonSpecies = null;
	
	// Holder for sound data
	int xcId;
	int speciesId;
	int fs;
	
	// Holder for species data
	String engName;
	String[] engNames;
	
	// Holder for ground truth onset_loc data
	double[] onsetLocs = null;
	
	// The location of the onset
	int onsetSamp;
	
	// Onset and offset location of the annotation in terms of seconds in original audio
	double waveOnset;
	double waveOffset;
	
	// The number of seconds before and after each onset plot
	double numOffsetSecs = 0.1;
	float secsPerPx;
	int startSamp;
	int endSamp;
	
	// Number of samps in original file that each samp in waveform file represents
	int numWaveformSamps = 256;
	
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
	private static final String TAG_ENG_NAME = "eng_name";
	private static final String TAG_RESULTS = "results";
	
	// JSON POST TAGS
	private static final String POST_SOUND = "sound";
	private static final String POST_USER = "user";
	private static final String POST_WAVE_ONSET = "wave_onset";
	private static final String POST_WAVE_OFFSET = "wave_offset";
	private static final String POST_SPECIES = "species";
	
	// Drawing stuff
	WaveformPanel wp;  
	FrameLayout frm;
	int screenWidth;
	int screenHeight;
	
	// Gesture detector for the overall view
	GestureDetector gestureDetector;
	
	// Gesture detector for the buttons
	GestureDetector buttGestureDetector;
	
	// Keeps track of which sound we are on
	int currSnd;
	
	// Keeps track of which onset we are on
	int currOnset;
	
	// Holds the normalized audiowaveform array
	float[] normalized = null;
	
	// MediaPlayer object
	MediaPlayer player = null;
	
	// MediaPlayer start and end samples, milliseconds
	double playerStartMs;
	double playerEndMs;
	
	// Number of buttons (annotations) that have been created (for a particular sound)
	int numButts;
	
	// Tag associated with buttons
	int BUTT_TAG = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lesson_test);
		
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
		
		// Query NTB API to download the waveform file for the current sound
		normalized = getWaveformFile();
		
		// Query NTB API to get the onsetLocs (truth) of the current sound
		currOnset = 0;
		getTruthData();
		
		// Get the first onset
		float[] onset = getOnset(normalized);
		
		// Set up the waveform drawing surface and add it to the current view
		drawWaveform(onset);
		
		// Create a Gesture Detector for the view w/ listener class
//		gestureDetector = new GestureDetector(this, new GestureListener());
		
		// Create a Gesture Detector for the buttons w/ listener class
		buttGestureDetector = new GestureDetector(this, new ButtonGestureListener());
		
//		setContentView(R.layout.activity_lesson_test);

		// Show the Up button in the action bar.
		setupActionBar();
		
		// Get the media player set up
		ImageButton pButt = (ImageButton) findViewById(R.id.b_play);
//		pButt.setOnClickListener(this);
		player.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
//				mp.start();
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
			
			// Reset the button counter
			numButts = 0;
			
			// Get the sound data for the next sound
			getSoundData();
			
			// Download the waveform file for the next sound
			float[] normalized = getWaveformFile();
			
			// Get the truth data for the next sound
			getTruthData();
			
			// Reset the onset counter
			currOnset = 0;
			
			// Get the first onset
			float[] onset = getOnset(normalized);
			
			// Set up the waveform drawing surface for the next sound and add it to the current view
			drawWaveform(onset);
			
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
			
			// Reset the onset counter
			currOnset = 0;
			
			// Reset the button counter
			numButts = 0;
			
			// Get the sound data for the previous sound
			getSoundData();
			
			// Download the waveform file for the next sound
			float[] normalized = getWaveformFile();
			
			// Get the truth data for the previous sound
			getTruthData();
			
			// Get the first onset
			float[] onset = getOnset(normalized);
			
			// Set up the waveform drawing surface for the next sound and add it to the current view
			drawWaveform(onset);
			
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
				player.seekTo((int)playerStartMs);
				player.start();
				
				CountDownTimer timer = new CountDownTimer((long) (playerEndMs-playerStartMs), 10) {

				    @Override
				    public void onTick(long millisUntilFinished) {
				       // Nothing to do
				    }

				    @Override
				    public void onFinish() {
				        if (player.isPlaying()) {
				             player.pause();
				        }
				    }
				};
				timer.start(); 
				
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
	
	public void setCurrentPos() {
		player.seekTo((int)playerStartMs);
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
		
		// Query NTB API to get the engName[] from the species id of each species in the current lesson
		getLessonSpeciesData();
		
	}
	
	// Query NTB API to get the xc_id, fs, and species of the current sound
	public void getSoundData() {
		
		// Form the query url
//		int currSndId;
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
		
		// Get the species data for the current sound
		getSpeciesData();
		
	}
	
	// Query NTB API to get the eng_name of the current sound's species
	public void getLessonSpeciesData() {
		
		engNames = new String[lessonSpecies.length()];
		
		// Loop through the current lesson's species array
		for (int i = 0; i < lessonSpecies.length(); i++) {
			
			// Form the query url for the current species
			try {
				queryUrl = speciesUrl + lessonSpecies.getInt(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Get the current species' json data
			try {
				lessonSpeciesJson = new JSONParse().execute().get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			// Decode the JSON
//			decodeSpeciesJSON();
			
			try {
				engNames[i] = lessonSpeciesJson.getString(TAG_ENG_NAME);
				Log.e("engNames", engNames[i]);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		
		// Set up the species name grid view
		setupSpeciesGrid();
		
	}
	
	// Get species data
	public void getSpeciesData() {
		
		// Form the query url for the current sound
		queryUrl = speciesUrl + speciesId;
		
		// HttpRequest to get species data from NTB API
		try {
			speciesJson = new JSONParse().execute().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Decode the JSON
		decodeSpeciesJSON();
		
	}
	
	// Query NTB API to get the truth (onset_locs) of the current sound
	public void getTruthData() {
		
		// Form the query url
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
			lessonSpecies = lessonJson.getJSONArray(TAG_SPECIES);
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
			
			// Also set the datUrl
			datUrl = mediaUrl + xcId + datExt;
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	// Decodes JSON String
	public void decodeSpeciesJSON() {
		
		try {
			engName = speciesJson.getString(TAG_ENG_NAME);
			Log.e("eng_name", engName);
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
	
	// Get onset
	public float[] getOnset(float[] normalized) {
		
		// Num of secs that each pixel-sample represents
		secsPerPx = (float)numWaveformSamps/(float)fs;
		Log.e("secsPerPx", String.valueOf(secsPerPx));
		
		// Sample marker that the given onset starts on
		Log.e("currOnset", String.valueOf(currOnset));
		Log.e("onsetLocs.length", String.valueOf(onsetLocs.length));
		float onsetSec = (float) onsetLocs[currOnset];
		Log.e("onsetSec", String.valueOf(onsetSec));
//		float onsetSec = (float) 1.5;
		onsetSamp = (int) Math.floor(onsetSec/secsPerPx);
		Log.e("onsetSamp", String.valueOf(onsetSamp));
		
		// Figure out the number of offset samps
		int numOffsetSamps = (int) Math.floor(numOffsetSecs/secsPerPx);
		
		// Figure out the start and end onset locations
		startSamp = onsetSamp - numOffsetSamps;
		endSamp = onsetSamp + numOffsetSamps;
		
		if (startSamp < 0) {
			startSamp = 0;
		} 
		else if (startSamp > normalized.length) {
			startSamp = normalized.length - numOffsetSamps;
		}
		
		if (endSamp > normalized.length) {
			endSamp = normalized.length-1;
		}
		
		// Set the player start and end position
		playerStartMs = startSamp*secsPerPx*1000;
		playerEndMs = endSamp*secsPerPx*1000;
		
		// Now, take only the portion of the normalized array that we want
		float[] onset = new float[numOffsetSamps*2+1];
		System.arraycopy(normalized, startSamp, onset, 0, endSamp-startSamp);
		
		// Return the onset array
		return onset;
	}
	
	// Draw waveform
	public void drawWaveform(float[] onset) {
		wp = new WaveformPanel(this, onset);
//		setContentView(R.layout.activity_lesson_test);
		frm = (FrameLayout)findViewById(R.id.frameLayout);
		frm.addView(wp);
		
//		// Also add a button to the view
//		Button onsetButt = new Button(this);
//		frm.addView(onsetButt);
////		onsetButt.setX(onsetSamp*((screenWidth/2 + 10)/onset.length));
//		onsetButt.setX(10 + onsetSamp*((screenWidth-20)/onset.length));
//		onsetButt.setY(wp.getWaveformHeight() + 20);
//
//		// Change the width, height, x loc, y loc, and background color of the button
//		FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) onsetButt.getLayoutParams();
//		params.width = 20;
//		params.height = 200;
//		onsetButt.setLayoutParams(params);
		
		gestureDetector = new GestureDetector(this, new GestureListener());

	}
	
	// Download waveform file for current sound from NTB API
	public float[] getWaveformFile() {
		
		// Read in dat file
		List<Byte> wavData = new ArrayList<Byte>();
		try {
			wavData = new DownloadFile().execute().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.e("wavData.size()", String.valueOf(wavData.size()));
		
		
		byte[] byteArray = new byte[wavData.size()];
		int i = 0;

		for (Byte b : wavData) {
		    byteArray[i++] = (b != null ? b : 0);
		}
		
		// Decode bytes to floats
		float[] floatArray = decodeBytes(byteArray);
		Log.e("floatArray", String.valueOf(floatArray.length));
		
		// Normalize data
		float[] normalized = normalizer(floatArray);
		
		return normalized;
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
	
	// WAVEFORM STUFF
	
	public float[] decodeBytes (byte byteArray[]) { 
		float floatArray[] = new float[byteArray.length/4]; 

		// wrap the source byte array to the byte buffer 
		ByteBuffer byteBuf = ByteBuffer.wrap(byteArray); 

		// create a view of the byte buffer as a float buffer 
		FloatBuffer floatBuf = byteBuf.asFloatBuffer(); 

		// now get the data from the float buffer to the float array, 
		// it is actually retrieved from the byte array 
		floatBuf.get (floatArray); 

		return floatArray; 
	} 
	
	public float[] normalizer(float[] raw) {
		
		float[] normalized = new float[raw.length];
		
		float min = raw[0];
		float max = raw[0];
		float curr;
		
		for (int i = 0; i < raw.length; i++) {
			
			curr = raw[i];
			
			if (curr > max) {
				max = curr;
			}
			
			if (curr < min) {
				min = curr;
			}
			
		}
		
		float normMax = 1;
		
		// Puts everything in range [0,1]
		for (int i = 0; i < raw.length; i++) {
			normalized[i] = ((normMax * (raw[i] - min))/(max - min));
		}
		
		return normalized;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		return gestureDetector.onTouchEvent(e);

	}
	
	private class DownloadFile extends AsyncTask<Void, Void, List<Byte>> {

		@Override
		protected List<Byte> doInBackground(Void... arg0) {
			
			int count;
			List<Byte> wavData = new ArrayList<Byte>();
			
			try {
				URL url = new URL(datUrl);
				URLConnection connection = url.openConnection();
				connection.connect();
				int fileLength = connection.getContentLength();
//				Log.e("length of file", Integer.toString(fileLength));
				
				InputStream input = new BufferedInputStream(url.openStream(), 8192);
				
				byte data[] = new byte[1024];
				
				while ((count = input.read(data)) != -1) {
					for (int i = 0; i < data.length; i++) {
						wavData.add(data[i]);
					}
				}
				
			} catch (Exception e) {
		            Log.e("Error: ", e.getMessage());
		    }
			
			return wavData;
			
		}
	}
	
	
	// Gesture Detector class for the View
	private class GestureListener extends GestureDetector.SimpleOnGestureListener {

		private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
		
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
		
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			float x = e.getX() - 20;
			float y = e.getY()/3;
			
			boolean result = false;
			
			if ((x >= wp.getxMin() && x <= wp.getxMax())
					&& (y >= wp.getyMin() && y <= wp.getWaveformHeight() * 3)) {
				
				// Create a button
				createButton(x, y);
				
				result = true;
			}
			
			return result;
		
		}
		
		@Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            
            float x = e1.getX();
			float y = e1.getY();
            
			if ((x >= wp.getxMin() && x <= wp.getxMax())
					&& (y >= wp.getyMin() && y <= wp.getWaveformHeight() * 3)) {
			
	            try {
	                float diffY = e2.getY() - e1.getY();
	                float diffX = e2.getX() - e1.getX();
	                if (Math.abs(diffX) > Math.abs(diffY)) {
	                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
	                        if (diffX > 0) {
	                            onSwipeRight();
	                        } else {
	                            onSwipeLeft();
	                        }
	                    }
	                } else {
	                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
	                        if (diffY > 0) {
	                            onSwipeBottom();
	                        } else {
	                            onSwipeTop();
	                        }
	                    }
	                }
	                result = true;
	                
	            } catch (Exception exception) {
	                exception.printStackTrace();
	            }
			}
			
            return result;
        }
		
		public void onSwipeRight() {
			// Decrement the onset counter
			if (currOnset > 0) {
				currOnset --;
				
				// Get the current onset
				float[] onset = getOnset(normalized);
				
				// Set up the waveform drawing surface and add it to the current view
				drawWaveform(onset);
			}
	    }

	    public void onSwipeLeft() {
	    	// Increment the onset counter
	    	if (currOnset < onsetLocs.length - 1) {
	    		currOnset ++;
	    		
	    		// Get the current onset
				float[] onset = getOnset(normalized);
				
				// Set up the waveform drawing surface and add it to the current view
				drawWaveform(onset);
	    		
	    	}
	    }

	    public void onSwipeTop() {
	    	Log.e("swipe", "top");
	    }

	    public void onSwipeBottom() {
	    	Log.e("swipe", "bottom");
	    }
		
	    public void createButton(float x, float y) {
	    
	    	// Increment the button counter
	    	numButts += 1;
	    	
	    	// Add a new button to the view
			Button onsetButt = new Button(wp.getContext());
			frm.addView(onsetButt);
			onsetButt.setX(x);
			onsetButt.setY(wp.getyMin());

			// Set the parameters of the button
			FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) onsetButt.getLayoutParams();
			params.width = 50;
			params.height = (int) wp.getWaveformHeight()*2 - (int) wp.yPad;
			onsetButt.setLayoutParams(params);
			
			// Set the tag/id for the button
			onsetButt.setId(numButts-1);
			onsetButt.setTag(BUTT_TAG);
			
			// Set waveOnset and waveOffset params of the current button
			float onsetSamp = ((onsetButt.getX() - params.width/2) - wp.getxMin()) / wp.getPxPerSamp();
			float offsetSamp = ((onsetButt.getX() + params.width/2) - wp.getxMin()) / wp.getPxPerSamp();
			waveOnset = (onsetSamp * secsPerPx) + (startSamp * secsPerPx);
			waveOffset = (offsetSamp * secsPerPx) + (startSamp * secsPerPx);
			
			Log.i("wp.pxPerSamp", String.valueOf(wp.getPxPerSamp()));
			Log.i("onsetSamp", String.valueOf(onsetSamp));
			Log.i("offsetSamp", String.valueOf(offsetSamp));
			Log.i("secsPerPx", String.valueOf(secsPerPx));
			Log.i("waveOnset", String.valueOf(waveOnset));
			Log.i("waveOffset", String.valueOf(waveOffset));
			
			Log.e("id: ", String.valueOf(onsetButt.getId()));
			Log.e("tag: ", String.valueOf(onsetButt.getTag()));
			
			// Set the onClickListener for the button
			onsetButt.setOnClickListener(buttClicked);
			
//			// Set the onTouchListener for the button
//			onsetButt.setOnTouchListener(new View.OnTouchListener() {
//				
//				@Override
//				public boolean onTouch(View v, MotionEvent event) {
//					// TODO Auto-generated method stub
//					return buttGestureDetector.onTouchEvent(event);
//				}
//			});	
	    	
	    }
	    
	    OnClickListener buttClicked = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Log the id value
				Log.e("id: ", String.valueOf(v.getId()));
			}
	    	
	    };
	    
	}
	
	// Gesture Detector class for Annotation buttons
	private class ButtonGestureListener extends GestureDetector.SimpleOnGestureListener {
		
		@Override
		public boolean onDown(MotionEvent e) {
		    // TODO Auto-generated method stub
			Log.i("Test", "On Down");
		    return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
		        float velocityY) {
		    // TODO Auto-generated method stub
		    Log.i("Test", "On Fling");
		    return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		    // TODO Auto-generated method stub
			
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
		        float distanceY) {
		    // TODO Auto-generated method stub
		    return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		    // TODO Auto-generated method stub

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
		    // TODO Auto-generated method stub
			Log.e("Test", "onSingleTapUp");
		    return false;
		}
		
	}
	
	public void setupSpeciesGrid() {
		
		// Instantiate the GridView
		gridView = (GridView) findViewById(R.id.gv_lesson_test);
		
		for (int i = 0; i < engNames.length; i++) {
			Log.i("engNamesssss", engNames[i]);
		}
		
		Log.i("gridView context", gridView.getContext().toString());
		
		// Instantiate the gridSetup object
		gridSetup = new ListViewSetup(this, engNames, gridView);
		gridView = gridSetup.setupGrid();
		
		// Listen for list item clicks
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position,
					long id) {
				Log.e("item clicked: ", Integer.toString((int)id));
				
				// Get all the data we need
				int sound = currSndId;
				int user = USER;
//				double waveOnset = waveOnset;
//				double waveOffset = waveOffset;
				int species = 1;
				try {
					species = lessonSpecies.getInt((int)id);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// Make the JSON Object for HTTP POST
				JSONObject obj = dataToJSON(sound, user, waveOnset, waveOffset, species);
				
				// Call AsyncTask to do HTTP POST
				new JsonPost().execute(obj);
				
			}
			
		});
		
	}
	
	// Pack data into a JSON Object
	public JSONObject dataToJSON(int sound, int user, double waveOnset, double waveOffset, int species) {
		
		// Initialize JSONObject
		JSONObject obj = new JSONObject();
		
		// Put data into the object
    	try {
    		obj.put(POST_SOUND, sound);
    		obj.put(POST_USER, user);
    		obj.put(POST_WAVE_ONSET, waveOnset);
    		obj.put(POST_WAVE_OFFSET, waveOffset);
    		obj.put(POST_SPECIES, species);
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
		
		return obj;
	}
	
	// Make HTTP POST
	public void postToDb(JSONObject obj) {
		
		try {
    		
    		// Create a default HTTP client
    		HttpClient client = new DefaultHttpClient();
    		
    		// Create HTTP post object
    		HttpPost poster = new HttpPost(annotationsUrl);
    		
    		// Get a string from the JSON Object
    		String jsonString = obj.toString();
    		Log.e("json string: ", jsonString);
    		
    		// Set the HTTP entity
    		StringEntity entity = new StringEntity(jsonString);
    		poster.setEntity(entity);
    		
    		// Set the header
    		poster.setHeader("Accept", "application/json");
            poster.setHeader("Content-type","application/json");
    		
    		// Execute the post
    		HttpResponse response = client.execute((HttpUriRequest)poster);
    		
    		// Get entity from the response
    		HttpEntity entityHttp = response.getEntity();
    		
    		// Log the response (should be JSON string data)
    		if (entity != null) {
                Log.e("result: ", EntityUtils.toString(entityHttp));
            }
    		
    	} catch(Exception e) {
    		Log.e("post error: ", "Unable to post to database");
    		e.printStackTrace();
    	}
		
	}
	
	// Class for JSON posting
	public class JsonPost extends AsyncTask<JSONObject, Void, String> {

		private ProgressDialog pDialog;
		private String dMessage = "Posting annotation to database...";
		
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
		protected String doInBackground(JSONObject... obj) {
			
			postToDb(obj[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(String str) {
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
