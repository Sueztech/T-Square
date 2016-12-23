package com.sueztech.t_square.common;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;

final class Utils {

	private Utils () {
	}

	static String fetchURL (String url) throws IOException {
		return IOUtils.toString(new URL(url).openConnection().getInputStream(), "UTF-8");
	}

}
