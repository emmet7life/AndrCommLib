package org.mrchen.commlib.helper;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

public class IntentHelper {

	/**
	 * 调用系统相机进行拍照并保存图片
	 * 
	 * @param activity
	 * @param file
	 * @param requestCode
	 */
	public static void takePictureBySysCamera(Activity activity, File file, int requestCode) {
		String sysCameraPackageName = "com.android.camera";
		final Intent intent = new Intent();
		final Intent intent_camera = activity.getPackageManager().getLaunchIntentForPackage(sysCameraPackageName);
		if (intent_camera != null) {
			intent.setPackage(sysCameraPackageName);
		}
		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
		if (isIntentAvailable(activity, intent)) {
			activity.startActivityForResult(intent, requestCode);
		} else {
			Toast.makeText(activity, "无法调起系统相机", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 判断Intent是否存在<br>
	 * <br>
	 * 当Android系统调用Intent时，如果没有找到Intent匹配的Activity组件（Component），那么应用将报以下错误：<br>
	 * android.content.ActivityNotFoundException:Unable to find explicit
	 * activity class<br>
	 * 如果没有使用UncaughtExceptionHandler类来处理全局异常，那么程序将异常退出造成不好的用户体验。<br>
	 * 为了防止ActivityNotFoundException错误的出现，在启动Activity之前先判断Intent是否存在。<br>
	 * 
	 * @param context
	 * @param intent
	 * @return
	 */
	public static boolean isIntentAvailable(Context context, Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
		return list.size() > 0;
	}
}
