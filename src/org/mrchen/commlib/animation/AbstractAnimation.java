package org.mrchen.commlib.animation;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Interpolator;

public abstract class AbstractAnimation implements AbstractAnimationImpl {

	private final int FRAME_TIME = 20;// 一帧的时间 毫秒
	protected View mTargetView;// 执行"动画"的目标View
	protected Interpolator mInterpolator;// 插值器
	protected boolean isFinished = true;// 动画结束标识;

	protected int mDuration; // 动画运行的时间
	protected long mStartTime;// 动画开始时间
	protected float mDurationReciprocal;// Reciprocal:相互的，倒数的
	protected int mDSize; // 需要改变view大小的增量

	private AnimationListener mAnimationListener;

	public interface AnimationListener {
		public void animationEnd(View v);
	}

	public void setOnAnimationListener(AnimationListener listener) {
		mAnimationListener = listener;
	}

	public void setInterpolator(Interpolator interpolator) {
		mInterpolator = interpolator;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setDuration(int duration) {
		mDuration = duration;
	}

	protected AnimationHandler mHandler = new AnimationHandler();

	class AnimationHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

			if (msg.what == 1) {
				if (!computeSize()) {
					mHandler.sendEmptyMessageDelayed(1, FRAME_TIME);
				} else {
					if (mAnimationListener != null) {
						mAnimationListener.animationEnd(mTargetView);
					}
				}
			}
			super.handleMessage(msg);
		}

		public void start() {
			sendEmptyMessage(1);
		}
	}

}
