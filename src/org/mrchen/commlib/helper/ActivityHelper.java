package org.mrchen.commlib.helper;

import android.app.Activity;
import android.content.Intent;

public class ActivityHelper {
	public static void startActivityWithNoData(Activity start, Activity from, Class<Activity> end) {
		if (start == null || from == null || end == null)
			return;
		Intent intent = new Intent();
		intent.setClass(from, end);
		start.startActivity(intent);
	}

	public static void startActivityForResultWithNoData(Activity start, Activity from, Class<Activity> end,
			int requestCode) {
		if (start == null || from == null || end == null)
			return;
		Intent intent = new Intent();
		intent.setClass(from, end);
		start.startActivityForResult(intent, requestCode);
	}

}
