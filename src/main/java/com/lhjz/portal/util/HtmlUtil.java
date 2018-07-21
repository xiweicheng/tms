package com.lhjz.portal.util;

import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class HtmlUtil {

	private HtmlUtil() {
	}

	public static boolean isUrl(String str) {
		String regex = "^(?:https?://)?[\\w]{1,}(?:\\.?[\\w]{1,})+[\\w-_/\\.+?&=#%:]*$";
		return match(regex, str);
	}

	private static boolean match(String regex, String str) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		return matcher.matches();
	}

	public static String favicon(String icon, String url) {

		if (StringUtils.isEmpty(icon))
			return StringUtils.EMPTY;

		icon = StringUtils.trim(icon);

		if (StringUtils.startsWithIgnoreCase(icon, "http://")) {
			return icon;
		}
		if (StringUtils.startsWithIgnoreCase(icon, "https://")) {
			return icon;
		}

		if (StringUtils.startsWith(icon, "//")) {
			try {
				return new URL(url).getProtocol() + ":" + icon;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return "http:" + icon;
			}
		}

		if (StringUtils.startsWith(icon, "/")) {
			return baseUrl(url, false) + icon;
		}

		return baseUrl(url, true) + icon;

	}

	public static String baseUrl(String url, boolean path) {
		try {
			URL loc = new URL(url);
			if (loc.getPort() == -1) {
				return loc.getProtocol() + "://" + loc.getHost() + (path ? baseUrlPath(loc.getPath()) : "/");
			} else {
				return loc.getProtocol() + "://" + loc.getHost() + ":" + loc.getPort()
						+ (path ? baseUrlPath(loc.getPath()) : "/");
			}
		} catch (Exception e) {
			return StringUtils.EMPTY;
		}
	}

	public static String baseUrlPath(String path) {

		if (StringUtils.isEmpty(path)) {
			return "/";
		}

		if (StringUtils.endsWith(path, "/")) {
			return path;
		}

		int index = path.lastIndexOf("/");
		return path.substring(0, index + 1);
	}

	public static String summary(String url) {

		try {
			Document doc = Jsoup.connect(url).get();

			URL loc = new URL(doc.location());

			Elements icon = doc.select("link[rel*=icon]");

			Elements desc = doc.select("meta[name=description]");

			String tpl = "> <img style=\"width: 16px; height:16px;\" src=\"{icon}\" /> [{host}]({baseUrl})\n> [**{title}**]({loc})\n"
					+ "> {desc}";

			if (StringUtils.isBlank(doc.title()) && StringUtils.isBlank(desc.attr("content"))) {
				return StringUtils.EMPTY;
			}

			HashMap<String, Object> map = Maps.newHashMap();
			map.put("loc", doc.location());
			map.put("title", doc.title());
			map.put("host", loc.getHost());
			map.put("baseUrl", baseUrl(url, false));
			map.put("desc", desc.attr("content"));
			map.put("icon", favicon(icon.attr("href"), url));

			System.out.println(StringUtil.replaceByMap(tpl, map));

			return StringUtil.replaceByMap(tpl, map);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return StringUtils.EMPTY;
		}

	}

}
