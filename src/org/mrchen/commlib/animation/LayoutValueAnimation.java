package org.mrchen.commlib.animation;

import org.mrchen.commlib.animation.LayoutValueAnimation.LayoutValueParams;
import org.mrchen.commlib.helper.LogHelper;

import android.view.View;
import android.view.animation.AnimationUtils;

/**
 * 促使调用mTargetView.layout(l,t,r,b); <br>
 * 参数配置类:{@link LayoutValueParams}
 * 
 * @author chenjianli
 * 
 */
public class LayoutValueAnimation extends AbstractAnimation {

	private final static String TAG = "LayoutValueAnimation";

	private int mCurrSize;
	private int originalLeft;
	private int originalRight;
	private int originalTop;
	private int originalBottom;

	private int targetLeft;
	private int targetRight;
	private int targetTop;
	private int targetBottom;

	/**
	 * {@link LayoutValueAnimation} 的辅助参数类，用于指定位移和移动方向 <br>
	 * 
	 * @author chenjianli
	 * 
	 */
	public static class LayoutValueParams {
		public int size;

		public static final int DIRECTION_LEFT = 1;
		public static final int DIRECTION_RIGHT = 2;
		public static final int DIRECTION_TOP = 3;
		public static final int DIRECTION_BOTTOM = 4;

		public int direction;

		/**
		 * 
		 * @param size
		 *            只能是正整数
		 * @param direction
		 *            指明运动方向，有<br>
		 *            {@link LayoutValueParams.DIRECTION_LEFT}，<br>
		 *            {@link LayoutValueParams.DIRECTION_RIGHT}，<br>
		 *            {@link LayoutValueParams.DIRECTION_TOP}，<br>
		 *            {@link LayoutValueParams.DIRECTION_BOTTOM}，<br>
		 */
		public LayoutValueParams(int size, int direction) {
			this.size = size;
			this.direction = direction;
		}
	}

	private LayoutValueParams mParams;

	public LayoutValueAnimation(LayoutValueParams params, int duration) {
		mParams = params;
		mDuration = duration;
	}

	// 启动动画
	public void startAnimation(View view) {
		if (view != null) {
			mTargetView = view;
		} else {
			LogHelper.e(TAG, "view 不能为空");
			return;
		}

		if (isFinished) {
			mDurationReciprocal = 1.0f / (float) mDuration;
			isFinished = false;
			// 记录下动画开始的时间
			mStartTime = AnimationUtils.currentAnimationTimeMillis();
			mDSize = mParams.size;
			LogHelper.d(TAG, "mDSize=" + mDSize);
			int l = mTargetView.getLeft();
			int t = mTargetView.getTop();
			int r = mTargetView.getRight();
			int b = mTargetView.getBottom();
			LogHelper.d(TAG, "startAnimation >原始的> l = " + l + ", t = " + t + ", r = " + r + ", b = " + b);
			originalLeft = l;
			originalRight = r;
			originalTop = t;
			originalBottom = b;
			mHandler.start();
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
			switch (mParams.direction) {
			case LayoutValueParams.DIRECTION_LEFT:
			case LayoutValueParams.DIRECTION_TOP:
				mCurrSize = -Math.round(x * mDSize);
				break;
			case LayoutValueParams.DIRECTION_RIGHT:
			case LayoutValueParams.DIRECTION_BOTTOM:
				mCurrSize = Math.round(x * mDSize);
				break;
			}
		} else {
			isFinished = true;
			switch (mParams.direction) {
			case LayoutValueParams.DIRECTION_LEFT:
			case LayoutValueParams.DIRECTION_TOP:
				mCurrSize = -mDSize;
				break;
			case LayoutValueParams.DIRECTION_RIGHT:
			case LayoutValueParams.DIRECTION_BOTTOM:
				mCurrSize = mDSize;
				break;
			}
		}

		// 计算最终目标坐标
		switch (mParams.direction) {
		case LayoutValueParams.DIRECTION_LEFT:
		case LayoutValueParams.DIRECTION_RIGHT:
			targetLeft = originalLeft + mCurrSize;
			targetRight = originalRight + mCurrSize;
			targetTop = originalTop;
			targetBottom = originalBottom;
			break;
		case LayoutValueParams.DIRECTION_TOP:
		case LayoutValueParams.DIRECTION_BOTTOM:
			targetTop = originalTop + mCurrSize;
			targetBottom = originalBottom + mCurrSize;
			targetLeft = originalLeft;
			targetRight = originalRight;
			break;
		}
		LogHelper.d(TAG, "computeSize >目标> l = " + targetLeft + ", t = " + targetTop + ", r = " + targetRight
				+ ", b = " + targetBottom);
		applySize();
		return isFinished;
	}

	@Override
	public void applySize() {
		// TODO Auto-generated method stub
		if (mTargetView != null && mTargetView.getVisibility() != View.GONE) {
			mTargetView.layout(targetLeft, targetTop, targetRight, targetBottom);
		}
	}
}
