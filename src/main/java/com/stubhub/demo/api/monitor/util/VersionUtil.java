package com.stubhub.demo.api.monitor.util;

import java.util.regex.Pattern;

public class VersionUtil {
	public static int compare(String v1, String v2) {
		String s1 = normalisedVersion(v1);
		String s2 = normalisedVersion(v2);
		int cmp = s1.compareTo(s2);
		return cmp;
	}

	public static String normalisedVersion(String version) {
		return normalisedVersion(version, ".", 4);
	}

	public static String normalisedVersion(String version, String sep, int maxWidth) {
		String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
		StringBuilder sb = new StringBuilder();
		for (String s : split) {
			sb.append(String.format("%" + maxWidth + 's', s));
		}
		return sb.toString();
	}
}
