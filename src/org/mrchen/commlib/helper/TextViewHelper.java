package org.mrchen.commlib.helper;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

public class TextViewHelper {

	/**
	 * 设置字体为粗体<br>
	 * <br>
	 * 在xml中可以定义android:textStyle="bold"来使字体显示为粗体，<br>
	 * 但是这只对英文有效， 当你的TextView要显示中文的时候要在code中<br>
	 * 设置粗体的paint来实现。
	 * 
	 * @param v
	 */
	public static void setTextStyleBlod(TextView v) {
		if (v != null) {
			TextPaint paint = v.getPaint();
			paint.setFakeBoldText(true);
		}
	}

	/**
	 * 设置字体变粗，缩放<br>
	 * <br>
	 * 更具体的用法参见：http://blog.sina.com.cn/s/blog_416bfbd90101mul5.html<br>
	 * 
	 * @param sp
	 *            源文本SpannableString对象
	 * @param message
	 *            源文本
	 * @param preStr
	 *            作用字符前面的字符，勇于精确定位(尚未完全精确)
	 * @param title
	 *            作用字符
	 * @param bigger
	 *            缩放倍率
	 */
	public static void setSpan(SpannableString sp, String message, String preStr, String title, float bigger) {
		if (sp != null && preStr != null && message != null && bigger > 0) {
			int preIndex = message.indexOf(preStr);
			if (preIndex != -1) {
				int index = message.indexOf(title, preIndex);
				if (index != -1) {
					int start = index;
					int end = start + title.length();
					/**
					 * 这些样式都在android.text.style包下，可以查看该包下的类使用更多的效果
					 */
					// 设置字体前景色
					sp.setSpan(new ForegroundColorSpan(Color.parseColor("#333333")), start, end,
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					// 设置粗体
					sp.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					// 设置字体大小
					sp.setSpan(new RelativeSizeSpan(bigger), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}
	}

}
