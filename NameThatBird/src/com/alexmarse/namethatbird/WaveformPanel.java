package com.alexmarse.namethatbird;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class WaveformPanel extends SurfaceView implements SurfaceHolder.Callback {

	WaveformThread _thread;
	float[] _wavData;
	
	int viewHeight;
	int viewWidth;
	float waveformHeight;
	float waveformWidth;
	float xPad;
	float yPad;
	float xAxis;
	float xMin;
	float xMax;
	float yMin;
	float yMax;
	
	public WaveformPanel(Context context, float[] wavData) {
		super(context);
		getHolder().addCallback(this);
		_wavData = wavData;
		
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		
		// Init paint object
		Paint paint = new Paint(); 
		paint.setStyle(Paint.Style.FILL); 
		
		// Setup canvas 
		paint.setColor(Color.WHITE); // make canvas yellow
		canvas.drawPaint(paint); // another way to do this is to use: // canvas.drawColor(Color.WHITE); 
		
		// Make the waveform blue
		paint.setColor(Color.BLUE);
		
		// Draw waveform
//		drawWaveformOld(canvas, paint);
		drawWaveform(canvas, paint);
		
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
		// Get the height and width of our available screen real estate
		viewHeight = this.getHeight();
		viewWidth = this.getWidth();
		Log.e("size", "width: " + Integer.toString(viewWidth) + " height: " + Integer.toString(viewHeight));
		
		// Set up the waveform drawing area bounds
		setBounds(); 
		
		// Get the thread going
		setWillNotDraw(false);
		_thread = new WaveformThread(getHolder(), this);
		_thread.setRunning(true);
		_thread.start();
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
		try {
			_thread.setRunning(false);
			_thread.join();
		} catch (InterruptedException e) {
		}
		
	}
	
	public void setBounds() {
		
		// Set the waveform height and width, taking into account some padding
		xPad = 10;
		yPad = 20;
		
		waveformHeight = viewHeight/4 - yPad; // For now, make it take up half the height of the view minus some padding
		waveformWidth = viewWidth - xPad; // Take up the width of the view minus some padding
		
		Log.e("size", "waveform width: " + String.valueOf(waveformWidth) + " waveform height: " + String.valueOf(waveformHeight));
		
		// Set the y position of the x-axis
		xAxis = waveformHeight;
		Log.e("size", "xAxis: " + String.valueOf(xAxis));
		
		// Set the left and right bounds
		xMin = xPad;
		xMax = viewWidth - xPad;
		Log.e("size", "left bound: " + String.valueOf(xMin) + " right bound: " + String.valueOf(xMax));
		
		// Set the bottom and top bounds
		yMax = waveformHeight;
		yMin = yPad;
		Log.e("size", "top bound: " + String.valueOf(yMin) + " bottom bound: " + String.valueOf(yMax));
		
		Log.e("setBounds:", "bounds set!");
		
	}
	
	// Drawing waveform
	public void drawWaveform(Canvas canvas, Paint paint) {
		
		float x1;
		float y1;
		float x2;
		float y2;
		
		// Loop through waveform array
		for (int i = 0; i < _wavData.length; i++) {
			
			// Start drawing on the second sample
			if (i == 0) {
				continue;
			}
			
			// Set x and y points
			x1 = i - 1 + xMin;
			y1 = _wavData[i-1]*waveformHeight;
			x2 = i + xMin;
			y2 = _wavData[i]*waveformHeight;

			// Draw the upper and lower lines
			canvas.drawLine(x1, y1+yMin, x2, y2+yMin, paint);
			canvas.drawLine(x1, yMax-y1+xAxis, x2, yMax-y2+xAxis, paint);
			
//			canvas.drawLine(x1, xAxis-y1, x2, xAxis-y2, paint); // these work more or less, but they're flipped
//			canvas.drawLine(x1, xAxis+y1, x2, xAxis+y2, paint);
			
//			canvas.drawLine(x1, yMax-y1, x2, yMax-y2, paint);
			
		}
		
	}
	
	// Draw waveform old version
	public void drawWaveformOld(Canvas canvas, Paint paint) {
		float yScale = 150;
		float xScale = 50;
		float yOffset = 250;
		float xOffset = 20;
		float waveformWidth = 50;
		float x1;
		float y1;
		float x2;
		float y2;
		
		// Loop through waveform array
		for (int i = 0; i < _wavData.length; i++) {
			
			// Start drawing on the second sample
			if (i == 0) {
				continue;
			}
			
			// Set x and y points
			x1 = i - 1 + xOffset;
			y1 = _wavData[i-1] * yScale;
			x2 = i + xOffset;
			y2 = _wavData[i] * yScale;
			
			// Draw the upper and lower lines
			canvas.drawLine(x1, yOffset+y1, x2, yOffset+y2, paint);
			canvas.drawLine(x1, yOffset-y1, x2, yOffset-y2, paint);
			
		}
		
//		canvas.drawLines(_wavData, paint);
//		canvas.drawCircle(20, 20, 15, paint); // draw blue circle with antialiasing turned on 
//		paint.setAntiAlias(true); 
//		paint.setColor(Color.BLUE); 
//		canvas.drawCircle(60, 20, 15, paint);
	}
	
	public class WaveformThread extends Thread {
		
		private SurfaceHolder _surfaceHolder;
		private WaveformPanel _panel;
		private boolean _run = false;
		
		public WaveformThread(SurfaceHolder surfaceHolder, WaveformPanel panel) {
			_surfaceHolder = surfaceHolder;
			_panel = panel;
		}
		
		public void setRunning(boolean run) {
			_run = run;
		}
		
		@Override
		public void run() {
			Canvas c;
			
			while (_run) {
				c = null;
				
				try {
					c = _surfaceHolder.lockCanvas(null);
					synchronized (_surfaceHolder) {
						postInvalidate();
					}
				} finally {
					if (c != null) {
						_surfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}

	}

}
