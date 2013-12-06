package com.sothree.slidinguppanel;

import me.imid.swipebacklayout.lib.ViewDragHelper;

import org.mrchen.commlib.R;
import org.mrchen.commlib.helper.LogHelper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

public class SlidingPanelLayout extends ViewGroup {

	private static final String TAG = SlidingPanelLayout.class.getSimpleName();

	/**
	 * Default peeking out panel height
	 */
	private static final int DEFAULT_PANEL_HEIGHT = 68; // dp;

	/**
	 * Default height of the shadow above the peeking out panel
	 */
	private static final int DEFAULT_SHADOW_HEIGHT = 4; // dp;

	/**
	 * If no fade color is given by default it will fade to 80% gray.
	 */
	private static final int DEFAULT_FADE_COLOR = 0x99000000;

	/**
	 * Minimum velocity that will be detected as a fling
	 */
	private static final int MIN_FLING_VELOCITY = 400; // dips per second

	/**
	 * The fade color used for the panel covered by the slider. 0 = no fading.
	 */
	private int mCoveredFadeColor = DEFAULT_FADE_COLOR;

	/**
	 * The paint used to dim the main layout when sliding
	 */
	private final Paint mCoveredFadePaint = new Paint();

	/**
	 * Drawable used to draw the shadow between panes.
	 */
	private Drawable mShadowDrawable;

	/**
	 * True if need to draw mShadowDrawable
	 */
	private boolean mIsNeedToDrawShadow = true;

	/**
	 * The size of the overhang悬垂 in pixels.
	 */
	private int mPanelHeight;

	/**
	 * The size of the shadow in pixels.
	 */
	private final int mShadowHeight;

	/**
	 * True if a panel can slide滑动 with the current measurements
	 */
	private boolean mCanSlide;

	/**
	 * If provided, the panel can be dragged by only this view. Otherwise, the
	 * entire整个 panel can be used for dragging.
	 */
	private View mDragView;

	/**
	 * The child view that can slide, if any.
	 */
	private View mSlideableView;

	/**
	 * How far the panel is offset from its expanded position. range [0, 1]
	 * where 0 = expanded, 1 = collapsed.<br>
	 * 当前面板的顶部距离父控件顶部的距离差值对mSlideRange的占比，<br>
	 * 主要用来与锚点做对比来最终确定手指松开时面板要滑动到什么位置
	 */
	private float mSlideOffset;

	/**
	 * How far in pixels the slideable panel may move.<br>
	 * 面板可滚动的高度范围
	 */
	private int mSlideRange;

	/**
	 * A panel view is locked into internal内部 scrolling or another condition条件
	 * that is preventing阻止，防御 a drag.
	 */
	private boolean mIsUnableToDrag;

	/**
	 * Flag indicating that sliding feature is enabled\disabled
	 */
	private boolean mIsSlidingEnabled;

	/**
	 * Flag indicating if a drag view can have its own touch events. If set to
	 * true, a drag view can scroll horizontally and have its own click
	 * listener. 设置底部的那个拖动面板上的控件是否应该响应本身的点击事件
	 * 
	 * Default is set to false. 默认为false
	 */
	private boolean mIsUsingDragViewTouchEvents = true;

	/**
	 * Threshold to tell if there was a scroll touch event.
	 */
	private int mScrollTouchSlop;

	private float mInitialMotionX;
	private float mInitialMotionY;
	private boolean mDragViewHit;// Hit 撞击

	private final float DEFAULT_ANCHOR_POINT = 0.f;
	private float mAnchorPoint = DEFAULT_ANCHOR_POINT;// 点击拖动面板时控件上移至屏幕的百分比

	private PanelSlideListener mPanelSlideListener;

	private final ViewDragHelper mDragHelper;

	/**
	 * Stores whether or not是否保存 the pane窗格 was expanded the last time it was
	 * slideable. If expand/collapse operations are invoked this state is
	 * modified. Used by instance state save/restore.<br>
	 * 是否保存面板展开/关闭的状态，在save/restore中存取
	 */
	private boolean mPreservedExpandedState;// preserve 保存
	private boolean mFirstLayout = true;

	private final Rect mTmpRect = new Rect();

	/**
	 * Listener for monitoring events about sliding panes.
	 */
	public interface PanelSlideListener {
		/**
		 * Called when a sliding pane's position changes.
		 * 
		 * @param panel
		 *            The child view that was moved
		 * @param slideOffset
		 *            The new offset of this sliding pane within its range, from
		 *            0-1
		 */
		public void onPanelSlide(View panel, float slideOffset);

		/**
		 * Called when a sliding pane becomes slid completely collapsed. The
		 * pane may or may not be interactive互动 at this point depending on if
		 * it's shown or hidden
		 * 
		 * @param panel
		 *            The child view that was slid to an collapsed position,
		 *            revealing揭开 other panes
		 */
		public void onPanelCollapsed(View panel);

		/**
		 * Called when a sliding pane becomes slid completely expanded. The pane
		 * is now guaranteed保证 to be interactive. It may now obscure掩盖，覆盖 other
		 * views in the layout.
		 * 
		 * @param panel
		 *            The child view that was slid to a expanded position
		 */
		public void onPanelExpanded(View panel);

