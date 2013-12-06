package org.mrchen.commlib.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ImageHelper {

	public static final String TAG = "ImageHelper";

	/**
	 * Bitmap图像信息存入文件
	 * 
	 * @param bitmap
	 *            源Bitmap对象
	 * @param pictureStoreFilePathStr
	 *            文件存储路径
	 */
	public static void storeBitmapToFile(Bitmap bitmap, File pictureStoreFilePathStr) {
		storeBitmapToFile(bitmap, pictureStoreFilePathStr, 90);
	}

	/**
	 * Bitmap图像信息存入文件
	 * 
	 * @param bitmap
	 *            源Bitmap对象
	 * @param pictureStoreFilePathStr
	 *            文件存储路径
	 * @param compressQuality
	 *            压缩质量
	 */
	public static void storeBitmapToFile(Bitmap bitmap, File pictureStoreFilePathStr, int compressQuality) {
		if (pictureStoreFilePathStr == null) {
			LogHelper.d(TAG, "Error -> pictureStoreFilePathStr is null. 文件存储路径不能为空!");
			return;
		}

		try {
			if (compressQuality < 0) {
				compressQuality = 0;
			} else if (compressQuality > 100) {
				compressQuality = 100;
			}

			FileOutputStream fos = null;
			fos = new FileOutputStream(pictureStoreFilePathStr);
			bitmap.compress(Bitmap.CompressFormat.PNG, compressQuality, fos);
		} catch (FileNotFoundException e) {
			LogHelper.d(TAG, "Error -> File not found." + e.getMessage());
		} catch (IOException e) {
			LogHelper.d(TAG, "Error -> IOException." + e.getMessage());
		}
	}

	public static Drawable getRepeatDrawableByResId(Resources res, int resId) {
		Bitmap bitmap = BitmapFactory.decodeResource(res, resId);
		// bitmap = Bitmap.createBitmap(100, 20, Config.ARGB_8888);
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		drawable.setDither(true);
		return drawable;
	}

	public static Bitmap createRepeaterBitmap(int width, Bitmap src) {
		int count = (width + src.getWidth() - 1) / src.getWidth();
		Bitmap bitmap = Bitmap.createBitmap(width, src.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		for (int idx = 0; idx < count; ++idx) {
			canvas.drawBitmap(src, idx * src.getWidth(), 0, null);
		}
		return bitmap;
	}

}
