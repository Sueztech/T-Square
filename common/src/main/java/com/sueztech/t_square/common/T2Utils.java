package com.sueztech.t_square.common;

import org.json.JSONObject;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;

public class T2Utils {

	private static final String LOGIN_URL = "https://t-square.gatech.edu/portal/login";
	private static final String API_DIRECT_URL = "https://t-square.gatech.edu/direct";

	private T2Utils () {

	}

	public static boolean doLogin () {

		if (CookieHandler.getDefault() == null)
			CookieHandler.setDefault(new CookieManager());

		try {
			Utils.fetchURL(LOGIN_URL);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;

	}

	public static class User {

		private static JSONObject mJson;

		private User () {

		}

		public static boolean refresh () {
			String userWeb;
			try {
				userWeb = Utils.fetchURL(API_DIRECT_URL + "/user/current.json");
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			mJson = new JSONObject(userWeb);
			return true;
		}

		public static JSONObject getJson () {
			return mJson;
		}

		public static String getId () {
			return mJson.getString("id");
		}

		public static String getFirstName () {
			return mJson.getString("firstName");
		}

	}

}
