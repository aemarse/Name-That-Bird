package com.alexmarse.namethatbird;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.FrameLayout;


public class WaveformDrawer extends Activity {

	String datUrl = "http://namethatbird.org/media/131693.dat";
//	String datUrl = "http://namethatbird.org/media/143593.dat";
	WaveformPanel wp;  
	FrameLayout frm;
	int screenWidth;
	int screenHeight;
	
	GestureDetector gestureDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_waveform_drawer);
		
		// Get the Intent
		Intent intent = getIntent();
		Log.e("onCreate: ", "WaveformDrawer Activity launched!");
		
		// Get the screen dimensions
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
		screenHeight = size.y;
//		Log.e("size", "width: " + Integer.toString(screenWidth) + " height: " + Integer.toString(screenHeight));	
		
		
		
		// Read in a dat file
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
		
		byte[] byteArray = new byte[wavData.size()];
		int i = 0;

		for (Byte b : wavData) {
		    byteArray[i++] = (b != null ? b : 0);
		}
		
		float[] floatArray = decodeBytes(byteArray);
		
		float[] normalized = normalizer(floatArray);
		
		wp = new WaveformPanel(this, normalized);
		setContentView(R.layout.activity_waveform_drawer);
		frm = (FrameLayout)findViewById(R.id.frameLayout);
		frm.addView(wp);
		
		gestureDetector = new GestureDetector(this, new GestureListener());
		
//		setContentView(wp);
		
	}

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
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.waveform_drawer, menu);
		return true;
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
	
	private class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
		
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			float x = e.getX();
			float y = e.getY();
			
			if ((x >= wp.getxMin() && x <= wp.getxMax())
					&& (y >= wp.getyMin() && y <= wp.getWaveformHeight() * 2)) {
				Log.e("double tap: ", "x: " + String.valueOf(x) + " y: "
						+ String.valueOf(y));
				return true;
			} else {
				return false;
			}
		
		}
		
		}

}
