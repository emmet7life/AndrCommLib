package org.mrchen.commlib.animation;

import android.view.View;

public interface AbstractAnimationImpl {
	public boolean computeSize();// 计算变量

	public void applySize();// 应用计算的变量

	public void startAnimation(View v);// 启动动画
}
