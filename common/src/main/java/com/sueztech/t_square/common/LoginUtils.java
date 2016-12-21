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
	private static final String LOGIN_URL = "https://login.gatech.edu/cas/login";

	public LoginStatus doLogin (String username, String password) {

		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);

		Document document;
		try {
			document = Jsoup.connect(LOGIN_URL).get();
		} catch (IOException e) {
			e.printStackTrace();
			return new LoginStatus(false, ERR_GETLOGIN, "");
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
			return new LoginStatus(false, ERR_UNEXPECT, "");
		}

		URL url;
		HttpURLConnection urlConnection;
		try {
			url = new URL(LOGIN_URL);
			urlConnection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
			return new LoginStatus(false, ERR_PUTLOGIN, "");
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
				return new LoginStatus(false, ERR_SIGNINRQ, "");
			}

			for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
				if (cookie.getName().equals("CASTGT")) {
					urlConnection.disconnect();
					return new LoginStatus(true, ERR_NOERROR, cookie.getValue());
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			return new LoginStatus(false, ERR_PUTLOGIN, "");
		} finally {
			urlConnection.disconnect();
		}

		return new LoginStatus(false, ERR_NOERROR, "");

	}

	public class LoginStatus {

		public boolean loggedIn;
		public int errorMsg;
		public String payload;

		public LoginStatus (boolean loggedIn, int errorMsg, String payload) {
			this.loggedIn = loggedIn;
			this.errorMsg = errorMsg;
			this.payload = payload;
		}

	}

}
