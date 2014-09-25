package com.stylingandroid.polish;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import eu.andlabs.tutorial.animatedgifs.decoder.GifDecoder;

public class MainActivity extends Activity {
	private static final String TAG = "Polish";
	final Handler mHandler = new Handler();
	private ScrollView mScroll;
	private TextView mText;
	private View mContent;
	private ImageView mImage;
	private boolean mIsPlayingGif = false;
	private GifDecoder mGifDecoder;
	private Bitmap mTmpBitmap;

	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			if (mTmpBitmap != null && !mTmpBitmap.isRecycled()) {
				mImage.setImageBitmap(mTmpBitmap);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mScroll = (ScrollView) findViewById(R.id.scroll);
		mText = (TextView) findViewById(R.id.text);
		mContent = findViewById(R.id.content);
		mImage = (ImageView) findViewById(R.id.image);
		final ViewTreeObserver vto = mText.getViewTreeObserver();
		if (vto != null) {
			vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					ViewTreeObserver vto = mText.getViewTreeObserver();
					if (vto != null) {
						vto.removeOnGlobalLayoutListener(this);
					}
					mScroll.computeScroll();
					Animator anim = ObjectAnimator.ofInt(mScroll, "scrollY", 0, mContent.getBottom());
					anim.setDuration(30000);
					anim.setInterpolator(new LinearInterpolator());
					anim.start();
				}
			});
		}
	}

	@Override
	protected void onPause() {
		stopRendering();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		InputStream is = null;
		try {
			is = getAssets().open("animation.gif");
			playGif(is);
		} catch (IOException e) {
			Log.e(TAG, "Error loading animated GIF", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
					Log.w(TAG, "Error closing stream", e);
				}
			}
		}
	}

	private void playGif(InputStream stream) {
		mGifDecoder = new GifDecoder();
		mGifDecoder.read(stream);

		mIsPlayingGif = true;

		new Thread(new Runnable() {
			public void run() {
				final int n = mGifDecoder.getFrameCount();
				final int ntimes = mGifDecoder.getLoopCount();
				int repetitionCounter = 0;
				do {
					for (int i = 0; i < n; i++) {
						mTmpBitmap = mGifDecoder.getFrame(i);
						int t = mGifDecoder.getDelay(i);
						mHandler.post(mUpdateResults);
						try {
							Thread.sleep(t);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if (ntimes != 0) {
						repetitionCounter++;
					}
				} while (mIsPlayingGif && (repetitionCounter <= ntimes));
			}
		}).start();
	}

	public void stopRendering() {
		mIsPlayingGif = false;
	}
}
