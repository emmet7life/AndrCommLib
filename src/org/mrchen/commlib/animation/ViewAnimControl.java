package org.mrchen.commlib.animation;

import org.mrchen.commlib.R;
import org.mrchen.commlib.helper.ScreenHelper;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

public class ViewAnimControl {

	public static final long DELAYED = 1000;
	public static final long START_OFFSET = 0;
	public static final long DUTAION = 1800;

	public static void startTheadSendHandleMessage(final Handler handler, final View view, final int what) {
		startTheadSendHandleMessage(handler, view, what, 0);
	}

	public static void startTheadSendHandleMessage(final Handler handler, final View view, final int what,
			final long delayed) {
		if (handler != null && view != null) {
			// new Thread(new Runnable() {
			// @Override
			// public void run() {
			view.setVisibility(View.VISIBLE);
			Message msg = new Message();
			msg.obj = view;
			msg.what = what;
			handler.sendMessage(msg);
			// }
			// }).start();
		}
	}

	public static void startTranslateAnim(View view) {
		if (view != null) {
			// view.setVisibility(View.VISIBLE);
			int commVWidth = view.getMeasuredWidth();
			// TranslateAnimation animation = new TranslateAnimation(commVWidth,
			// 0, 0, 0);
			// animation.setInterpolator(new BounceInterpolator());
			// // animation.setStartOffset(START_OFFSET);
			// animation.setDuration(DUTAION);
			// view.startAnimation(animation);
			startTranslateAnim(view, commVWidth, 0);
		}
	}

	public static void startTranslateAnim(View view, float fromY) {
		startTranslateAnim(view, 0, fromY);
	}

	public static void startTranslateAnim(View view, float fromX, float fromY) {
		if (view != null) {
			// view.setVisibility(View.VISIBLE);
			AnimationSet set = new AnimationSet(true);
			TranslateAnimation animation1 = new TranslateAnimation(fromX, 0, fromY, 0);
			animation1.setDuration(300);
			TranslateAnimation animation2 = new TranslateAnimation(0, -15 * ScreenHelper.density, 0, 0);
			animation2.setDuration(300);
			animation2.setStartOffset(300);
			TranslateAnimation animation3 = new TranslateAnimation(0, 15 * ScreenHelper.density, 0, 0);
			animation3.setDuration(300);
			animation3.setStartOffset(600);
			set.addAnimation(animation1);
			set.addAnimation(animation2);
			set.addAnimation(animation3);
			set.setFillAfter(true);
			// animation.setInterpolator(new BounceInterpolator());
			// set.setDuration(DUTAION);
			view.startAnimation(set);
		}
	}

	public static void startBottomBarTransAnim(View view, float fromY) {
		startBottomBarTransAnim(view, fromY, 0);
	}

	public static void startBottomBarTransAnim(View view, float fromY, float toYDelta) {
		if (view != null) {
			// view.setVisibility(View.VISIBLE);
			TranslateAnimation animation = new TranslateAnimation(0, 0, fromY, toYDelta);
			animation.setDuration(600);
			// animation.setFillAfter(true);
			view.startAnimation(animation);
		}
	}

	// public static void startBottomTabVerticalAnim(View view) {
	// if (view != null) {
	// TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -20);
	// // animation.setInterpolator(new BounceInterpolator());
	// animation.setDuration(300);
	// animation.setFillAfter(true);
	// view.startAnimation(animation);
	// }
	// }
	//
	// public static void startBottomTabVerticalDownAnim(View view) {
	// if (view != null) {
	// TranslateAnimation animation = new TranslateAnimation(0, 0, 0, 20);
	// // animation.setInterpolator(new BounceInterpolator());
	// animation.setDuration(100);
	// animation.setFillAfter(true);
	// view.startAnimation(animation);
	// }
	// }

	// 连续循环旋转
	public static Animation getCycleRotateAnimation(Context context) {
		Animation animation = AnimationUtils.loadAnimation(context, R.anim.sweep_rotate);
		LinearInterpolator lir = new LinearInterpolator();
		animation.setInterpolator(lir);
		return animation;
	}

}
