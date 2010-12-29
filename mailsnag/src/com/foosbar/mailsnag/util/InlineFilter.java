package com.foosbar.mailsnag.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InlineFilter {
	
	private static final Pattern SRC_PATTERN = Pattern.compile("<(IMG)(.+?)(SRC\\s*?=\\s*?)([\"'\\s]{0,1})(cid:(.*?))([\"'\\s>]+)(.+?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	
	public static String filter(String content, String path) {
		
		Matcher m = SRC_PATTERN.matcher(content);
		
		if(m.find())
			return m.replaceAll("<$1$2$3$4" + path + "/$6$7$8>");

		return content;
	}
}