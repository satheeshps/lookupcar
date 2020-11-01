package com.trolp.lookupcar;

import android.support.v4.view.ViewPager.PageTransformer;
import android.view.View;

public class ScalePageTransformer implements PageTransformer {
	public ScalePageTransformer() {
	}

	@Override
	public void transformPage(View view, float position) {
		int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
//            view.setAlpha(0);

        } else if (position <= 1) { // [-1,1]
//            view.setAlpha(1);

            // Counteract the default slide transition
//            view.setTranslationX(pageWidth * -position);

            //set Y position to swipe in from top
            float yPosition = position * pageHeight;
//            view.setTranslationY(yPosition);

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
//            view.setAlpha(0);
        }
	}
}
