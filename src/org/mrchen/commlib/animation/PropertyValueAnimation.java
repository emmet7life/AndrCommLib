package org.mrchen.commlib.animation;

import org.mrchen.commlib.helper.LogHelper;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;

/**
 * 属性动画，用于更改mTargetView的width和height属性
 * 
 * @author chenjianli
 * 
 */
public class PropertyValueAnimation extends AbstractAnimation {

	private final static String TAG = "ValueAnimation";

	private int mCurrSize; // 当前大小
	private int mRawSize;
	private int mFinalSize;// 最终要固定的大小
	private TYPE mType = TYPE.width;

	public static enum TYPE {
		width, // 改变view的宽度
		height // 改变view的高度
	}

	private final int PARAM_NONE = -1;
	private final int PARAM_BIGGER = 0;
	private final int PARAM_SMALLER = 1;
	private int mParamEffect = PARAM_NONE;

	/**
	 * 
	 * @param finalSize
	 *            控件最终大小
	 * @param type
	 *            指定应用到哪个属性上<br>
	 *            有TYPE.width -> LayoutParams.width，TYPE.height ->
	 *            LayoutParams.height值<br>
	 * @param duration
	 *            动画执行时间
	 */
	public PropertyValueAnimation(int finalSize, TYPE type, int duration) {
		if (mFinalSize < 0) {
			throw new RuntimeException("最终目标的大小不可以为负值 // finalSize < 0 is not allow.");
		}
		mFinalSize = finalSize;
		mType = type;
		mDuration = duration;
	}

	public TYPE getType() {
		return mType;
	}

	public void startAnimation(View view) {
		if (view != null) {
			mTargetView = view;
		} else {
			LogHelper.e(TAG, "view 不能为空");
			return;
		}

		if (isFinished) {
			//
			mDurationReciprocal = 1.0f / (float) mDuration;
			if (mType == TYPE.height) {
				mRawSize = mCurrSize = mTargetView.getHeight();
			} else if (mType == TYPE.width) {
				mRawSize = mCurrSize = mTargetView.getWidth();
			}
			LogHelper.i(TAG, "mRawSize=" + mRawSize);
			if (mFinalSize < 0) {
				throw new RuntimeException("最终目标的大小不可以为负值 // finalSize < 0 is not allow.");
			}
			isFinished = false;
			// 记录下动画开始的时间
			mStartTime = AnimationUtils.currentAnimationTimeMillis(); // 动画开始时间
			if (mCurrSize < mFinalSize) {
				mDSize = mFinalSize - mCurrSize;
				mParamEffect = PARAM_BIGGER;
			} else {
				mDSize = mCurrSize - mFinalSize;
				mParamEffect = PARAM_SMALLER;
			}
			mHandler.start();
		}
	}

	@Override
	public boolean computeSize() {
		// TODO Auto-generated method stub
		if (isFinished) {
			return isFinished;
		}
		// 计算已经消耗的时间（timePassed =：0-mDuration）
		int timePassed = (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);
		if (timePassed <= mDuration) {
			float x = timePassed * mDurationReciprocal;
			if (mInterpolator != null) {
				x = mInterpolator.getInterpolation(x);
			}
			if (mParamEffect == PARAM_BIGGER) {
				mCurrSize = mRawSize + Math.round(x * mDSize);// Math.round()
																// 四舍五入
			} else if (mParamEffect == PARAM_SMALLER) {
				mCurrSize = mRawSize - Math.round(x * mDSize);
			}
		} else {
			isFinished = true;
			if (mParamEffect == PARAM_BIGGER) {
				mCurrSize = mRawSize + mDSize;
			} else if (mParamEffect == PARAM_SMALLER) {
				mCurrSize = mRawSize - mDSize;
			}
		}
		applySize();
		return isFinished;
	}

	@Override
	public void applySize() {
		// TODO Auto-generated method stub
		if (mTargetView != null && mTargetView.getVisibility() != View.GONE) {
			LayoutParams params = mTargetView.getLayoutParams();
			if (mType == TYPE.height) {
				params.height = mCurrSize;
			} else if (mType == TYPE.width) {
				params.width = mCurrSize;
			}
			mTargetView.setLayoutParams(params);
		}
	}
}
