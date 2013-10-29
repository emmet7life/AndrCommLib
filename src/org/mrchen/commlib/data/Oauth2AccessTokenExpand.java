package org.mrchen.commlib.data;

import com.weibo.sdk.android.Oauth2AccessToken;

/**
 * 扩充Oauth2AccessToken，补上UID数据备用
 * 
 * @author chenjianli
 * 
 */
public class Oauth2AccessTokenExpand extends Oauth2AccessToken {

	private String uid;

	public String getUID() {
		return uid;
	}

	public void setUID(String uid) {
		this.uid = uid;
	}

	public Oauth2AccessTokenExpand(String token, String expires_in) {
		super(token, expires_in);
	}

	public Oauth2AccessTokenExpand(String token, String expires_in, String uid) {
		super(token, expires_in);
		setUID(uid);
	}

}
