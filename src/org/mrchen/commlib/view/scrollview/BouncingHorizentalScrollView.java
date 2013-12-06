package org.mrchen.commlib.view.scrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.HorizontalScrollView;

/**
 * author:http://www.techhui.com/group/android/forum/topics/support-bounce-
 * property-for
 * 
 * @author chenjianli
 * 
 */
public class BouncingHorizentalScrollView extends HorizontalScrollView {

	public static final int GAP = 100;

	public BouncingHorizentalScrollView(Context context) {
		super(context);
		init();
	}

	public BouncingHorizentalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BouncingHorizentalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		// LayoutInflater inflater = (LayoutInflater)
		// getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// inflater.inflate(R.layout.mygallery, this);
	}

	@Override
	public void fling(int velocityX) {
		super.fling(velocityX);
		int duration = calculate(velocityX, 0);
		checkingFling(duration);
	}

	private void checkingFling(final int duration) {
		postDelayed(new Runnable() {
			@Override
			public void run() {
				int left = getScrollX();
				boolean checked = false;
				if (left < GAP) {
					smoothScrollBy(GAP - left, 0);
					checked = true;
				} else {
					View child = getChildAt(0);
					int gap = left + getWidth() - (child.getWidth() - GAP);
					if (gap > 0) {
						smoothScrollBy(-gap, 0);
						checked = true;
					}
				}
				if (checked) {
					return;
				}
				int newDuration = duration - 200;
				if (newDuration > 0) {
					checkingFling(newDuration);
				}
			}
		}, 200);
	}

	private int calculate(int velocityX, int velocityY) {
		float ppi = getContext().getResources().getDisplayMetrics().density * 160.0f;
		float mDeceleration = 9.8f // g (m/s^2)
				* 39.37f // inch/meter
				* ppi // pixels per inch
				* ViewConfiguration.getScrollFriction();
		float velocity = (float) Math.hypot(velocityX, velocityY);

		return (int) (1000 * velocity / mDeceleration); // Duration is in
														// milliseconds
	}

	public void scrollBy(int x, final int y) {
		super.scrollBy(x, y);
		postDelayed(new Runnable() {
			@Override
			public void run() {

				int left = getScrollX();
				if (left < GAP) {
					scrollTo(GAP, 0);
				} else {
					// View child = ETGallery.this.getChildAt(0);
					View child = getChildView();
					if (child != null) {
						int gap = left + getWidth() - (child.getWidth() - GAP);
						if (gap > 0) {
							// scrollTo( child.getWidth() - GAP, 0 );
							scrollBy(-gap, y);

						}
					}
				}
			}
		}, 200);
	}

	private View getChildView() {
		return null;
	}

	public void adjustLeft() {
		checkingFling(400);
	}

	public void animateToLeft() {
		postDelayed(new Runnable() {

			@Override
			public void run() {
				View child = getChildAt(0);
				scrollTo(child.getWidth(), 0);

				postDelayed(new Runnable() {

					@Override
					public void run() {
						fullScroll(View.FOCUS_LEFT);
						adjustLeft();
					}
				}, 200);
			}
		}, 200);

	}

}
