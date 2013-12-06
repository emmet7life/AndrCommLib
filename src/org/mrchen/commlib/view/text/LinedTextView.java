package org.mrchen.commlib.view.text;

import org.mrchen.commlib.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * A custom EditText that draws lines between each line of text that is
 * displayed.
 */
public class LinedTextView extends TextView {
	private Rect mRect;
	private Bitmap mLineBitmap;

	// we need this constructor for LayoutInflater
	public LinedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mRect = new Rect();
		Bitmap lineBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lined_text_view_line_bg);
		int lineWidth = lineBitmap.getWidth();
		int lineHeight = lineBitmap.getHeight();
		int mWidgetWidth = getWidth();
		// TODO
		mWidgetWidth = 600;
		float scaleX = mWidgetWidth / Float.parseFloat(String.valueOf(lineWidth));
		Matrix matrix = new Matrix();
		matrix.postScale(scaleX, 1);
		mLineBitmap = Bitmap.createBitmap(lineBitmap, 0, 0, lineWidth, lineHeight, matrix, true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int count = getLineCount();
		Rect r = mRect;
		for (int i = 0; i < count; i++) {
			int baseline = getLineBounds(i, r);
			canvas.drawBitmap(mLineBitmap, r.left, baseline, null);
		}
		super.onDraw(canvas);
	}
}
