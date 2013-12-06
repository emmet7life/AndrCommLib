package org.mrchen.commlib.view;

import org.mrchen.commlib.R;
import org.mrchen.commlib.constant.Direction;
import org.mrchen.commlib.helper.LogHelper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * 类似zaker的推门效果
 * 
 * @author chenjianli
 * 
 */
public class PullDoorView extends RelativeLayout {

	private final String TAG = "PullDoorView";

	private Context mContext;

	private Scroller mBounceScroller;
	private Scroller mLinearScroller;
	private Scroller mScroller;

	private int mScreenWidth = 0;

	private int mScreenHeight = 0;

	private int mLastDown = 0;

	private int mCurr;

	private int mDel;

	private boolean mOpenFlag = false;

	private ImageView mImgView;

	// add
	private final static int LEFT = 0;
	private final static int RIGHT = 1;
	private final static int TOP = 2;
	private final static int BOTTOM = 3;
	private int mDirection = LEFT;

	private final float DEFAULT_WEIGHT = 0.5f;
	private float mEffectWeight = DEFAULT_WEIGHT;
	private int mScrollEffectValue;

	private final int DEFAULT_OPEN_TIME = 330;
	private final int DEFAULT_CLOSE_TIME = 660;
	private int mExecOpenAnimTime = DEFAULT_OPEN_TIME;
	private int mExecCloseAnimTime = DEFAULT_CLOSE_TIME;

	public PullDoorView(Context context) {
		this(context, null);
	}

