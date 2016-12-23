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

		private User () {

		}

		public static String getId () {

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

}
