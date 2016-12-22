package com.sueztech.t_square.common;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {

	public static String fetchURL (String urlToFetch) throws IOException {
		URL url = new URL(urlToFetch);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		InputStream in = new BufferedInputStream(urlConnection.getInputStream());
		String out = IOUtils.toString(in, "UTF-8");
		in.close();
		urlConnection.disconnect();
		return out;
	}

}
