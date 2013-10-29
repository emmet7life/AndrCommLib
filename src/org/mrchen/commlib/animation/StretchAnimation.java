package org.mrchen.commlib.animation;

import org.mrchen.commlib.helper.LogHelper;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;

/**
 * 伸缩Animation，该Animation有特定的用法，详见Example
 * 
 * @author chenjianli
 * 
 */
public class StretchAnimation extends AbstractAnimation {

	private final static String TAG = "StretchAnimation";

	private int mCurrSize; // 当前大小
	private int mRawSize;
	private int mMinSize; // 最小大小 固定值
	private int mMaxSize; // 最大大小 固定值
	private TYPE mType = TYPE.vertical;

	public static enum TYPE {
		horizontal, // 改变view水平方向的大小
		vertical // 改变view竖直方向的大小
	}

	public StretchAnimation(int maxSize, int minSize, TYPE type, int duration) {
		if (minSize >= maxSize) {
			throw new RuntimeException("View的最大改变值不能小于最小改变值");
		}
		mMinSize = minSize;
		mMaxSize = maxSize;
		mType = type;
		mDuration = duration;
	}

	public TYPE getmType() {
		return mType;
	}

	public void startAnimation(View view) {
		if (view != null) {
			mTargetView = view;
		} else {
			LogHelper.e(TAG, "view 不能为空");
			return;
		}
		// 获取要执行动画的控件的LayoutParams
		// LayoutParams params = mView.getLayoutParams();
		// 上一次的动画已经结束，可以接着启动这次的了
		if (isFinished) {
			//
			mDurationReciprocal = 1.0f / (float) mDuration;
			if (mType == TYPE.vertical) {
				mRawSize = mCurrSize = mTargetView.getHeight();
			} else if (mType == TYPE.horizontal) {
				mRawSize = mCurrSize = mTargetView.getWidth();
			}
			LogHelper.i(TAG, "mRawSize=" + mRawSize);
			if (mCurrSize > mMaxSize || mCurrSize < mMinSize) {
				throw new RuntimeException("View 的大小不达标 currentViewSize > mMaxSize || currentViewSize < mMinSize");
			}
			isFinished = false;
			// 记录下动画开始的时间
			mStartTime = AnimationUtils.currentAnimationTimeMillis(); // 动画开始时间
			if (mCurrSize < mMaxSize) {
				mDSize = mMaxSize - mCurrSize;
			} else {
				mDSize = mMinSize - mMaxSize;
			}
			LogHelper.i(TAG, "mDSize=" + mDSize);
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
			mCurrSize = mRawSize + Math.round(x * mDSize);// Math.round() 四舍五入
		} else {
			isFinished = true;
			mCurrSize = mRawSize + mDSize;
		}
		applySize();
		return isFinished;
	}

	@Override
	public void applySize() {
		// TODO Auto-generated method stub
		if (mTargetView != null && mTargetView.getVisibility() != View.GONE) {
			LayoutParams params = mTargetView.getLayoutParams();
			if (mType == TYPE.vertical) {
				params.height = mCurrSize;
			} else if (mType == TYPE.horizontal) {
				params.width = mCurrSize;
			}
			mTargetView.setLayoutParams(params);
		}
	}

}
