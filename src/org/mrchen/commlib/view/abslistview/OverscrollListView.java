package org.mrchen.commlib.view.abslistview;

import org.mrchen.commlib.R;
import org.mrchen.commlib.helper.LogHelper;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;

public class OverscrollListView extends ListView implements OnScrollListener, View.OnTouchListener,
		android.widget.AdapterView.OnItemSelectedListener {

	private final String TAG = "OverscrollListView";

	protected static float BREAKSPEED = 4f, ELASTICITY = 0.67f;

	public Handler mHandler = new Handler();
	public int nHeaders = 1, nFooters = 1, divHeight = 0, delay = 10;
	private int firstVis, visibleCnt, lastVis, totalItems, scrollstate;
	private boolean bounce = true, rebound = false, recalcV = false, trackballEvent = false;
	private long flingTimestamp;// 滑动时间戳
	private float velocity;// 速度
	private View measure;
	private GestureDetector gesture;// 检测手势的类

	public OverscrollListView(Context context) {
		super(context);
		initialize(context);
	}

	public OverscrollListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	public OverscrollListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		LogHelper.i(TAG, "OnScrollListener >> onScroll >> " + "f = " + firstVisibleItem + ", v = " + visibleItemCount
				+ ", t = " + totalItemCount + ", l = " + lastVis);
		firstVis = firstVisibleItem;
		visibleCnt = visibleItemCount;
		totalItems = totalItemCount;
		lastVis = firstVisibleItem + visibleItemCount;
		// LogHelper.d(TAG, "f = " + firstVisibleItem + ", v = " +
		// visibleItemCount + ", t = " + totalItemCount + ", l = "
		// + lastVis);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		LogHelper.i(TAG, "OnScrollListener >> onScrollStateChanged >> scrollState = " + getScrollState(scrollState));
		scrollstate = scrollState;
		if (scrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
			LogHelper.i(TAG, "OnScrollListener >> onScrollStateChanged >> scrollState = " + getScrollState(scrollState)
					+ " -> post");
			rebound = true;
			mHandler.postDelayed(checkListviewTopAndBottom, delay);
		}
	}

	private String getScrollState(int state) {
		switch (state) {
		case SCROLL_STATE_TOUCH_SCROLL:
			return "scroll";
		case SCROLL_STATE_FLING:
			return "fling";
		case SCROLL_STATE_IDLE:
			return "idle";
		}
		return null;
	}

	@Override
	public void onItemSelected(AdapterView<?> av, View v, int position, long id) {
		LogHelper.i(TAG, "OverscrollListView >> onItemSelected -> post");
		rebound = true;
		mHandler.postDelayed(checkListviewTopAndBottom, delay);
	}

	@Override
	public void onNothingSelected(AdapterView<?> av) {
		LogHelper.i(TAG, "OverscrollListView >> onNothingSelected -> post");
		rebound = true;
		mHandler.postDelayed(checkListviewTopAndBottom, delay);
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		LogHelper.i(TAG, "OverscrollListView >> onTrackballEvent -> post");
		trackballEvent = true;
		rebound = true;
		mHandler.postDelayed(checkListviewTopAndBottom, delay);
		return super.onTrackballEvent(event);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		LogHelper.i(TAG, "OverscrollListView >> onTouch");
		gesture.onTouchEvent(event);
		return false;
	}

	/**
	 * 手势监听器
	 * 
	 * @author chenjianli
	 * 
	 */
	private class OverScrollListViewGestureListener implements OnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			LogHelper.d(TAG, "OverScrollListViewGestureListener >> onDown");
			rebound = false;
			recalcV = false;
			velocity = 0f;
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			LogHelper.d(TAG, "OverScrollListViewGestureListener >> onFling");
			rebound = true;
			recalcV = true;
			velocity = velocityY / 25f;
			flingTimestamp = System.currentTimeMillis();
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			LogHelper.d(TAG, "OverScrollListViewGestureListener >> onLongPress");
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			LogHelper.d(TAG, "OverScrollListViewGestureListener >> onScroll");
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			LogHelper.d(TAG, "OverScrollListViewGestureListener >> onShowPress");
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			LogHelper.d(TAG, "OverScrollListViewGestureListener >> onSingleTapUp");
			rebound = true;
			recalcV = false;
			velocity = 0f;
			return false;
		}
	};

	private void initialize(Context context) {
		// 在头部和尾部添加一块空白区域
		View header = new View(context);
		int minHeight = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
				.getHeight() / 2;// 屏幕高度
		LogHelper.d(TAG, "initialize >> minHeight = " + minHeight);
		header.setMinimumHeight(minHeight);
		// header.setBackgroundColor(Color.parseColor("#ff4131"));
		ImageView logo = new ImageView(context);
		logo.setImageResource(R.drawable.icon_scroll_logo);
		logo.setScaleType(ScaleType.CENTER_INSIDE);
		logo.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, minHeight));
		addHeaderView(logo, null, false);
		// addHeaderView(header, null, false);
		addFooterView(header, null, false);
		// 设置手势监听
		gesture = new GestureDetector(new OverScrollListViewGestureListener());
		gesture.setIsLongpressEnabled(false);
		flingTimestamp = System.currentTimeMillis();
		setHeaderDividersEnabled(false);
		setFooterDividersEnabled(false);
		setOnTouchListener(this);
		setOnScrollListener(this);
		setOnItemSelectedListener(this);
	}

	public void initializeValues() {
		nHeaders = getHeaderViewsCount();
		nFooters = getFooterViewsCount();
		divHeight = getDividerHeight();
		firstVis = 0;
		visibleCnt = 0;
		lastVis = 0;
		totalItems = 0;
		scrollstate = 0;
		rebound = true;
		setSelectionFromTop(nHeaders, divHeight);
		mHandler.postDelayed(checkListviewTopAndBottom, delay);
	}

	/**
	 * Turns bouncing animation on/off.
	 * 
	 * @param bouncing
	 *            Default is true (on)
	 */
	public void setBounce(boolean bouncing) {
		bounce = bouncing;
	}

	/**
	 * Sets how fast the animation will be. Higher value means faster animation.
	 * Must be >= 1.05. Together with Elasticity <= 0.75 it will not bounce
	 * forever.
	 * 
	 * @param breakspead
	 *            Default is 4.0
	 */
	public void setBreakspeed(final float breakspeed) {
		if (Math.abs(breakspeed) >= 1.05f) {
			BREAKSPEED = Math.abs(breakspeed);
		}
	}

	/**
	 * Sets how much it will keep bouncing. Lower value means less bouncing.
	 * Must be <= 0.75. Together with Breakspeed >= 1.05 it will not bounce
	 * forever.
	 * 
	 * @param elasticity
	 *            Default is 0.67
	 */
	public void setElasticity(final float elasticity) {
		if (Math.abs(elasticity) <= 0.75f) {
			ELASTICITY = Math.abs(elasticity);
		}
	}

	/**
	 * 检查ListView位置的任务类
	 */
	public Runnable checkListviewTopAndBottom = new Runnable() {
		@Override
		public void run() {

			// 移除之前的任务
			mHandler.removeCallbacks(checkListviewTopAndBottom);

			if (trackballEvent && firstVis < nHeaders && lastVis >= totalItems) {
				trackballEvent = false;
				rebound = false;
				return;
			}

			if (rebound) {
				// 当ListView的第一个视图出现的时候，即位于顶部的HeaderView出现在视图内时
				if (firstVis < nHeaders) {
					// hack to avoid strange behaviour when there aren't enough
					// items to fill the entire listview
					// 当没有足够的列表数据来填充整个ListView时要尽量避免过于奇怪的行为
					if (lastVis >= totalItems) {
						smoothScrollBy(0, 0);
						rebound = false;
						recalcV = false;
						velocity = 0f;
					}

					if (recalcV) {
						recalcV = false;
						velocity /= (1f + ((System.currentTimeMillis() - flingTimestamp) / 1000f));
					}

					if (firstVis == nHeaders) {
						recalcV = false;
					}

					if (visibleCnt > nHeaders) {
						measure = getChildAt(nHeaders);
						if (measure.getTop() + velocity < divHeight) {
							velocity *= -ELASTICITY;
							if (!bounce || Math.abs(velocity) < BREAKSPEED) {
								rebound = false;
								recalcV = false;
								velocity = 0f;
							} else {
								setSelectionFromTop(nHeaders, divHeight + 1);
							}
						}
					} else {
						if (velocity > 0f)
							velocity = -velocity;
					}

					if (rebound) {
						smoothScrollBy((int) -velocity, 0);
						if (velocity > BREAKSPEED) {
							velocity *= ELASTICITY;
							if (velocity < BREAKSPEED) {
								rebound = false;
								recalcV = false;
								velocity = 0f;
							}
						} else
							velocity -= BREAKSPEED;
					}

					// 位于尾部
				} else if (lastVis >= totalItems) {

					if (recalcV) {
						recalcV = false;
						velocity /= (1f + ((System.currentTimeMillis() - flingTimestamp) / 1000f));
					}
					if (lastVis == totalItems - nHeaders - nFooters) {
						rebound = false;
						recalcV = false;
						velocity = 0f;
					} else {
						if (visibleCnt > (nHeaders + nFooters)) {
							measure = getChildAt(visibleCnt - nHeaders - nFooters);
							if (measure.getBottom() + velocity > getHeight() - divHeight) {
								velocity *= -ELASTICITY;
								if (!bounce || Math.abs(velocity) < BREAKSPEED) {
									rebound = false;
									recalcV = false;
									velocity = 0f;
								} else {
									setSelectionFromTop(lastVis - nHeaders - nFooters, getHeight() - divHeight
											- measure.getHeight() - 1);
								}
							}
						} else {
							if (velocity < 0f)
								velocity = -velocity;
						}
					}

					if (rebound) {
						smoothScrollBy((int) -velocity, 0);
						if (velocity < -BREAKSPEED) {
							velocity *= ELASTICITY;
							if (velocity > -BREAKSPEED / ELASTICITY) {
								rebound = false;
								recalcV = false;
								velocity = 0f;
							}
						} else
							velocity += BREAKSPEED;
					}

				} else if (scrollstate == OnScrollListener.SCROLL_STATE_IDLE) {

					rebound = false;
					recalcV = false;
					velocity = 0f;
				}

				LogHelper.i(TAG, "OverscrollListView >> checkListviewTopAndBottom >> rebound is true -> post");
				mHandler.postDelayed(checkListviewTopAndBottom, delay);
				return;
			}

			if (scrollstate != OnScrollListener.SCROLL_STATE_IDLE)
				return;

			if (totalItems == (nHeaders + nFooters) || firstVis < nHeaders) {
				setSelectionFromTop(nHeaders, divHeight);
				smoothScrollBy(0, 0);
			} else if (lastVis == totalItems) {
				int offset = getHeight() - divHeight;
				measure = getChildAt(visibleCnt - nHeaders - nFooters);
				if (measure != null)
					offset -= measure.getHeight();
				setSelectionFromTop(lastVis - nHeaders - nFooters, offset);
				smoothScrollBy(0, 0);
			}
		}
	};
}
