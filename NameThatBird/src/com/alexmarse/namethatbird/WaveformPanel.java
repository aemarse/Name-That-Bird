package com.alexmarse.namethatbird;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class WaveformPanel extends SurfaceView implements
		SurfaceHolder.Callback {

	WaveformThread _thread;
	float[] _wavData;
	float[] resampledWavData;

	int viewHeight;
	int viewWidth;
	float waveformHeight;
	float waveformWidth;
	float xPad;
	float yPad;
	float xAxis;
	float xMin;
	float xMax;
	private float yMin;
	float yMax;
	float pxPerSamp;

	float fs = 44100;
	float spp = 256;

	GestureDetector gestureDetector;

	public WaveformPanel(Context context, float[] wavData) {
		super(context);
		getHolder().addCallback(this);
		_wavData = wavData;
		gestureDetector = new GestureDetector(context, new GestureListener());
	}

	@Override
	public void onDraw(Canvas canvas) {

		// Init paint object
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);

		// Setup canvas
		paint.setColor(Color.WHITE); // make canvas yellow
		canvas.drawPaint(paint); // another way to do this is to use: //
									// canvas.drawColor(Color.WHITE);

		// Make the waveform blue
		paint.setColor(Color.BLUE);

		// Draw waveform
		// drawWaveformOld(canvas, paint);
		drawWaveform(canvas, paint);

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		// Get the height and width of our available screen real estate
		viewHeight = this.getHeight();
		viewWidth = this.getWidth();
		Log.e("size", "width: " + Integer.toString(viewWidth) + " height: "
				+ Integer.toString(viewHeight));

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

//	@Override
//	public boolean onTouchEvent(MotionEvent e) {
//		return gestureDetector.onTouchEvent(e);
//
//	}

	public void setBounds() {

		// Set the waveform height and width, taking into account some padding
		xPad = 10;
		yPad = 80;

		setWaveformHeight((int)(viewHeight / 2.5 - yPad)); // For now, make it take up half
												// the height of the view minus
												// some padding
//		setWaveformHeight(viewHeight / 4 - yPad);
		
		setWaveformWidth(viewWidth - xPad * 2); // Take up the width of the view
												// minus the l/r padding

		Log.e("size", "waveform width: " + String.valueOf(waveformWidth)
				+ " waveform height: " + String.valueOf(waveformHeight));

		// Set the y position of the x-axis
		xAxis = waveformHeight;
//		Log.e("size", "xAxis: " + String.valueOf(xAxis));

		// Set the left and right bounds
		xMin = xPad;
		xMax = viewWidth - xPad;
//		Log.e("size", "left bound: " + String.valueOf(xMin) + " right bound: "
//				+ String.valueOf(xMax));

		// Set the bottom and top bounds
		yMax = waveformHeight;
		setyMin(yPad);
//		Log.e("size", "top bound: " + String.valueOf(yMin) + " bottom bound: "
//				+ String.valueOf(yMax));

//		Log.e("setBounds:", "bounds set!");

	}

	// Resample waveform
	public void resampleWaveform() {

		// Figure out the number of pixels and samples
		int numPx = (int) waveformWidth;
		int numSamps = _wavData.length;
//		Log.e("numPx", String.valueOf(numPx));
//		Log.e("numSamps", String.valueOf(numSamps));

		pxPerSamp = (float) numPx / (float) numSamps;
//		Log.e("pxPerSamp", String.valueOf(pxPerSamp));

		// If the audio waveform has more samples than there are pixels
		if (numSamps > numPx) {

			// Initialize a new array for the resampled waveform
			float[] resamp = new float[numPx];

			// Figure out the number of samples that will be represented by each
			// pixel
			float sampsPerPx = numSamps / numPx;
			pxPerSamp = numPx / numSamps;
//			Log.e("sampsPerPx", String.valueOf(sampsPerPx));
//			Log.e("pxPerSamp", String.valueOf(pxPerSamp));

			// Loop through the samples and throw some away
			for (int i = 0; i < numSamps; i++) {

			}

		}

	}

	// Drawing waveform
	public void drawWaveform(Canvas canvas, Paint paint) {

		float x1;
		float y1;
		float x2;
		float y2;
		float prevX1;	

		// Init paint object for drawing box around waveform
		Paint p2 = new Paint();
		p2.setStyle(Paint.Style.FILL);
		p2.setColor(Color.BLACK);

		// Draw box around waveform
		canvas.drawLine(xMin, getyMin(), xMax, getyMin(), p2); // top
		canvas.drawLine(xMin, getyMin(), xMin, waveformHeight * 2, p2); // left
		canvas.drawLine(xMin, waveformHeight * 2, xMax, waveformHeight * 2, p2); // bottom
		canvas.drawLine(xMax, getyMin(), xMax, waveformHeight * 2, p2); // right

		// Draw the time grid below the waveform
		// float pxPerSec = fs/spp;
		// float numMarkers = waveformWidth/pxPerSec;
		//
		// float origNumSamps = _wavData.length * spp;
		// Log.e("origNumSamps", String.valueOf(origNumSamps));
		// float origNumSecs = origNumSamps / fs;
		// Log.e("origNumSecs", String.valueOf(origNumSecs));
		//
		// Paint p3 = new Paint();
		// p3.setStyle(Paint.Style.FILL);
		// p3.setColor(Color.RED);
		//
		// float curr = xMin+pxPerSec;
		// for (int i = 0; i < (int)numMarkers; i++) {
		// canvas.drawLine(curr, yMin+10, curr, yMin-10, p3);
		// curr += pxPerSec;
		// }

		// Set the px per samp
		pxPerSamp = waveformWidth / (float) _wavData.length;
		
		// Init value of x
		prevX1 = xMin;

		// Loop through waveform array
		for (int i = 1; i < _wavData.length; i++) {

			// Start drawing on the second sample
			// if (i == 0) {
			// continue;
			// }

			// Set x and y points
			// x1 = i - 1 + xMin;
			// y1 = _wavData[i-1]*waveformHeight;
			// x2 = i + xMin;
			// y2 = _wavData[i]*waveformHeight;

			// Set x and y points, resizing the waveform appropriately
			// WORKS, but there's a discontinuity between each sample on the x
			// axis
			// x1 = i*pxPerSamp - 1 + xMin;
			// y1 = _wavData[i-1]*waveformHeight;
			// x2 = i*pxPerSamp + xMin;
			// y2 = _wavData[i]*waveformHeight;

			// Testing this out: MOST ACCURATE SO FAR
			x1 = prevX1;
			y1 = _wavData[i - 1] * waveformHeight;
			x2 = i * pxPerSamp + xMin;
			y2 = _wavData[i] * waveformHeight;

//			Log.e("y1", String.valueOf(y1));

			// Draw the upper and lower lines
			canvas.drawLine(x1, y1 + getyMin(), x2, y2 + getyMin(), paint);
			canvas.drawLine(x1, yMax - y1 + xAxis, x2, yMax - y2 + xAxis, paint);
//			canvas.drawLine(x1, yMax*2 - y1, x2, yMax*2 - y2, paint); // this line should replace the above line properly (not yet tested)

			// canvas.drawLine(x1, xAxis-y1, x2, xAxis-y2, paint); // these work
			// more or less, but they're flipped
			// canvas.drawLine(x1, xAxis+y1, x2, xAxis+y2, paint);

			// canvas.drawLine(x1, yMax-y1, x2, yMax-y2, paint);

			// Set the previous x to ensure continuity between lines
			prevX1 = x2;

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
			y1 = _wavData[i - 1] * yScale;
			x2 = i + xOffset;
			y2 = _wavData[i] * yScale;

			// Draw the upper and lower lines of the waveform
			canvas.drawLine(x1, yOffset + y1, x2, yOffset + y2, paint);
			canvas.drawLine(x1, yOffset - y1, x2, yOffset - y2, paint);

		}

		// canvas.drawLines(_wavData, paint);
		// canvas.drawCircle(20, 20, 15, paint); // draw blue circle with
		// antialiasing turned on
		// paint.setAntiAlias(true);
		// paint.setColor(Color.BLUE);
		// canvas.drawCircle(60, 20, 15, paint);
	}

	// GETTER AND SETTER METHODS
	
	public float getyMin() {
		return yMin;
	}

	public void setyMin(float yMin) {
		this.yMin = yMin;
	}
	
	public float getxMin() {
		return xMin;
	}

	public void setxMin(float xMin) {
		this.xMin = xMin;
	}

	public float getxMax() {
		return xMax;
	}

	public void setxMax(float xMax) {
		this.xMax = xMax;
	}
	
	public float getWaveformHeight() {
		return waveformHeight;
	}
	
	public void setWaveformHeight(float waveformHeight) {
		this.waveformHeight = waveformHeight;
	}

	public float getWaveformWidth() {
		return waveformWidth;
	}
	
	public void setWaveformWidth(float waveformWidth) {
		this.waveformWidth = waveformWidth;
	}
	
	public float getPxPerSamp() {
		return pxPerSamp;
	}
	
	private class GestureListener extends
			GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			float x = e.getX();
			float y = e.getY();

			if ((x >= xMin && x <= xMax)
					&& (y >= getyMin() && y <= waveformHeight * 2)) {
				Log.e("double tap: ", "x: " + String.valueOf(x) + " y: "
						+ String.valueOf(y));
				return true;
			} else {
				return false;
			}

		}

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
