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
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
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
