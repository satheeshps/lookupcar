package com.trolp.lookupcar.view;

import com.trolp.lookupcar.ScalePageTransformer;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

public class VerticalViewPager1 extends ViewPager {
	public class OnSwipeTouchListener implements OnTouchListener {
		private final class GestureListener extends SimpleOnGestureListener {
			private static final int SWIPE_THRESHOLD = 100;
			private static final int SWIPE_VELOCITY_THRESHOLD = 100;

			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				onTouch(e);
				return true;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				boolean result = false;
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
				} catch (Exception exception) {
					exception.printStackTrace();
				}
				return result;
			}
		}

		private GestureDetector gestureDetector = null;

		public OnSwipeTouchListener(Context context) {
			gestureDetector = new GestureDetector(context, new GestureListener());
		}

		public boolean onTouch(final View v, final MotionEvent event) {
			return gestureDetector.onTouchEvent(event);
		}

		public void onTouch(MotionEvent e) {
		}

		public void onSwipeRight() {
		}

		public void onSwipeLeft() {
		}

		public void onSwipeTop() {
		}

		public void onSwipeBottom() {
		}
	}
	public VerticalViewPager1(Context context) {
		this(context, null);
	}

	public VerticalViewPager1(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPageTransformer(true, new ScalePageTransformer());
		setOnTouchListener(new OnSwipeTouchListener(context) {
			@Override
			public void onSwipeTop() {
				super.onSwipeTop();
				if(getCurrentItem() < getChildCount())
					setCurrentItem(getCurrentItem() + 1, true);
			}

			@Override
			public void onSwipeBottom() {
				super.onSwipeBottom();
				if(getCurrentItem() > 0)
					setCurrentItem(getCurrentItem() - 1, true);
			}
		});
	}
}
