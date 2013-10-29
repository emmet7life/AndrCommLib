package org.mrchen.commlib.animation;

import org.mrchen.commlib.animation.MarginValueAnimation.MarginValueParams;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;

/**
 * 促使调用mTargetView的margin属性发生变化的动画类; <br>
 * 参数配置类:{@link MarginValueParams}
 * 
 * @author chenjianli
 * 
 */
public class MarginValueAnimation extends AbstractAnimation {

	private final static String TAG = "MarginValueAnimation";

	private int mCurrSize; // 当前大小

	/**
	 * {@link MarginValueAnimation} 的辅助参数类，用于指定位移和移动方向 <br>
	 * 
	 * @param size
	 *            指定变化值
	 * @param direction
	 *            指定size应用于哪个属性值上<br>
	 *            {@link MarginValueParams.DIRECTION_LEFT}
	 *            作用于mTargetView的leftMargin属性值<br>
	 *            {@link MarginValueParams.DIRECTION_RIGHT}
	 *            作用于mTargetView的rightMargin属性值<br>
	 *            {@link MarginValueParams.DIRECTION_TOP}
	 *            作用于mTargetView的topMargin属性值<br>
	 *            {@link MarginValueParams.DIRECTION_BOTTOM}
	 *            作用于mTargetView的bottomMargin属性值<br>
	 * @author chenjianli
	 * 
	 */
	public static class MarginValueParams {
		public int size;

		public static final int DIRECTION_LEFT = 1;
		public static final int DIRECTION_RIGHT = 2;
		public static final int DIRECTION_TOP = 3;
		public static final int DIRECTION_BOTTOM = 4;

		public int direction;

		/**
		 * 
		 * @param size
		 *            指定变化值
		 * @param direction
		 *            指定size应用于哪个属性值上<br>
		 *            {@link MarginValueParams.DIRECTION_LEFT}
		 *            作用于mTargetView的leftMargin属性值<br>
		 *            {@link MarginValueParams.DIRECTION_RIGHT}
		 *            作用于mTargetView的rightMargin属性值<br>
		 *            {@link MarginValueParams.DIRECTION_TOP}
		 *            作用于mTargetView的topMargin属性值<br>
		 *            {@link MarginValueParams.DIRECTION_BOTTOM}
		 *            作用于mTargetView的bottomMargin属性值<br>
		 */
		public MarginValueParams(int size, int direction) {
			this.size = size;
			this.direction = direction;
		}
	}

	private MarginValueParams mMarginParam;

	/**
	 * 
	 * @param params
	 *            {@link MarginValueParams}
	 * @param duration
	 *            动画时间
	 */
	public MarginValueAnimation(MarginValueParams params, int duration) {
		mMarginParam = params;
		mDuration = duration;
	}

	public void startAnimation(View view) {
		if (view != null) {
			mTargetView = view;
		} else {
			Log.e(TAG, "view 不能为空");
			return;
		}

		if (isFinished) {
			mDurationReciprocal = 1.0f / (float) mDuration;
			isFinished = false;
			// 记录下动画开始的时间
			mStartTime = AnimationUtils.currentAnimationTimeMillis(); // 动画开始时间
			// 记录最初始的状态值
			initOriginalMarginValue();
			mDSize = mMarginParam.size;
			Log.d(TAG, "mDSize=" + mDSize);
			mHandler.start();
		}
	}

	private int mOriginalMarginValue;// 记录最初始的margin

	private void initOriginalMarginValue() {
		if (mTargetView != null && mTargetView.getVisibility() != View.GONE) {
			LayoutParams params = mTargetView.getLayoutParams();
			if (params != null) {
				if (params instanceof android.widget.LinearLayout.LayoutParams) {
					params = (android.widget.LinearLayout.LayoutParams) params;
					switch (mMarginParam.direction) {
					case MarginValueParams.DIRECTION_LEFT:
						mOriginalMarginValue = ((android.widget.LinearLayout.LayoutParams) params).leftMargin;
						break;
					case MarginValueParams.DIRECTION_RIGHT:
						mOriginalMarginValue = ((android.widget.LinearLayout.LayoutParams) params).rightMargin;
						break;
					case MarginValueParams.DIRECTION_TOP:
						mOriginalMarginValue = ((android.widget.LinearLayout.LayoutParams) params).topMargin;
						break;
					case MarginValueParams.DIRECTION_BOTTOM:
						mOriginalMarginValue = ((android.widget.LinearLayout.LayoutParams) params).bottomMargin;
						break;
					}
				} else if (params instanceof android.widget.RelativeLayout.LayoutParams) {
					params = (android.widget.RelativeLayout.LayoutParams) params;
					switch (mMarginParam.direction) {
					case MarginValueParams.DIRECTION_LEFT:
						mOriginalMarginValue = ((android.widget.RelativeLayout.LayoutParams) params).leftMargin;
						break;
					case MarginValueParams.DIRECTION_RIGHT:
						mOriginalMarginValue = ((android.widget.RelativeLayout.LayoutParams) params).rightMargin;
						break;
					case MarginValueParams.DIRECTION_TOP:
						mOriginalMarginValue = ((android.widget.RelativeLayout.LayoutParams) params).topMargin;
						break;
					case MarginValueParams.DIRECTION_BOTTOM:
						mOriginalMarginValue = ((android.widget.RelativeLayout.LayoutParams) params).bottomMargin;
						break;
					}
				} else if (params instanceof android.widget.FrameLayout.LayoutParams) {
					params = (android.widget.FrameLayout.LayoutParams) params;
					switch (mMarginParam.direction) {
					case MarginValueParams.DIRECTION_LEFT:
						mOriginalMarginValue = ((android.widget.FrameLayout.LayoutParams) params).leftMargin;
						break;
					case MarginValueParams.DIRECTION_RIGHT:
						mOriginalMarginValue = ((android.widget.FrameLayout.LayoutParams) params).rightMargin;
						break;
					case MarginValueParams.DIRECTION_TOP:
						mOriginalMarginValue = ((android.widget.FrameLayout.LayoutParams) params).topMargin;
						break;
					case MarginValueParams.DIRECTION_BOTTOM:
						mOriginalMarginValue = ((android.widget.FrameLayout.LayoutParams) params).bottomMargin;
						break;
					}
				}
			}
		}
	}