	public PullDoorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupView(context, attrs);
	}

	private void setupView(Context context, AttributeSet attrs) {
		mContext = context;

		// 这个Interpolator你可以设置别的
		// 我这里选择的是有弹跳效果的Interpolator
		Interpolator polator = new BounceInterpolator();
		mBounceScroller = new Scroller(mContext, polator);
		mLinearScroller = new Scroller(mContext);
		mScroller = mBounceScroller;// 默认的插值器

		// 获取屏幕分辨率
		WindowManager wm = (WindowManager) (mContext.getSystemService(Context.WINDOW_SERVICE));
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		mScreenHeight = dm.heightPixels;
		mScreenWidth = dm.widthPixels;

		// 这里你一定要设置成透明背景,不然会影响你看到底层布局
		this.setBackgroundColor(Color.argb(0, 0, 0, 0));
		mImgView = new ImageView(mContext);
		mImgView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		mImgView.setScaleType(ImageView.ScaleType.FIT_XY);// 填充整个屏幕
		// mImgView.setImageResource(R.drawable.general__book_cover_view__default_04);
		ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#46B525"));
		mImgView.setImageDrawable(colorDrawable);// 默认背景
		addView(mImgView);

		// 加载xml属性值
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PullDoorView);
		int direction = ta.getInt(R.styleable.PullDoorView_pull_direction, LEFT);
		setDirection(direction);
		float weight = ta.getFloat(R.styleable.PullDoorView_effect_weight, DEFAULT_WEIGHT);
		setEffectWeight(weight);
		ta.recycle();
		LogHelper.i(TAG, "PullDoorView ==>> direction = " + direction);
		LogHelper.i(TAG, "PullDoorView ==>> weight = " + weight);
	}

	public void setDirection(Direction direction) {
		setDirection(direction.ordinal());
	}

	private void setDirection(int direction) {
		mDirection = direction;
	}

	public void setEffectWeight(float weight) {
		if (mEffectWeight < 0) {
			mEffectWeight = DEFAULT_WEIGHT;
		}
		mEffectWeight = weight;
		if (mDirection == LEFT || mDirection == RIGHT) {
			mScrollEffectValue = (int) (mScreenWidth * mEffectWeight);
		} else if (mDirection == TOP || mDirection == BOTTOM) {
			mScrollEffectValue = (int) (mScreenHeight * mEffectWeight);
		}
	}

	public void setExecOpenTime(int time) {
		mExecOpenAnimTime = time;
	}

	public void setExecCloseTime(int time) {
		mExecCloseAnimTime = time;
	}

	// 设置推动门背景
	public void setBgImage(int id) {
		mImgView.setImageResource(id);
	}

	// 设置推动门背景
	public void setBgImage(Drawable drawable) {
		mImgView.setImageDrawable(drawable);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mOpenFlag = false;
			// 中止上次未完成的动画
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			if (mDirection == LEFT || mDirection == RIGHT) {
				mLastDown = (int) event.getX();
			} else if (mDirection == TOP || mDirection == BOTTOM) {
				mLastDown = (int) event.getY();
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			if (mDirection == LEFT || mDirection == RIGHT) {
				mCurr = (int) event.getX();
				mDel = mCurr - mLastDown;
				int finalScrollX = getScrollX() - mDel;
				if (mDirection == LEFT) {
					if (finalScrollX < 0) {
						scrollTo(0, 0);
					} else {
						scrollBy(-mDel, 0);
					}
				} else {
					if (finalScrollX > 0) {
						scrollTo(0, 0);
					} else {
						scrollBy(-mDel, 0);
					}
				}
			} else if (mDirection == TOP || mDirection == BOTTOM) {
				mCurr = (int) event.getY();
				mDel = mCurr - mLastDown;
				int finalScrollY = getScrollY() - mDel;
				if (mDirection == TOP) {
					if (finalScrollY < 0) {
						scrollTo(0, 0);
					} else {
						scrollBy(0, -mDel);
					}
				} else {
					if (finalScrollY > 0) {
						scrollTo(0, 0);
					} else {
						scrollBy(0, -mDel);
					}
				}
			}
			mLastDown = mCurr;
			break;
		case MotionEvent.ACTION_UP:
			mScroller = mBounceScroller;
			if (mDirection == LEFT || mDirection == RIGHT) {
				if (Math.abs(getScrollX()) > mScrollEffectValue) {
					if (mDirection == LEFT) {
						startAnimation(getScrollX(), 0, mScreenWidth, 0, mExecOpenAnimTime);
					} else {
						startAnimation(getScrollX(), 0, -mScreenWidth, 0, mExecOpenAnimTime);
					}
					setDoorAction(DoorAction.Opening);
					mOpenFlag = true;
				} else {
					startAnimation(getScrollX(), 0, -getScrollX(), 0, mExecCloseAnimTime);
				}
			} else if (mDirection == TOP || mDirection == BOTTOM) {
				if (Math.abs(getScrollY()) > mScrollEffectValue) {
					if (mDirection == TOP) {
						startAnimation(0, getScrollY(), 0, mScreenHeight, mExecOpenAnimTime);
					} else {
						startAnimation(0, getScrollY(), 0, -mScreenHeight, mExecOpenAnimTime);
					}
					setDoorAction(DoorAction.Opening);
					mOpenFlag = true;
				} else {
					startAnimation(0, getScrollY(), 0, -getScrollY(), mExecCloseAnimTime);
				}
			}
			break;
		}
		return super.onTouchEvent(event);
	}

	public enum DoorAction {
		Opening, Closing
	}

	private DoorAction mDoorAction = DoorAction.Closing;

	private void setDoorAction(DoorAction action) {
		mDoorAction = action;
	}

	public DoorAction getDoorAction() {
		return mDoorAction;
	}

	public boolean isDoorOpen() {
		return mOpenFlag;
	}

	/**
	 * 当门打开的时候，可以收回来
	 */
	public void backToPreDoorState() {
		if (mOpenFlag && getVisibility() == View.GONE) {
			mScroller = mLinearScroller;
			setVisibility(View.VISIBLE);
			setDoorAction(DoorAction.Closing);
			mOpenFlag = false;
			if (mDirection == LEFT || mDirection == RIGHT) {
				startAnimation(getScrollX(), 0, -getScrollX(), 0, mExecCloseAnimTime);
			} else if (mDirection == TOP || mDirection == BOTTOM) {
				startAnimation(0, getScrollY(), 0, -getScrollY(), mExecCloseAnimTime);
			}
		}
	}

	// 推动门的动画
	public void startAnimation(int startX, int startY, int dx, int dy, int duration) {
		LogHelper.i(TAG, "startX = " + startX + ", startY = " + startY + ", dx = " + dx + ", dy = " + dy);
		mScroller.startScroll(startX, startY, dx, dy, duration);
		postInvalidate();
	}

	@Override
	public void computeScroll() {
		boolean compute = mScroller.computeScrollOffset();
		LogHelper.i(TAG, "computeScroll >> compute = " + compute);
		if (compute) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		} else {
			if (mOpenFlag && getDoorAction() == DoorAction.Opening && getVisibility() != View.GONE) {
				this.setVisibility(View.GONE);
			}
		}
	}

}