		public void onPanelAnchored(View panel);
	}

	/**
	 * No-op stubs for {@link PanelSlideListener}. If you only want to implement
	 * a subset of the listener methods you can extend this instead of implement
	 * the full interface.
	 */
	public static class SimplePanelSlideListener implements PanelSlideListener {
		@Override
		public void onPanelSlide(View panel, float slideOffset) {
		}

		@Override
		public void onPanelCollapsed(View panel) {
		}

		@Override
		public void onPanelExpanded(View panel) {
		}

		@Override
		public void onPanelAnchored(View panel) {
		}
	}

	public SlidingPanelLayout(Context context) {
		this(context, null);
	}

	public SlidingPanelLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlidingPanelLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// 加载xml属性值
		// TypedArray ta = context.obtainStyledAttributes(attrs,
		// R.styleable.SlidingPanelLayout, defStyle,
		// R.style.SlidingPanelLayout);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingPanelLayout);
		int direction = ta.getInt(R.styleable.SlidingPanelLayout_panel_direction, PANEL_ON_BOTTOM);
		setPanelDirection(direction);
		setAnchorPoint(ta.getFloat(R.styleable.SlidingPanelLayout_panel_anchor, DEFAULT_ANCHOR_POINT));
		setShadowDrawable(getResources().getDrawable(
				ta.getResourceId(R.styleable.SlidingPanelLayout_panel_shadow, R.drawable.bg_nothing)));
		ta.recycle();
		// mPanelOnDirection = PANEL_ON_TOP;

		final float density = context.getResources().getDisplayMetrics().density;
		mPanelHeight = (int) (DEFAULT_PANEL_HEIGHT * density + 0.5f);
		mShadowHeight = (int) (DEFAULT_SHADOW_HEIGHT * density + 0.5f);

		// 调用这个方法是为了让该ViewGroup将可以调用onDraw(),更多详细信息参考 ->
		// http://blog.csdn.net/leehong2005/article/details/7299471 <-
		setWillNotDraw(false);

		mDragHelper = ViewDragHelper.create(this, 0.5f, new DragHelperCallback());
		mDragHelper.setMinVelocity(MIN_FLING_VELOCITY * density);

		mCanSlide = true;
		mIsSlidingEnabled = true;

		setCoveredFadeColor(DEFAULT_FADE_COLOR);