	@Override
	public boolean computeSize() {
		// TODO Auto-generated method stub
		if (isFinished) {
			return isFinished;
		}
		int timePassed = (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);
		if (timePassed <= mDuration) {
			float x = timePassed * mDurationReciprocal;
			if (mInterpolator != null) {
				x = mInterpolator.getInterpolation(x);
			}
			mCurrSize = mOriginalMarginValue + Math.round(x * mDSize);
		} else {
			isFinished = true;
			mCurrSize = mOriginalMarginValue + mDSize;
		}

		Log.d(TAG, "computeViewSize >> mCurrSize = " + mCurrSize);
		applySize();
		return isFinished;
	}

	@Override
	public void applySize() {
		// TODO Auto-generated method stub
		if (mTargetView != null && mTargetView.getVisibility() != View.GONE) {
			LayoutParams params = mTargetView.getLayoutParams();
			if (params != null) {
				if (params instanceof android.widget.LinearLayout.LayoutParams) {
					params = (android.widget.LinearLayout.LayoutParams) params;
					switch (mMarginParam.direction) {
					case MarginValueParams.DIRECTION_LEFT:
						((android.widget.LinearLayout.LayoutParams) params).leftMargin = mCurrSize;
						break;
					case MarginValueParams.DIRECTION_RIGHT:
						((android.widget.LinearLayout.LayoutParams) params).rightMargin = mCurrSize;
						break;
					case MarginValueParams.DIRECTION_TOP:
						((android.widget.LinearLayout.LayoutParams) params).topMargin = mCurrSize;
						break;
					case MarginValueParams.DIRECTION_BOTTOM:
						((android.widget.LinearLayout.LayoutParams) params).bottomMargin = mCurrSize;
						break;
					}
				} else if (params instanceof android.widget.RelativeLayout.LayoutParams) {
					params = (android.widget.RelativeLayout.LayoutParams) params;
					switch (mMarginParam.direction) {
					case MarginValueParams.DIRECTION_LEFT:
						((android.widget.RelativeLayout.LayoutParams) params).leftMargin = mCurrSize;
						break;
					case MarginValueParams.DIRECTION_RIGHT:
						((android.widget.RelativeLayout.LayoutParams) params).rightMargin = mCurrSize;
						break;
					case MarginValueParams.DIRECTION_TOP:
						((android.widget.RelativeLayout.LayoutParams) params).topMargin = mCurrSize;
						break;
					case MarginValueParams.DIRECTION_BOTTOM:
						((android.widget.RelativeLayout.LayoutParams) params).bottomMargin = mCurrSize;
						break;
					}
				} else if (params instanceof android.widget.FrameLayout.LayoutParams) {
					params = (android.widget.FrameLayout.LayoutParams) params;
					switch (mMarginParam.direction) {
					case MarginValueParams.DIRECTION_LEFT:
						((android.widget.FrameLayout.LayoutParams) params).leftMargin = mCurrSize;
						break;
					case MarginValueParams.DIRECTION_RIGHT:
						((android.widget.FrameLayout.LayoutParams) params).rightMargin = mCurrSize;
						break;
					case MarginValueParams.DIRECTION_TOP:
						((android.widget.FrameLayout.LayoutParams) params).topMargin = mCurrSize;
						break;
					case MarginValueParams.DIRECTION_BOTTOM:
						((android.widget.FrameLayout.LayoutParams) params).bottomMargin = mCurrSize;
						break;
					}
				}
				mTargetView.setLayoutParams(params);
			}
		}
	}
}
