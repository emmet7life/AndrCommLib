package org.mrchen.commlib;

import java.text.SimpleDateFormat;

import org.mrchen.commlib.data.Oauth2AccessTokenExpand;
import org.mrchen.commlib.helper.LogHelper;
import org.mrchen.commlib.helper.OauthHelper;
import org.mrchen.commlib.helper.OauthHelper.SinaWeibo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.sso.SsoHandler;
import com.weibo.sdk.android.util.AccessTokenKeeper;

public class OauthActivity extends Activity {

	private final String TAG = "OauthActivity";

	private Activity mContext;

	private SinaWeibo mSinaWeibo;
	private SsoHandler mSsoHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogHelper.d(TAG, "onCreate ->> ");
		mContext = this;
		// 开始验证SSO登录吧
		mSinaWeibo = OauthHelper.getInstance().getSinaWeibo();
		if (mSinaWeibo == null) {
			finish();
		} else {
			mSsoHandler = mSinaWeibo.initSsoHandler(this);
			mSsoHandler.authorize(new AuthDialogListener(), null);
		}
		// add nothing
	}

	// protected void onResume() {
	// super.onResume();
	// LogHelper.d(TAG, "onResume ->> ");
	// }
	//
	// protected void onDestroy() {
	// super.onDestroy();
	// LogHelper.d(TAG, "onDestroy ->> ");
	// }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);
		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	// ***************** Listener ******************* //

	/** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能 */
	private Oauth2AccessTokenExpand mAccessToken;

	/**
	 * 微博认证授权回调类。 1. SSO登陆时，需要在{@link #onActivityResult}
	 * 中调用mSsoHandler.authorizeCallBack后， 该回调才会被执行。 2. 非SSO登陆时，当授权后，就会被执行。
	 * 当授权成功后，请保存该access_token、expires_in等信息到SharedPreferences中。
	 */
	private class AuthDialogListener implements WeiboAuthListener {

		@Override
		public void onComplete(Bundle values) {
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			String uid = values.getString("uid");
			mAccessToken = new Oauth2AccessTokenExpand(token, expires_in, uid);
			if (mAccessToken.isSessionValid()) {
				String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new java.util.Date(mAccessToken
						.getExpiresTime()));
				// mText.setText("认证成功: \r\n access_token: " + token +
				// "\r\n" + "expires_in: " + expires_in
				// + "\r\n有效期：" + date);
				LogHelper.i(TAG, "认证成功: \r\n access_token: " + token + "\r\n" + "expires_in: " + expires_in
						+ "\r\n有效期：" + date);
				AccessTokenKeeper.keepAccessToken(mContext, mAccessToken);
				Toast.makeText(mContext, "认证成功", Toast.LENGTH_SHORT).show();
			}

			finish();

			if (mSinaWeibo.getISinaWeiboOauthListener() != null) {
				mSinaWeibo.getISinaWeiboOauthListener().onSinaOauthComplete(mAccessToken, false);
			}

		}

		@Override
		public void onError(WeiboDialogError e) {
			Toast.makeText(getApplicationContext(), "认证错误 : " + e.getMessage(), Toast.LENGTH_LONG).show();
			finish();

			if (mSinaWeibo.getISinaWeiboOauthListener() != null) {
				mSinaWeibo.getISinaWeiboOauthListener().onSinaOauthError(e, null, false);
			}
		}

		@Override
		public void onCancel() {
			Toast.makeText(getApplicationContext(), "取消认证", Toast.LENGTH_LONG).show();
			finish();

			if (mSinaWeibo.getISinaWeiboOauthListener() != null) {
				mSinaWeibo.getISinaWeiboOauthListener().onSinaOauthError(null, null, true);
			}
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(getApplicationContext(), "认证异常 : " + e.getMessage(), Toast.LENGTH_LONG).show();
			finish();

			if (mSinaWeibo.getISinaWeiboOauthListener() != null) {
				mSinaWeibo.getISinaWeiboOauthListener().onSinaOauthError(null, e, false);
			}
		}
	}

}
