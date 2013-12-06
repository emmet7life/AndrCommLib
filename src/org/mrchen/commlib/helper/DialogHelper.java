package org.mrchen.commlib.helper;

import org.mrchen.commlib.R;
import org.mrchen.commlib.dialog.CustomDialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class DialogHelper {

	public static Dialog createBaseDialog(final Context context, int resLayoutId) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(resLayoutId, null);
		final CustomDialog dialog = new CustomDialog(context, R.style.DialogBaseStyle);
		int width = context.getResources().getDimensionPixelSize(R.dimen.custom_dialog_width);
		dialog.setView(view, width);
		dialog.setCanceledOnTouchOutside(false);
		// dialog.show();
		return dialog;
	}
}
