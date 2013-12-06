package org.mrchen.commlib.dialog;

import org.mrchen.commlib.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

public class CustomDialog extends Dialog {

	private Window window = null;

	private Context context;

	public CustomDialog(Context context) {
		super(context);
		this.context = context;
	}

	public CustomDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
		this.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_SEARCH) {
					return true;
				}
				return false;
			}
		});
	}

	private float getDensity(Context context) {
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.density;
	}

	public void setView(View view) {
		setView(view, (int) (280 * getDensity(context)));
	}

	public void setView(View view, int width) {
		setContentView(view);
		windowDeploy(0, 0, width);
		// 设置触摸对话框意外的地方取消对话框
		setCanceledOnTouchOutside(true);
	}

	public void showDialog(int layoutResID) {
		setContentView(layoutResID);
		// getWindow().setLayout(LayoutParams.FILL_PARENT,
		// LayoutParams.FILL_PARENT);
		windowDeploy(0, 0, 280);
		// 设置触摸对话框意外的地方取消对话框
		setCanceledOnTouchOutside(true);
		show();
	}

	// 设置窗口显示
	public void windowDeploy(int x, int y, int width) {
		window = getWindow(); // 得到对话框

		// int width = 280;
		if (width <= 0)
			width = (int) (280 * getDensity(context));
		// int width =
		// int height = 150;
		LayoutParams params = window.getAttributes();
		// set width,height by density and gravity
		// float density = getDensity(context);
		// params.width = (int) (width * density);
		params.width = width;
		// params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		// params.height = (int) (height*density);
		params.height = LayoutParams.WRAP_CONTENT;
		params.gravity = Gravity.CENTER;
		window.setAttributes(params);

		window.setWindowAnimations(R.style.DialogAnimationStyle); // 设置窗口弹出动画
		// window.setWindowAnimations(R.style.toast_anim); // 设置窗口弹出动画
		// // window.setBackgroundDrawableResource(R.color.vifrification);
		// // //设置对话框背景为透明
		// WindowManager.LayoutParams wl = window.getAttributes();
		// // 根据x，y坐标设置窗口需要显示的位置
		// wl.x = x; // x小于0左移，大于0右移
		// wl.y = y; // y小于0上移，大于0下移
		// // wl.alpha = 0.6f; //设置透明度
		// // wl.gravity = Gravity.BOTTOM; //设置重力
		// window.setAttributes(wl);
	}

}
