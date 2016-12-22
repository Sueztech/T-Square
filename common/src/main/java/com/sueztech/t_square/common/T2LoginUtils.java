package com.sueztech.t_square.common;

import org.json.JSONObject;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;

public class T2LoginUtils {

	public static final String LOGIN_TOKEN_1 = "JSESSIONID";
	public static final String LOGIN_TOKEN_2 = "BIGipServer~sakai~t-square-prod-https";
	public static final String LOGIN_URL = "https://t-square.gatech.edu/portal/login";
	public static final String API_DIRECT_URL = "https://t-square.gatech.edu/direct";

	private String mLoginToken1;
	private String mLoginToken2;

	private CookieManager mCookieManager;

	public T2LoginUtils () {
		mCookieManager = new CookieManager();
		CookieHandler.setDefault(mCookieManager);
	}

	public T2LoginUtils (String loginToken1, String loginToken2) {
		this();
		updateLoginTokens(loginToken1, loginToken2);
	}

	public void updateLoginTokens (String loginToken1, String loginToken2) {
		mLoginToken1 = loginToken1;
		mLoginToken2 = loginToken2;
		mCookieManager.getCookieStore().add(null, new HttpCookie(LOGIN_TOKEN_1, loginToken1));
		mCookieManager.getCookieStore().add(null, new HttpCookie(LOGIN_TOKEN_2, loginToken2));
	}

	public String getUserId () {
		String userWeb;
		try {
			userWeb = Utils.fetchURL(API_DIRECT_URL + "/user/current.json");
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		JSONObject userJson = new JSONObject(userWeb);
		return userJson.getString("id");
	}

}
