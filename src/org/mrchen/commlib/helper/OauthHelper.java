package org.mrchen.commlib.helper;

import org.mrchen.commlib.OauthActivity;
import org.mrchen.commlib.data.Oauth2AccessTokenExpand;

import android.app.Activity;
import android.content.Intent;

import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.sso.SsoHandler;

public class OauthHelper {

	private final String TAG = "OauthHelper";

	private static volatile OauthHelper mOauthHelper;

	private OauthHelper() {
	}

	public static OauthHelper getInstance() {
		if (mOauthHelper == null) {
			synchronized (OauthHelper.class) {
				if (mOauthHelper == null) {
					mOauthHelper = new OauthHelper();
				}
			}
		}
		return mOauthHelper;
	}

	// ************* 新浪微博 **************** //
	private SinaWeibo mSinaWeibo;

	public SinaWeibo getSinaWeibo() {
		if (mSinaWeibo == null) {
			LogHelper.e(TAG, "还没有初始化，请调用initSinaWeibo方法并传入相应参数初始化对象后再次调用。");
			// throw new
			// IllegalStateException("还没有初始化，请调用initSinaWeibo方法并传入相应参数初始化对象后再次调用。");
		}
		return mSinaWeibo;
	}

	/**
	 * 初始化新浪微博
	 * 
	 * @param appKey
	 * @param redirectUrl
	 * @param scope
	 * @param listener
	 */
	public void initSinaWeibo(String appKey, String redirectUrl, String scope, ISinaWeiboOauthListener listener) {
		if (appKey != null && redirectUrl != null && scope != null) {
			// 初次调用或者参数发生变化时需要重新构造Weibo
			if (mSinaWeibo == null || !appKey.equals(mSinaWeibo.getAppKey())
					|| !redirectUrl.equals(mSinaWeibo.getRedirectUrl()) || !scope.equals(mSinaWeibo.getScope())) {
				mSinaWeibo = OauthHelper.SinaWeibo.getInstance();
				mSinaWeibo.init(appKey, redirectUrl, scope, listener);
			}
		}
	}

	/**
	 * 在进行SSO身份授权时必须先执行{@link}initSinaWeibo() 方法
	 * 
	 * @param curr
	 */
	public void startSinaWeiboSsoOauth(Activity curr) {
		if (curr != null) {
			Intent intent = new Intent();
			intent.setClass(curr, OauthActivity.class);
			curr.startActivity(intent);
		}
	}

	public static interface ISinaWeiboOauthListener {
		abstract void onSinaOauthComplete(Oauth2AccessTokenExpand oauth2Token, boolean isShowToast);

		abstract void onSinaOauthError(WeiboDialogError dialogError, WeiboException exception, boolean cancel);
	}

	public static class SinaWeibo {

		private ISinaWeiboOauthListener listener;

		public ISinaWeiboOauthListener getISinaWeiboOauthListener() {
			return listener;
		}

		public void setISinaWeiboOauthListener(ISinaWeiboOauthListener listener) {
			this.listener = listener;
		}

		private static final String TAG = "OauthHelper.SinaWeibo";

		// key rect //
		private String appKey;
		private String redirectUrl;
		private String scope;

		private Weibo mWeibo;
		// private SsoHandler mSsoHandler;

		public static volatile SinaWeibo mSinaWeibo;
		private Activity mSsoActivity;

		private SinaWeibo() {
		}

		public static SinaWeibo getInstance() {
			if (mSinaWeibo == null) {
				synchronized (SinaWeibo.class) {
					if (mSinaWeibo == null) {
						mSinaWeibo = new SinaWeibo();
					}
				}
			}
			return mSinaWeibo;
		}

		public String getAppKey() {
			return appKey;
		}

		public String getRedirectUrl() {
			return redirectUrl;
		}

		public String getScope() {
			return scope;
		}

		private void init(String appKey, String redirectUrl, String scope, ISinaWeiboOauthListener listener) {
			mWeibo = Weibo.getInstance(appKey, redirectUrl, scope);
			//
			this.appKey = appKey;
			this.redirectUrl = redirectUrl;
			this.scope = scope;
			this.listener = listener;
		}

		public SsoHandler initSsoHandler(Activity activity) {
			// if (mSsoHandler == null) {
			return new SsoHandler(activity, mWeibo);
			// }
			// return mSsoHandler;
		}

	}

}
