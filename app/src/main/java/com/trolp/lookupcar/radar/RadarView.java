package com.trolp.lookupcar.radar;

import com.trolp.lookupcar.MessageListener;
import com.trolp.lookupcar.recorder.Recorder;
import com.trolp.lookupcar.recorder.Recorder.ParkData;
import com.trolp.lookupcar.tracker.Tracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationManager;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class RadarView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
	private boolean pause = true;
    private boolean surfaceCreated = false;
	private Thread drawThread = null;
	private Object lock = new Object();

	private RadarCircle circle1;
	private RadarCircle circle2;
	private RadarSweeper sweeper;
	private MessageListener listener;

	public RadarView(Context context) {
		super(context);
		init();
	}

	public RadarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RadarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    public MessageListener getListener() {
        return listener;
    }

	private void init() {
		this.getHolder().addCallback(this);
		this.setWillNotDraw(false);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        int x = (this.getLeft() + this.getRight()) / 2 - this.getLeft();
        int y = (this.getTop() + this.getBottom()) / 2 - this.getTop();

        double maxRad = Math.min(x, y) - 10;
        if (circle1 == null)
            circle1 = new RadarCircle(x, y, maxRad);
        if (circle2 == null)
            circle2 = new RadarCircle(x, y, maxRad, 30);
        if (sweeper == null)
            sweeper = new RadarSweeper(x, y);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        circle1 = null;
        circle2 = null;
        sweeper = null;
	}

	public void pause() {
        if(!pause) {
            pause = true;
            Tracker.getTracker().off();
            boolean retry = true;
            while (retry) {
                try {
                    drawThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            circle1.init();
            circle2.init();
            drawThread = null;
            getListener().onNewMessage("", "");
        }
	}

	public void resume() {
        if(pause) {
            Tracker.getTracker().on();
            pause = false;
            drawThread = new Thread(this);
            drawThread.start();
        }
	}

	@Override
	public void run() {
		while(!pause) {
			if(this.getHolder().getSurface().isValid()) {
				synchronized (this.getHolder()) {
					postInvalidate();
				}
			}
			sleep(70);
		}
	}

	private void sleep(int ms) {
		synchronized (lock) {
			try {
				lock.wait(ms);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if(this.getHolder().getSurface().isValid()) {
			super.onDraw(canvas);
			circle1.grow(canvas);
			circle2.grow(canvas);
			String msg = sweeper.sweep(canvas, (float) circle1.getMaxRadius());
            if(!pause && getListener() != null)
                getListener().onNewMessage("Scanning..." ,msg);
		}
	}
}
