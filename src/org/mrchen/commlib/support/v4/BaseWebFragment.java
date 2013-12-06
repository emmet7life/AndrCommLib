package org.mrchen.commlib.support.v4;

import org.mrchen.commlib.R;
import org.mrchen.commlib.constant.Extra;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BaseWebFragment extends BaseFragment implements OnClickListener {

	public static final int WEB_MODE_NORMAL = 1001;
	public static final int WEB_MODE_WEIBO = 1002;

	// Data
	private String mWebTitle;
	private String mWebUrl;
	private boolean mLoadingUrl = true;
	private int mWebMode = WEB_MODE_NORMAL;

	// View
	private WebView mWebView;
	private TextView mTitleView;
	// private ImageView mBackButton;

	// add
	private ImageView mBackView;
	private ImageView mForwardView;
	private ImageView mRefreshView;
	private ProgressBar mProgressBar;
	private ProgressBar mProgressBarWithProgressLine;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		if (v == null) {
			mContentView = inflater.inflate(R.layout.include_base_webview, null);
			getIntentData();
			initUIViews();
			initFragmentInfo();
			initViewsHandle();
		}
		//
		return mContentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (!isUIViewLoadCompleted && !isUIViewLoading) {
			isUIViewLoading = true;
			mWebView.loadUrl(mWebUrl);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private void getIntentData() {
		Bundle bundle = getArguments();
		if (bundle != null) {
			if (bundle.containsKey(Extra.WEB_URL)) {
				mWebUrl = bundle.getString(Extra.WEB_URL, "");
			}

			if (bundle.containsKey(Extra.WEB_TITLE)) {
				mWebTitle = bundle.getString(Extra.WEB_TITLE, "");
			}
		}
	}

	private void initUIViews() {
		// mBackButton = (ImageView)
		// mContentView.findViewById(R.id.layout_head_2_back);
		mTitleView = (TextView) mContentView.findViewById(R.id.layout_head_2_title);

		mWebView = (WebView) mContentView.findViewById(R.id.webview);
		// nullView = contentView.findViewById(R.id.nullview);
		mBackView = (ImageView) mContentView.findViewById(R.id.layout_bottom_webview_goback);
		mForwardView = (ImageView) mContentView.findViewById(R.id.layout_bottom_webview_goforward);
		mRefreshView = (ImageView) mContentView.findViewById(R.id.layout_bottom_webview_refresh);
		mProgressBar = (ProgressBar) mContentView.findViewById(R.id.layout_bottom_webview_progress);
		mProgressBarWithProgressLine = (ProgressBar) mContentView.findViewById(R.id.progressBar);
	}

	private void initFragmentInfo() {
		mTitleView.setText(mWebTitle);
		switch (mWebMode) {
		default:
			loadurl(mWebView, mWebUrl);
			break;
		}
	}

	private void initViewsHandle() {
		// mBackButton.setOnClickListener(this);
		mBackView.setOnClickListener(this);
		mForwardView.setOnClickListener(this);
		mRefreshView.setOnClickListener(this);

		WebSettings webSettings = mWebView.getSettings();
		webSettings.setSavePassword(false);
		webSettings.setSaveFormData(false);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(false);
		mWebView.setWebViewClient(new WebViewClient() {

			public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
				loadurl(view, url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				onPageStart();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				onPageFinish(view);
			}
		});

		mWebView.setWebChromeClient(new MyWebChromeClient());

	}

	private void onPageStart() {
		mLoadingUrl = true;
		mProgressBar.setVisibility(View.VISIBLE);
		mProgressBarWithProgressLine.setProgress(0);
		mProgressBarWithProgressLine.setVisibility(View.VISIBLE);
		mRefreshView.setVisibility(View.INVISIBLE);
		// mBackView.setImageResource(R.drawable.bg_feedback_head_cancle);
	}

	private void onPageFinish(WebView view) {
		mLoadingUrl = false;
		// nullView.setVisibility(View.GONE);
		mWebView.setVisibility(View.VISIBLE);
		mRefreshView.setVisibility(View.VISIBLE);
		mProgressBarWithProgressLine.setProgress(0);
		mProgressBar.setVisibility(View.INVISIBLE);
		mProgressBarWithProgressLine.setVisibility(View.GONE);
		mBackView.setImageResource(R.drawable.tucao_tab_item2_selector);
		mTitleView.setText(view.getTitle());
		checkViewEnabled();
	}

	public void loadurl(final WebView view, final String url) {
		new Thread() {
			public void run() {
				Log.e("WebActivity", "loadurl " + url);
				view.loadUrl(url);
			}
		}.start();
	}

	public boolean onKeyDown(int keyCode) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (checkBackViewEnabled()) {
				mWebView.goBack(); // goBack()表示返回webView的上一页面
			} else {
				// 退出确认
				new AlertDialog.Builder(getActivity()).setTitle("提示").setMessage("确认退出(T_T)?")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								getActivity().finish();
							}
						}).setNegativeButton("取消", null).show();
			}
			return true;
		}
		return super.onKeyDown(keyCode);
	}

	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.layout_bottom_webview_goback) {
			if (mLoadingUrl) {
				mLoadingUrl = false;
				if (mWebMode == WEB_MODE_NORMAL) {
					mWebView.stopLoading();
				} else {
					onPageFinish(mWebView);
				}
				checkViewEnabled();
			} else {
				if (checkBackViewEnabled()) {
					mWebView.goBack();
				}
			}
		} else if (id == R.id.layout_bottom_webview_goforward) {
			if (checkForwardViewEnabled()) {
				mWebView.goForward();
			}
		} else if (id == R.id.layout_bottom_webview_refresh) {
			if (mWebMode == WEB_MODE_NORMAL) {
				mWebView.reload();
			}
		}
	}

	private void checkViewEnabled() {
		checkBackViewEnabled();
		checkForwardViewEnabled();
	}

	private boolean checkBackViewEnabled() {
		if (mWebView.canGoBack()) {
			mBackView.setEnabled(true);
			return true;
		} else {
			mBackView.setEnabled(false);
		}
		return false;
	}

	private boolean checkForwardViewEnabled() {
		if (mWebView.canGoForward()) {
			mForwardView.setEnabled(true);
			return true;
		} else {
			mForwardView.setEnabled(false);
		}
		return false;
	}

	public class MyWebChromeClient extends WebChromeClient {

		@Override
		public void onCloseWindow(WebView window) {
			super.onCloseWindow(window);
		}

		@Override
		public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
			return super.onCreateWindow(view, dialog, userGesture, resultMsg);
		}

		/**
		 * 覆盖默认的window.alert展示界面，避免title里显示为“：来自file:////”
		 */
		@Override
		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
			// builder.setTitle("lizongbo的Android webview测试alert对话框")

			builder.setMessage(message).setPositiveButton("确定", null);
			// 不需要绑定按键事件
			// 屏蔽keycode等于84之类的按键
			builder.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					Log.v("onJsAlert", "keyCode==" + keyCode + "event=" + event);
					return true;
				}

			});
			// 禁止响应按back键的事件
			builder.setCancelable(false);
			AlertDialog dialog = builder.create();
			dialog.show();
			result.confirm();// 因为没有绑定事件，需要强行confirm,否则页面会变黑显示不了内容。
			return true;
			// return super.onJsAlert(view, url, message, result);
		}

		@Override
		public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
			return super.onJsBeforeUnload(view, url, message, result);
		}

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
			mProgressBarWithProgressLine.setProgress(newProgress);
		}

		@Override
		public void onReceivedIcon(WebView view, Bitmap icon) {
			super.onReceivedIcon(view, icon);
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
		}

		@Override
		public void onRequestFocus(WebView view) {
			super.onRequestFocus(view);
		}

	}
}