		ViewConfiguration vc = ViewConfiguration.get(context);
		mScrollTouchSlop = vc.getScaledTouchSlop();
	}

	/**
	 * Set the color used to fade the pane covered by the sliding pane out when
	 * the pane will become fully covered in the expanded state.
	 * 
	 * @param color
	 *            An ARGB-packed color value
	 */
	public void setCoveredFadeColor(int color) {
		mCoveredFadeColor = color;
		invalidate();
	}

	/**
	 * @return The ARGB-packed color value used to fade the fixed pane
	 */
	public int getCoveredFadeColor() {
		return mCoveredFadeColor;
	}

	/**
	 * Set the collapsed panel height in pixels
	 * 
	 * @param val
	 *            A height in pixels
	 */
	public void setPanelHeight(int val) {
		mPanelHeight = val;
		requestLayout();
	}

	/**
	 * @return The current collapsed panel height
	 */
	public int getPanelHeight() {
		return mPanelHeight;
	}

	public void setPanelSlideListener(PanelSlideListener listener) {
		mPanelSlideListener = listener;
	}

	/**
	 * Set the draggable view portion一部分. Use to null, to allow the whole panel
	 * to be draggable
	 * 
	 * @param dragView
	 *            A view that will be used to drag the panel.
	 */
	public void setDragView(View dragView) {
		mDragView = dragView;
	}

	/**
	 * Set an anchor point设置一个锚点 where the panel can stop during sliding
	 * 
	 * @param anchorPoint
	 *            A value between 0 and 1, determining the position of the
	 *            anchor point starting from the top of the layout.
	 */
	public void setAnchorPoint(float anchorPoint) {
		if (anchorPoint > 0 && anchorPoint < 1)
			mAnchorPoint = anchorPoint;
	}

	/**
	 * Set the shadow for the sliding panel
	 * 
	 */
	public void setShadowDrawable(Drawable drawable) {
		mShadowDrawable = drawable;
		ViewCompat.postInvalidateOnAnimation(this);
	}

	public void setIsNeedToDrawShadow(boolean needable) {
		mIsNeedToDrawShadow = needable;
	}

	public boolean isNeedToDrawShadow() {
		return mIsNeedToDrawShadow;
	}

	void dispatchOnPanelSlide(View panel) {
		if (mPanelSlideListener != null) {
			mPanelSlideListener.onPanelSlide(panel, mSlideOffset);
		}
	}

	void dispatchOnPanelExpanded(View panel) {
		if (mPanelSlideListener != null) {
			mPanelSlideListener.onPanelExpanded(panel);
		}
		// Accessibility 无障碍 易接近
		// 发送辅助性事件，更多信息参考 ->
		// http://wiki.eoe.cn/page/Developing_Accessible_Applications.html <-
		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
	}

	void dispatchOnPanelCollapsed(View panel) {
		if (mPanelSlideListener != null) {
			mPanelSlideListener.onPanelCollapsed(panel);
		}
		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
	}

	void dispatchOnPanelAnchored(View panel) {
		if (mPanelSlideListener != null) {
			mPanelSlideListener.onPanelAnchored(panel);
		}
		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
	}

	// Obscured 遮蔽 隐藏
	/**
	 * 更新遮蔽视图的可见性
	 */
	void updateObscuredViewVisibility() {
		if (getChildCount() == 0) {
			return;
		}
		final int leftBound = getPaddingLeft();
		final int rightBound = getWidth() - getPaddingRight();
		final int topBound = getPaddingTop();
		final int bottomBound = getHeight() - getPaddingBottom();
		final int left;
		final int right;
		final int top;
		final int bottom;
		if (mSlideableView != null && hasOpaqueBackground(mSlideableView)) {
			left = mSlideableView.getLeft();
			right = mSlideableView.getRight();
			top = mSlideableView.getTop();
			bottom = mSlideableView.getBottom();
		} else {
			left = right = top = bottom = 0;
		}
		View child = getChildAt(0);
		final int clampedChildLeft = Math.max(leftBound, child.getLeft());
		final int clampedChildTop = Math.max(topBound, child.getTop());
		final int clampedChildRight = Math.min(rightBound, child.getRight());
		final int clampedChildBottom = Math.min(bottomBound, child.getBottom());
		final int vis;
		if (clampedChildLeft >= left && clampedChildTop >= top && clampedChildRight <= right
				&& clampedChildBottom <= bottom) {
			vis = INVISIBLE;
		} else {
			vis = VISIBLE;
		}
		child.setVisibility(vis);
	}

	void setAllChildrenVisible() {
		for (int i = 0, childCount = getChildCount(); i < childCount; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() == INVISIBLE) {
				child.setVisibility(VISIBLE);
			}
		}
	}

	private static boolean hasOpaqueBackground(View v) {
		final Drawable bg = v.getBackground();
		if (bg != null) {
			return bg.getOpacity() == PixelFormat.OPAQUE;
		}
		return false;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mFirstLayout = true;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mFirstLayout = true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("Width must have an exact value or MATCH_PARENT");
		} else if (heightMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("Height must have an exact value or MATCH_PARENT");
		}

		// 容器的布局高度
		int layoutHeight = heightSize - getPaddingTop() - getPaddingBottom();
		// 面板的高度
		int panelHeight = mPanelHeight;
		LogHelper.d(TAG, "onMeasure >> layoutHeight:" + layoutHeight);
		LogHelper.d(TAG, "onMeasure >> panelHeight:" + panelHeight);

		final int childCount = getChildCount();

		// 子视图超过两个时是不被支持的
		if (childCount > 2) {
			Log.e(TAG, "onMeasure: More than two child views are not supported.");
		} else if (getChildAt(1).getVisibility() == GONE) {
			// 当面板视图不可见时，设置面板高度为0
			panelHeight = 0;
		}

		// We'll find the current one below.
		mSlideableView = null;
		mCanSlide = false;

		// First pass. Measure based on child LayoutParams width/height.
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			final LayoutParams lp = (LayoutParams) child.getLayoutParams();

			int height = layoutHeight;
			if (child.getVisibility() == GONE) {
				lp.dimWhenOffset = false;
				continue;
			}

			// 面板视图
			if (i == 1) {
				lp.slideable = true;
				lp.dimWhenOffset = true;
				mSlideableView = child;
				mCanSlide = true;
			} else {
				height -= panelHeight;
			}

			int childWidthSpec;
			if (lp.width == LayoutParams.WRAP_CONTENT) {
				childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST);
			} else if (lp.width == LayoutParams.MATCH_PARENT) {
				childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
			} else {
				childWidthSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
			}

			int childHeightSpec;
			if (lp.height == LayoutParams.WRAP_CONTENT) {
				childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
			} else if (lp.height == LayoutParams.MATCH_PARENT) {
				childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
			} else {
				childHeightSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
			}

			child.measure(childWidthSpec, childHeightSpec);
		}

		// 这一步必须调用
		setMeasuredDimension(widthSize, heightSize);
	}

	public static final int PANEL_ON_LEFT = 1 << 0;
	public static final int PANEL_ON_RIGHT = 1 << 1;
	public static final int PANEL_ON_TOP = 1 << 2;
	public static final int PANEL_ON_BOTTOM = 1 << 3;
	private int mPanelOnDirection = PANEL_ON_BOTTOM;

	private void setPanelDirection(int direction) {
		if (direction == PANEL_ON_LEFT) {
			mPanelOnDirection = PANEL_ON_LEFT;
			// requestLayout();
		} else if (direction == PANEL_ON_RIGHT) {
			mPanelOnDirection = PANEL_ON_RIGHT;
			// requestLayout();
		} else if (direction == PANEL_ON_TOP) {
			mPanelOnDirection = PANEL_ON_TOP;
			// setShadowDrawable(getResources().getDrawable(R.drawable.below_shadow));
			// requestLayout();
		} else if (direction == PANEL_ON_BOTTOM) {
			mPanelOnDirection = PANEL_ON_BOTTOM;
			// setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));
			// requestLayout();
		}
	}

	public int getPanelDirection() {
		return mPanelOnDirection;
	}

	// @Override
	// public void requestLayout() {
	// // if (!mFirstLayout) {
	// super.requestLayout();
	// // }
	// }

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (mFirstLayout) {
			mSlideOffset = mCanSlide && mPreservedExpandedState ? 0.f : 1.f;
		}

		final int paddingLeft = getPaddingLeft();
		final int paddingTop = getPaddingTop();
		final int paddingRight = getPaddingRight();
		final int paddingBottom = getPaddingBottom();

		final int childCount = getChildCount();

		int start = 0;
		int nextStart = 0;
		if (mPanelOnDirection == PANEL_ON_TOP || mPanelOnDirection == PANEL_ON_BOTTOM) {
			start = paddingTop;
			nextStart = start;
		} else if (mPanelOnDirection == PANEL_ON_LEFT || mPanelOnDirection == PANEL_ON_RIGHT) {
			start = paddingLeft;
			nextStart = start;
		}

		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);

			// 如果视图被设置为不可见，则直接忽略不布局
			if (child.getVisibility() == GONE) {
				continue;
			}

			final LayoutParams lp = (LayoutParams) child.getLayoutParams();

			// 在onMeatrue()方法中，我们已经测量好了子控件的宽度高度，这里直接取
			int childHeight = child.getMeasuredHeight();
			int childWidth = child.getMeasuredWidth();

			LogHelper.d(TAG, "onLayout >> childHeight:" + childHeight);
			LogHelper.d(TAG, "onLayout >> childWidth:" + childWidth);

			if (mPanelOnDirection == PANEL_ON_TOP) {
				if (lp.slideable) {
					mSlideRange = childHeight - mPanelHeight;
					start = nextStart - childHeight;
					start += (int) (mSlideRange * (1 - mSlideOffset));
				} else {
					start = nextStart + mPanelHeight;
				}

				final int childTop = start;
				final int childBottom = childTop + childHeight;
				final int childLeft = paddingLeft;
				final int childRight = childLeft + child.getMeasuredWidth();
				child.layout(childLeft, childTop, childRight, childBottom);

				LogHelper.i(TAG, "onLayout >> l:" + childLeft + ", r:" + childRight + ", t:" + childTop + ", b:"
						+ childBottom);

				nextStart = start;
			} else if (mPanelOnDirection == PANEL_ON_BOTTOM) {
				// lp.slideable为true时，表示当前布局的是面板控件
				if (lp.slideable) {
					mSlideRange = childHeight - mPanelHeight;
					start += (int) (mSlideRange * mSlideOffset);
				} else {
					start = nextStart;
				}

				final int childTop = start;
				final int childBottom = childTop + childHeight;
				final int childLeft = paddingLeft;
				final int childRight = childLeft + child.getMeasuredWidth();
				child.layout(childLeft, childTop, childRight, childBottom);

				nextStart += child.getHeight();
			} else if (mPanelOnDirection == PANEL_ON_LEFT) {

			} else if (mPanelOnDirection == PANEL_ON_RIGHT) {

			}

		}

		if (mFirstLayout) {
			updateObscuredViewVisibility();
		}

		mFirstLayout = false;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// Recalculate sliding panes and their details
		// 重新计算滑动面板和他们的信息
		if (h != oldh) {
			mFirstLayout = true;
		}
	}

	/**
	 * Set sliding enabled flag
	 * 
	 * @param enabled
	 *            flag value
	 */
	public void setSlidingEnabled(boolean enabled) {
		mIsSlidingEnabled = enabled;
	}

	/**
	 * Set if the drag view can have its own touch events. If set to true, a
	 * drag view can scroll horizontally and have its own click listener.<br>
	 * 设置面板上控件的点击事件监听是否可用
	 * 
	 * Default is set to false.
	 */
	public void setEnableDragViewTouchEvents(boolean enabled) {
		mIsUsingDragViewTouchEvents = enabled;
	}

	/**
	 * 判断传入的坐标点是否在mDragView/mSlideableView范围内
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isDragViewHit(int x, int y) {
		View v = mDragView != null ? mDragView : mSlideableView;
		if (v == null)
			return false;
		int[] viewLocation = new int[2];
		v.getLocationOnScreen(viewLocation);
		int[] parentLocation = new int[2];
		this.getLocationOnScreen(parentLocation);
		int screenX = parentLocation[0] + x;
		int screenY = parentLocation[1] + y;
		return screenX >= viewLocation[0] && screenX < viewLocation[0] + v.getWidth() && screenY >= viewLocation[1]
				&& screenY < viewLocation[1] + v.getHeight();
	}

	@Override
	public void requestChildFocus(View child, View focused) {
		super.requestChildFocus(child, focused);
		if (!isInTouchMode() && !mCanSlide) {
			mPreservedExpandedState = child == mSlideableView;
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = MotionEventCompat.getActionMasked(ev);

		if (!mCanSlide || !mIsSlidingEnabled || (mIsUnableToDrag && action != MotionEvent.ACTION_DOWN)) {
			mDragHelper.cancel();
			return super.onInterceptTouchEvent(ev);
		}

		if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
			mDragHelper.cancel();
			return false;
		}

		final float x = ev.getX();
		final float y = ev.getY();
		boolean interceptTap = false;

		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			mIsUnableToDrag = false;
			mInitialMotionX = x;
			mInitialMotionY = y;
			// 判断是否落在了有效的可拖动范围内
			mDragViewHit = isDragViewHit((int) x, (int) y);
			//
			if (mDragViewHit && !mIsUsingDragViewTouchEvents) {
				interceptTap = true;
			}
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			final float adx = Math.abs(x - mInitialMotionX);
			final float ady = Math.abs(y - mInitialMotionY);
			final int dragSlop = mDragHelper.getTouchSlop();

			// Handle any horizontal scrolling on the drag view.
			if (mIsUsingDragViewTouchEvents) {
				// 水平向的拖动距离超过临界值切垂直向未超过时交由上层处理
				if (adx > mScrollTouchSlop && ady < mScrollTouchSlop) {
					return super.onInterceptTouchEvent(ev);
				}
				// Intercept the touch if the drag view has any vertical scroll.
				// onTouchEvent will determine if the view should drag
				// vertically.
				// 只要垂直向的滚动生效即超过滚动行为临界值时即可拦截
				else if (ady > mScrollTouchSlop) {
					// 当然前提是触摸了有效区
					interceptTap = mDragViewHit;
				}
			}

			if (ady > dragSlop && adx > ady) {
				mDragHelper.cancel();
				mIsUnableToDrag = true;
				return false;
			}
			break;
		}
		}

		final boolean interceptForDrag = mDragViewHit && mDragHelper.shouldInterceptTouchEvent(ev);

		return interceptForDrag || interceptTap;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (!mCanSlide || !mIsSlidingEnabled) {
			return super.onTouchEvent(ev);
		}

		mDragHelper.processTouchEvent(ev);

		final int action = ev.getAction();
		boolean wantTouchEvents = true;

		switch (action & MotionEventCompat.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			final float x = ev.getX();
			final float y = ev.getY();
			mInitialMotionX = x;
			mInitialMotionY = y;
			break;
		}

		case MotionEvent.ACTION_UP: {
			final float x = ev.getX();
			final float y = ev.getY();
			final float dx = x - mInitialMotionX;
			final float dy = y - mInitialMotionY;
			final int slop = mDragHelper.getTouchSlop();
			if (dx * dx + dy * dy < slop * slop && isDragViewHit((int) x, (int) y)) {
				View v = mDragView != null ? mDragView : mSlideableView;
				v.playSoundEffect(SoundEffectConstants.CLICK);

				if (!isExpanded() && !isAnchored()) {
					expandPane(mSlideableView, 0, mAnchorPoint);
				} else {
					collapsePane();
				}
				break;
			}

			// if (mPanelOnDirection == PANEL_ON_BOTTOM) {
			// } else if (mPanelOnDirection == PANEL_ON_TOP) {
			// }
			break;
		}
		}

		return wantTouchEvents;
	}

	private boolean expandPane(View pane, int initialVelocity, float mSlideOffset) {
		if (mFirstLayout || smoothSlideTo(mSlideOffset, initialVelocity)) {
			mPreservedExpandedState = true;
			return true;
		}
		return false;
	}

	private boolean collapsePane(View pane, int initialVelocity) {
		if (mFirstLayout || smoothSlideTo(1.f, initialVelocity)) {
			mPreservedExpandedState = false;
			return true;
		}
		return false;
	}

	/**
	 * Collapse the sliding pane if it is currently slideable. If first layout
	 * has already completed this will animate.
	 * 
	 * @return true if the pane was slideable and is now collapsed/in the
	 *         process of collapsing
	 */
	public boolean collapsePane() {
		return collapsePane(mSlideableView, 0);
	}

	/**
	 * Expand the sliding pane if it is currently slideable. If first layout has
	 * already completed this will animate.
	 * 
	 * @return true if the pane was slideable and is now expanded/in the process
	 *         of expading
	 */
	public boolean expandPane() {
		return expandPane(0);
	}

	/**
	 * Partially expand the sliding pane up to a specific offset
	 * 
	 * @param mSlideOffset
	 *            Value between 0 and 1, where 0 is completely expanded.
	 * @return true if the pane was slideable and is now expanded/in the process
	 *         of expading
	 */
	public boolean expandPane(float mSlideOffset) {
		if (!isPaneVisible()) {
			showPane();
		}
		return expandPane(mSlideableView, 0, mSlideOffset);
	}

	/**
	 * Check if the layout is completely expanded.
	 * 
	 * @return true if sliding panels are completely expanded
	 */
	public boolean isExpanded() {
		return mFirstLayout && mPreservedExpandedState || !mFirstLayout && mCanSlide && mSlideOffset == 0;
	}

	/**
	 * Check if the layout is anchored in an intermediate中间 point.
	 * 
	 * @return true if sliding panels are anchored
	 */
	public boolean isAnchored() {
		int anchoredTop = (int) (mAnchorPoint * mSlideRange);
		return !mFirstLayout && mCanSlide && mSlideOffset == (float) anchoredTop / (float) mSlideRange;
	}

	/**
	 * Check if the content in this layout cannot fully fit side by side并排、并肩
	 * and therefore因此 the content pane can be slid back and forth.内容面板可以前后滑动
	 * 
	 * @return true if content in this layout can be expanded
	 */
	public boolean isSlideable() {
		return mCanSlide;
	}

	public boolean isPaneVisible() {
		if (getChildCount() < 2) {
			return false;
		}
		View slidingPane = getChildAt(1);
		return slidingPane.getVisibility() == View.VISIBLE;
	}

	public void showPane() {
		if (getChildCount() < 2) {
			return;
		}
		View slidingPane = getChildAt(1);
		slidingPane.setVisibility(View.VISIBLE);
		requestLayout();
	}

	public void hidePane() {
		if (mSlideableView == null) {
			return;
		}
		mSlideableView.setVisibility(View.GONE);
		requestLayout();
	}

	private void onPanelDragged(int newTop) {
		if (mPanelOnDirection == PANEL_ON_BOTTOM) {
			// newTop 面板距离屏幕顶部的距离
			final int topBound = getPaddingTop();
			mSlideOffset = (float) (newTop - topBound) / mSlideRange;
			Log.d(TAG, "onPanelDragged >> newTop:" + newTop + ", mSlideOffset:" + mSlideOffset);
		} else if (mPanelOnDirection == PANEL_ON_TOP) {
			if (newTop < 0) {
				final int paddingTop = getPaddingTop();
				final int layoutHeight = getMeasuredHeight() - paddingTop - getPaddingBottom();
				final int topBound = paddingTop + mPanelHeight - layoutHeight;
				int del = Math.abs(topBound) - Math.abs(newTop);
				mSlideOffset = (float) del / mSlideRange;
			} else {
				mSlideOffset = 1 - (float) (getPaddingTop() - newTop) / mSlideRange;
			}
			Log.d(TAG, "onPanelDragged >> newTop:" + newTop + ", mSlideOffset:" + mSlideOffset);
		}
		dispatchOnPanelSlide(mSlideableView);
	}

	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
		final LayoutParams lp = (LayoutParams) child.getLayoutParams();
		boolean result;
		final int save = canvas.save(Canvas.CLIP_SAVE_FLAG);

		boolean drawScrim = false;

		if (mCanSlide && !lp.slideable && mSlideableView != null) {
			// Clip against the slider; no sense drawing what will immediately
			// be covered.
			canvas.getClipBounds(mTmpRect);
			if (mPanelOnDirection == PANEL_ON_BOTTOM) {
				mTmpRect.bottom = Math.min(mTmpRect.bottom, mSlideableView.getTop());
			} else if (mPanelOnDirection == PANEL_ON_TOP) {
				mTmpRect.top = Math.min(mTmpRect.top, mSlideableView.getBottom());
			}
			canvas.clipRect(mTmpRect);
			if (mSlideOffset < 1) {
				drawScrim = true;
			}
		}

		result = super.drawChild(canvas, child, drawingTime);
		canvas.restoreToCount(save);

		if (drawScrim) {
			float tempSlideOffset = 0;
			if (mPanelOnDirection == PANEL_ON_BOTTOM) {
				tempSlideOffset = mSlideOffset;
			} else if (mPanelOnDirection == PANEL_ON_TOP) {
				tempSlideOffset = 1 - mSlideOffset;
			}
			final int baseAlpha = (mCoveredFadeColor & 0xff000000) >>> 24;
			final int imag = (int) (baseAlpha * (1 - tempSlideOffset));
			final int color = imag << 24 | (mCoveredFadeColor & 0xffffff);
			mCoveredFadePaint.setColor(color);
			canvas.drawRect(mTmpRect, mCoveredFadePaint);
		}

		return result;
	}

	/**
	 * Smoothly animate mDraggingPane to the target X position within its range.<br>
	 * 滚动到制定位置
	 * 
	 * @param slideOffset
	 *            position to animate to 将要滚动到的位置，这个值为float值，具体查看
	 *            {@link #mSlideOffset}
	 * @see #mSlideOffset
	 * @param velocity
	 *            initial velocity in case of fling, or 0.
	 */
	boolean smoothSlideTo(float slideOffset, int velocity) {
		if (!mCanSlide) {
			// Nothing to do.
			return false;
		}

		int y = 0;
		final int topBound = getPaddingTop();
		// slideOffset 滑动距离的百分比，依据此判断滑动的最终方位
		if (mPanelOnDirection == PANEL_ON_BOTTOM) {
			y = (int) (topBound + slideOffset * mSlideRange);
		} else if (mPanelOnDirection == PANEL_ON_TOP) {
			y = (int) (getMeasuredHeight() - slideOffset * mSlideRange - mPanelHeight - topBound);
		}

		LogHelper.i(TAG, "smoothSlideTo >> slideOffset:" + slideOffset + ", y:" + y);

		if (mDragHelper.smoothSlideViewTo(mSlideableView, mSlideableView.getLeft(), y)) {
			setAllChildrenVisible();
			ViewCompat.postInvalidateOnAnimation(this);
			return true;
		}
		return false;
	}

	@Override
	public void computeScroll() {
		if (mDragHelper.continueSettling(true)) {
			if (!mCanSlide) {
				mDragHelper.abort();
				return;
			}
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	@Override
	public void draw(Canvas c) {
		super.draw(c);

		if (mSlideableView == null || !mIsNeedToDrawShadow) {
			// No need to draw a shadow if we don't have one.
			return;
		}

		if (mPanelOnDirection == PANEL_ON_BOTTOM) {
			// 阴影绘制在mSlideableView视图的上方
			final int right = mSlideableView.getRight();
			final int top = mSlideableView.getTop() - mShadowHeight;
			final int bottom = mSlideableView.getTop();
			final int left = mSlideableView.getLeft();

			if (mShadowDrawable != null) {
				mShadowDrawable.setBounds(left, top, right, bottom);
				mShadowDrawable.draw(c);
			}
		} else if (mPanelOnDirection == PANEL_ON_TOP) {
			// 阴影绘制在mSlideableView视图的下方
			final int right = mSlideableView.getRight();
			final int top = mSlideableView.getBottom();
			final int bottom = mSlideableView.getBottom() + mShadowHeight;
			final int left = mSlideableView.getLeft();

			if (mShadowDrawable != null) {
				mShadowDrawable.setBounds(left, top, right, bottom);
				mShadowDrawable.draw(c);
			}
		}

	}

	/**
	 * Tests scrollability within child views of v given a delta of dx.
	 * 
	 * @param v
	 *            View to test for horizontal scrollability
	 * @param checkV
	 *            Whether the view v passed should itself be checked for
	 *            scrollability (true), or just its children (false).
	 * @param dx
	 *            Delta scrolled in pixels
	 * @param x
	 *            X coordinate of the active touch point
	 * @param y
	 *            Y coordinate of the active touch point
	 * @return true if child views of v can be scrolled by delta of dx.
	 */
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (v instanceof ViewGroup) {
			final ViewGroup group = (ViewGroup) v;
			final int scrollX = v.getScrollX();
			final int scrollY = v.getScrollY();
			final int count = group.getChildCount();
			// Count backwards - let topmost views consume scroll distance
			// first.
			for (int i = count - 1; i >= 0; i--) {
				final View child = group.getChildAt(i);
				if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() && y + scrollY >= child.getTop()
						&& y + scrollY < child.getBottom()
						&& canScroll(child, true, dx, x + scrollX - child.getLeft(), y + scrollY - child.getTop())) {
					return true;
				}
			}
		}
		return checkV && ViewCompat.canScrollHorizontally(v, -dx);
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams();
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof MarginLayoutParams ? new LayoutParams((MarginLayoutParams) p) : new LayoutParams(p);
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams && super.checkLayoutParams(p);
	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		SavedState ss = new SavedState(superState);
		ss.isExpanded = isSlideable() ? isExpanded() : mPreservedExpandedState;

		return ss;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());

		if (ss.isExpanded) {
			expandPane();
		} else {
			collapsePane();
		}
		mPreservedExpandedState = ss.isExpanded;
	}

	/**
	 * 
	 * @author chenjianli
	 * 
	 */
	private class DragHelperCallback extends ViewDragHelper.Callback {

		// 判断是否应该捕获该child视图呢
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			if (mIsUnableToDrag) {
				return false;
			}
			return ((LayoutParams) child.getLayoutParams()).slideable;
		}

		@Override
		public void onViewDragStateChanged(int state) {
			if (mDragHelper.getViewDragState() == ViewDragHelper.STATE_IDLE) {
				if (mSlideOffset == 0) {
					// mSlideOffset = 0的时候面板控件处于展开状态
					updateObscuredViewVisibility();
					dispatchOnPanelExpanded(mSlideableView);
					mPreservedExpandedState = true;
				} else if (isAnchored()) {
					// 处于锚点处
					updateObscuredViewVisibility();
					dispatchOnPanelAnchored(mSlideableView);
					mPreservedExpandedState = true;
				} else {
					// 其他状态则是面板收缩状态了
					dispatchOnPanelCollapsed(mSlideableView);
					mPreservedExpandedState = false;
				}
			}
		}

		// 当视图捕获时回调
		@Override
		public void onViewCaptured(View capturedChild, int activePointerId) {
			// Make all child views visible in preparation for sliding things
			// around
			setAllChildrenVisible();
		}

		// 拖动过程中，位置改变时的回调
		@Override
		public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
			LogHelper.d(TAG, "onViewPositionChanged >> left:" + left + ", top:" + top + ", dx:" + dx + ", dy:" + dy);
			onPanelDragged(top);
			invalidate();
		}

		// 手指送开始，确定最终的位置
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			// xvel，yvel代表x轴和y轴的速率，xvel大于0表示向右，yvel大于0表示向下
			int top = getPaddingTop();
			int bottom = getPaddingBottom();
			int finalTop = top;

			if (mPanelOnDirection == PANEL_ON_BOTTOM) {
				// 锚点值被设置了，此时就要判断当前的top与锚点位置的距离了，
				// 根据一定的规则判断是定位到锚点处还是收缩面板还是展开面板
				if (mAnchorPoint != 0) {
					// 锚点位置（屏幕顶部为0开始算的距离）
					int anchoredTop = (int) (mAnchorPoint * mSlideRange);
					// anchorOffset 近似于 mAnchorPoint
					float anchorOffset = (float) anchoredTop / (float) mSlideRange;
					// 滑动到面板初始位置
					if (yvel > 0 || (yvel == 0 && mSlideOffset >= (1f + anchorOffset) / 2)) {
						finalTop += mSlideRange;
					} else if (yvel == 0 && mSlideOffset < (1f + anchorOffset) / 2 && mSlideOffset >= anchorOffset / 2) {
						// 滑动到锚点处
						finalTop += mSlideRange * mAnchorPoint;
					}
				} else if (yvel > 0 || (yvel == 0 && mSlideOffset > 0.5f)) {
					// 滚回到面板初始位置
					finalTop += mSlideRange;
				}
			} else if (mPanelOnDirection == PANEL_ON_TOP) {
				final int layoutHeight = getMeasuredHeight() - top - bottom;
				final int topBound = top + mPanelHeight - layoutHeight;
				final int bottomBound = layoutHeight - Math.abs(topBound);
				float tSlideOffset = 1 - mSlideOffset;
				if (mAnchorPoint != 0) {
					int anchoredTop = (int) (mAnchorPoint * mSlideRange);
					float anchorOffset = (float) anchoredTop / (float) mSlideRange;
					if (yvel < 0 || (yvel == 0 && tSlideOffset >= (1f + anchorOffset) / 2)) {
						finalTop = topBound;
					} else if (yvel == 0 && tSlideOffset < (1f + anchorOffset) / 2 && tSlideOffset >= anchorOffset / 2) {
						finalTop = (int) (bottomBound + mSlideRange * mAnchorPoint) - layoutHeight;
					}
				} else if (yvel < 0 || (yvel == 0 && tSlideOffset < 0.5f)) {
					finalTop = topBound;
				}
			}

			Log.i(TAG, "onViewReleased >> finalTop:" + finalTop);
			mDragHelper.settleCapturedViewAt(releasedChild.getLeft(), finalTop);
			invalidate();
		}

		// 垂直向拖动的范围
		@Override
		public int getViewVerticalDragRange(View child) {
			return mSlideRange;
		}

		// 确保返回的值在容器的高度范围内
		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			int newLeft = 0;
			if (mPanelOnDirection == PANEL_ON_BOTTOM) {
				final int topBound = getPaddingTop();
				final int bottomBound = topBound + mSlideRange;
				newLeft = Math.min(Math.max(top, topBound), bottomBound);
				LogHelper.i(TAG, "clampViewPositionVertical >> topBound:" + topBound + ", top:" + top
						+ ", bottomBound:" + bottomBound + ", newLeft:" + newLeft + ", View ID:" + getId());
			} else if (mPanelOnDirection == PANEL_ON_TOP) {
				final int paddingTop = getPaddingTop();
				final int layoutHeight = getMeasuredHeight() - paddingTop - getPaddingBottom();
				final int topBound = paddingTop + mPanelHeight - layoutHeight;
				final int bottomBound = layoutHeight - Math.abs(topBound);
				newLeft = Math.min(Math.max(top, topBound), paddingTop);
				// LogHelper.i(TAG, "clampViewPositionVertical >> layoutHeight:"
				// + layoutHeight + ", topBound:" + topBound
				// + ", top:" + top + ", bottomBound:" + bottomBound +
				// ", newLeft:" + newLeft + ", View ID:"
				// + getId());
			}
			return newLeft;
		}

	}

	public static class LayoutParams extends ViewGroup.MarginLayoutParams {
		private static final int[] ATTRS = new int[] { android.R.attr.layout_weight };

		/**
		 * True if this pane is the slideable pane in the layout.
		 */
		boolean slideable;

		/**
		 * True if this view should be drawn dimmed暗淡 when it's been offset from
		 * its default position.
		 */
		boolean dimWhenOffset;

		Paint dimPaint;

		public LayoutParams() {
			super(MATCH_PARENT, MATCH_PARENT);
		}

		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public LayoutParams(android.view.ViewGroup.LayoutParams source) {
			super(source);
		}

		public LayoutParams(MarginLayoutParams source) {
			super(source);
		}

		public LayoutParams(LayoutParams source) {
			super(source);
		}

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);

			final TypedArray a = c.obtainStyledAttributes(attrs, ATTRS);
			a.recycle();
		}

	}

	static class SavedState extends BaseSavedState {
		boolean isExpanded;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			isExpanded = in.readInt() != 0;
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(isExpanded ? 1 : 0);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}
