package com.sueztech.t_square.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginUtils {

	public static final int ERR_NOERROR = 0;
	public static final int ERR_UNEXPECT = 1;
	public static final int ERR_GETLOGIN = 2;
	public static final int ERR_PUTLOGIN = 3;
	public static final int ERR_SIGNINRQ = 4;

	public static final String GT_LOGIN_TOKEN = "CASTGT";
	public static final String T2_LOGIN_TOKEN_1 = "JSESSIONID";
	public static final String T2_LOGIN_TOKEN_2 = "BIGipServer~sakai~t-square-prod-https";

	private static final String GT_LOGIN_URL = "https://login.gatech.edu/cas/login";
	private static final String T2_LOGIN_URL = "https://t-square.gatech.edu/portal/login";

	public LoginStatus doGTLogin (String username, String password) {

		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);

		Document document;
		try {
			document = Jsoup.connect(GT_LOGIN_URL).get();
		} catch (IOException e) {
			e.printStackTrace();
			return new LoginStatus(false, ERR_GETLOGIN, "", "");
		}

		String lt = "";
		String execution = "";

		Elements elements = document.getElementsByClass("btn-row");
		for (Element element : elements.get(0).children()) {
			switch (element.attr("name")) {
				case "lt":
					lt = element.attr("value");
					break;
				case "execution":
					execution = element.attr("value");
					break;
			}
		}

		String formData;
		try {
			formData = "username=" + URLEncoder.encode(username, "UTF-8") +
					"&password=" + URLEncoder.encode(password, "UTF-8") +
					"&lt=" + URLEncoder.encode(lt, "UTF-8") +
					"&execution=" + URLEncoder.encode(execution, "UTF-8") +
					"&_eventId=" + URLEncoder.encode("submit", "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return new LoginStatus(false, ERR_UNEXPECT, "", "");
		}

		URL url;
		HttpURLConnection urlConnection;
		try {
			url = new URL(GT_LOGIN_URL);
			urlConnection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
			return new LoginStatus(false, ERR_PUTLOGIN, "", "");
		}

		try {

			urlConnection.setDoOutput(true);
			urlConnection.setFixedLengthStreamingMode(formData.getBytes().length);

			PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
			out.print(formData);
			out.close();

			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			in.close();
			if (!url.getHost().equals(urlConnection.getURL().getHost())) {
				return new LoginStatus(false, ERR_SIGNINRQ, "", "");
			}

			for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
				if (cookie.getName().equals(GT_LOGIN_TOKEN)) {
					urlConnection.disconnect();
					return new LoginStatus(true, ERR_NOERROR, cookie.getValue(), "");
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			return new LoginStatus(false, ERR_PUTLOGIN, "", "");
		} finally {
			urlConnection.disconnect();
		}

		return new LoginStatus(false, ERR_NOERROR, "", "");

	}

	public LoginStatus doT2Login (String loginToken) {

		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);

		cookieManager.getCookieStore().add(null, new HttpCookie(GT_LOGIN_TOKEN, loginToken));

		URL url;
		HttpURLConnection urlConnection;
		try {
			url = new URL(T2_LOGIN_URL);
			urlConnection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
			return new LoginStatus(false, ERR_PUTLOGIN, "", "");
		}

		try {

			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			in.close();
			if (!urlConnection.getURL().getHost().contains("gatech.edu")) {
				return new LoginStatus(false, ERR_SIGNINRQ, "", "");
			}

		} catch (IOException e) {
			e.printStackTrace();
			return new LoginStatus(false, ERR_PUTLOGIN, "", "");
		} finally {
			urlConnection.disconnect();
		}

		String payload = "";
		String payload2 = "";
		for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
			if (cookie.getName().equals(T2_LOGIN_TOKEN_1)) {
				payload = new HttpCookie(cookie.getName(), cookie.getValue()).toString();
			} else if (cookie.getName().equals(T2_LOGIN_TOKEN_2)) {
				payload2 = new HttpCookie(cookie.getName(), cookie.getValue()).toString();
			}
		}

		System.out.println(HttpCookie.parse(payload).toString());
		System.out.println(HttpCookie.parse(payload2).toString());

		return new LoginStatus(true, ERR_NOERROR, payload, payload2);

	}

	public class LoginStatus {

		public final boolean loggedIn;
		public final int errorMsg;
		public final String payload;
		public final String payload2;

		public LoginStatus (boolean loggedIn, int errorMsg, String payload, String payload2) {
			this.loggedIn = loggedIn;
			this.errorMsg = errorMsg;
			this.payload = payload;
			this.payload2 = payload;
		}

	}

}
