package org.mrchen.commlib.view.calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.mrchen.commlib.R;
import org.mrchen.commlib.adapter.AbsBaseAdapter;
import org.mrchen.commlib.helper.CommonHelper;
import org.mrchen.commlib.helper.LogHelper;
import org.mrchen.commlib.helper.ScreenHelper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CalendarView extends ScrollView {

	// params
	private String[] titleStrs = { "日", "一", "二", "三", "四", "五", "六" };
	private final int DEFAULT_TITLE_VIEW_HEIGHT = 20;
	private int titleViewHeight = DEFAULT_TITLE_VIEW_HEIGHT;
	private final String DEFAULT_TITLE_VIEW_COLOR = "#333333";
	private int titleViewColor = Color.parseColor(DEFAULT_TITLE_VIEW_COLOR);

	private class HisHitCalendarGridAdapter extends AbsBaseAdapter {

		private final String TAG = "HisHitCalendarGridAdapter";

		private LayoutInflater mInflater;
		// private byte mDayCount;

		// 假定传入的时间格式为yyyy-MM
		private String mTimeFormatStr;
		private byte mDayOfWeek;
		private byte mMaxDate;
		private byte mLines = 5;
		private Context mContext;

		public HisHitCalendarGridAdapter(Context context, String timeFormatStr) {
			mInflater = LayoutInflater.from(context);
			this.mContext = context;
			initTimeFormat(timeFormatStr);
		}

		private void initTimeFormat(String timeFormatStr) {
			if (timeFormatStr != null) {
				mTimeFormatStr = timeFormatStr;
				Calendar calendar = Calendar.getInstance();
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
				try {
					Date date = format.parse(mTimeFormatStr);
					if (date != null) {
						calendar.setTime(date);
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// 1日是星期几
				byte dayOfWeek = (byte) calendar.get(Calendar.DAY_OF_WEEK);
				// 最大的天数
				byte maxDate = (byte) calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

				// 得出当月的1日是星期几，当月的天数
				if (dayOfWeek >= 1 && dayOfWeek <= 7) {
					mDayOfWeek = dayOfWeek;
				} else {
					mDayOfWeek = 2;// 一般不会出现这种情况，这里设定如果出现错误的值，则将当月第一天设置为星期一
				}

				if (maxDate >= 28 && maxDate <= 31) {
					mMaxDate = maxDate;
				} else {
					mMaxDate = 31;
				}

				if (mDayOfWeek >= 6 && mMaxDate > 29) {
					// 总天数超过29的，当月的1日在星期四之后，则行数变成6行
					mLines = 6;
				}

				startTemp = mDayOfWeek - 1;
				endTemp = mMaxDate + startTemp - 1;

				// Log
				LogHelper.d(TAG, "initTimeFormat >> timeFormatStr = " + timeFormatStr + ", mDayOfWeek = " + mDayOfWeek
						+ ", mMaxDate = " + mMaxDate + ", mLines = " + mLines + ", startTemp = " + startTemp
						+ ", endTemp = " + endTemp);
			}
		}

		public byte getLines() {
			return mLines;
		}

		@Override
		public int getCount() {
			return 7 * mLines;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View v, ViewGroup parent) {
			ViewHolder mViewHolder = null;
			if (v == null) {
				mViewHolder = new ViewHolder();
				v = mInflater.inflate(R.layout.history_hit_calendar_grid_item, null);
				mViewHolder.mRootView = v;
				mViewHolder.mDayView = (TextView) v.findViewById(R.id.calendar_item_day);
				mViewHolder.mProbabilityView = (TextView) v.findViewById(R.id.calendar_item_probability);
				v.setTag(mViewHolder);
			} else {
				mViewHolder = (ViewHolder) v.getTag();
			}

			mViewHolder.mRootView.setOnClickListener(null);

			if (position < startTemp) {
				// mViewHolder.mRootView.setBackgroundResource(R.drawable.calendar_grid_item_invisible_view_bg);
				mViewHolder.mRootView.setBackgroundResource(R.drawable.calendar_grid_item_invisible_bg);
				// mViewHolder.mRootView.setBackgroundColor(Color.parseColor("#1f1f1f"));
				mViewHolder.mDayView.setText(null);
				mViewHolder.mProbabilityView.setText(null);
				// mViewHolder.mDayView.setBackgroundColor(Color.parseColor("#871871"));
				// v.setVisibility(View.INVISIBLE);
			} else {
				mViewHolder.mRootView.setBackgroundResource(R.drawable.calendar_grid_item_bg);
				// mViewHolder.mDayView.setVisibility(View.VISIBLE);
				// mViewHolder.mProbabilityView.setVisibility(View.VISIBLE);
				// v.setVisibility(View.VISIBLE);
				if (position > endTemp) {
					// mViewHolder.mRootView.setBackgroundResource(R.drawable.calendar_grid_item_invisible_view_bg);
					mViewHolder.mRootView.setBackgroundResource(R.drawable.calendar_grid_item_invisible_bg);
					// mViewHolder.mRootView.setBackgroundColor(Color.parseColor("#1f1f1f"));
					// mViewHolder.mDayView.setText("#1f1");
					// mViewHolder.mProbabilityView.setText("#1f1");
					mViewHolder.mDayView.setText(null);
					mViewHolder.mProbabilityView.setText(null);
					// mViewHolder.mProbabilityView.setBackgroundColor(Color.parseColor("#666666"));
					// v.setVisibility(View.INVISIBLE);
				} else {
					final String date = String.valueOf(position - startTemp + 1);
					mViewHolder.mDayView.setText(date);
					mViewHolder.mProbabilityView.setText(String.valueOf(new Random().nextInt(100)) + "%");
					mViewHolder.mRootView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// Toast.makeText(AppApplication.mContext,
							// String.valueOf(position),
							// Toast.LENGTH_SHORT).show();
							// DialogHelper.createChargePublicDialog(mContext,
							// null);

							if (mItemClickListener != null) {
								mItemClickListener.onCalendarItemClick(v, position, Integer.parseInt(date));
							}
						}
					});
				}
			}
			return v;
		}

		private int startTemp;
		private int endTemp;

		class ViewHolder {
			View mRootView;
			TextView mDayView;
			TextView mProbabilityView;
		}

	}

	private OnCalendarItemClickListener mItemClickListener;

	public void setOnCalendarItemClickListener(OnCalendarItemClickListener listener) {
		mItemClickListener = listener;
	}

	public static interface OnCalendarItemClickListener {
		public abstract void onCalendarItemClick(View v, int position, int date);
	}

	public CalendarView(Context context) {
		this(context, null);
	}

	public CalendarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CalendarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ScreenHelper.initialize(context);
		//
		LinearLayout containLinear = new LinearLayout(context);
		LayoutParams containLinearParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		containLinear.setLayoutParams(containLinearParams);
		containLinear.setOrientation(LinearLayout.VERTICAL);
		addView(containLinear);
		// title view
		LinearLayout titleLinear = new LinearLayout(context);
		LinearLayout.LayoutParams titleLinearParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		titleLinear.setBackgroundResource(R.drawable.calendar_head_week_bar_bg_xml);
		titleLinear.setLayoutParams(titleLinearParams);
		titleLinear.setOrientation(LinearLayout.HORIZONTAL);
		titleLinear.setGravity(Gravity.CENTER_VERTICAL);
		containLinear.addView(titleLinear);
		//
		int tempTitleViewHeight = (int) (ScreenHelper.density * titleViewHeight);
		for (int i = 0; i < titleStrs.length; i++) {
			LinearLayout.LayoutParams titleViewParams = new LinearLayout.LayoutParams(0, tempTitleViewHeight, 1);
			TextView titleView = new TextView(context);
			titleView.setText(titleStrs[i]);
			titleView.setGravity(Gravity.CENTER);
			titleView.setTextColor(titleViewColor);
			titleLinear.addView(titleView, titleViewParams);
		}
		// gridview
		GridView calendarGrid = new GridView(context);
		LinearLayout.LayoutParams calendarGridParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		calendarGrid.setCacheColorHint(Color.TRANSPARENT);
		calendarGrid.setBackgroundColor(Color.parseColor("#111111"));
		calendarGrid.setGravity(Gravity.CENTER);
		calendarGrid.setNumColumns(7);
		// calendarGrid.setSelector(null);
		calendarGrid.setSelector(new ColorDrawable(Color.TRANSPARENT));
		setFitsSystemWindows(calendarGrid, true);
		calendarGrid.setVerticalSpacing((int) (2 * ScreenHelper.density));
		calendarGrid.setHorizontalSpacing((int) (2 * ScreenHelper.density));
		calendarGrid.setScrollingCacheEnabled(false);
		calendarGrid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		containLinear.addView(calendarGrid, calendarGridParams);

		// load xml attrs
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CalendarView);
		String dateFormatStr = ta.getString(R.styleable.CalendarView_date_format);
		if (dateFormatStr != null && dateFormatStr.length() != 0) {
			HisHitCalendarGridAdapter adapter = new HisHitCalendarGridAdapter(context, dateFormatStr);
			calendarGrid.setAdapter(adapter);
			int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources()
					.getDisplayMetrics());
			CommonHelper.setGridViewHeightBasedOnChildren(calendarGrid, height, adapter.getLines());
		}
		ta.recycle();
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void setFitsSystemWindows(GridView v, boolean b) {
		v.setFitsSystemWindows(b);
	}

}
