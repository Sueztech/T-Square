package com.sueztech.t_square.common;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.LinkedHashMap;
import java.util.Map;

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

		private static JSONObject mUserJson;

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
			mUserJson = new JSONObject(userWeb);
			return true;
		}

		public static String getId () {
			return mUserJson.getString("id");
		}

		public static String getUsername () {
			return mUserJson.getString("displayId");
		}

		public static String getShortName () {
			return mUserJson.getString("firstName");
		}

		public static String getFullName () {
			return mUserJson.getString("displayName");
		}

		public static class Courses {

			public static Map<String, JSONObject> mCourses = new LinkedHashMap<>();

			private Courses () {

			}

			public static boolean refresh () {

				String coursesWeb;
				try {
					coursesWeb = Utils.fetchURL(API_DIRECT_URL + "/site.json");
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}

				JSONArray site_collection = new JSONObject(coursesWeb).getJSONArray("site_collection");
				for (int i = 0; i < site_collection.length(); i++) {
					JSONObject jsonObject = site_collection.getJSONObject(i);
					mCourses.put(jsonObject.getString("id"), jsonObject);
				}

				return true;
			}

		}

	}

}
