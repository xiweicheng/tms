package com.lhjz.portal.util;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import com.google.common.collect.Maps;

public class JsoupTest {

	public String favicon(String icon, String url) {

		if (StringUtils.isEmpty(icon))
			return StringUtils.EMPTY;

		icon = StringUtils.trim(icon);

		if (StringUtils.startsWithIgnoreCase(icon, "http://")) {
			return icon;
		}
		if (StringUtils.startsWithIgnoreCase(icon, "https://")) {
			return icon;
		}

		if (StringUtils.startsWith(icon, "/")) {
			return baseUrl(url, false) + icon;
		}

		return baseUrl(url, true) + icon;

	}

	public String baseUrl(String url, boolean path) {
		try {
			URL loc = new URL(url);
			if (loc.getPort() == -1) {
				return loc.getProtocol() + "://" + loc.getHost() + (path ? baseUrlPath(loc.getPath()) : "");
			} else {
				return loc.getProtocol() + "://" + loc.getHost() + ":" + loc.getPort()
						+ (path ? baseUrlPath(loc.getPath()) : "");
			}
		} catch (Exception e) {
			return StringUtils.EMPTY;
		}
	}

	public String baseUrlPath(String path) {

		if (StringUtils.isEmpty(path)) {
			return "/";
		}

		if (StringUtils.endsWith(path, "/")) {
			return path;
		}

		int index = path.lastIndexOf("/");
		return path.substring(0, index);
	}

	@Test
	public void test() throws IOException {

		//		String url = "https://blog.csdn.net/u014789533/article/details/79348657";
		//				String url = "https://github.com/luin/ioredis";
		//		String url = "http://www.w3school.com.cn/cssref/css_selectors.asp";
		//		String url = "https://www.baidu.com/";
		//		String url = "http://localhost:8090/#/home";
		String url = "https://www.cnblogs.com/silentjesse/p/3242701.html";

		Document doc = Jsoup.connect(url).get();

		System.out.println(doc.location());

		URL loc = new URL(doc.location());
		System.out.println(loc.getHost());
		System.out.println(loc.getProtocol());
		System.out.println(loc.getPort());
		System.out.println(loc.getPath());

		System.out.println(doc.title());

		Elements icon = doc.select("link[rel*=icon]");
		System.out.println(icon.attr("href"));
		System.out.println(favicon(icon.attr("href"), url));

		Elements desc = doc.select("meta[name=description]");
		System.out.println(desc.attr("content"));

		System.out.println("=========================");

		String tpl = "> <img style=\"width: 16px; height:16px;\" src=\"{icon}\" /> [{host}]({baseUrl})\n> [**{title}**]({loc})\n"
				+ "> {desc}";

		HashMap<String, Object> map = Maps.newHashMap();
		map.put("loc", doc.location());
		map.put("title", doc.title());
		map.put("host", loc.getHost());
		map.put("baseUrl", baseUrl(url, false));
		map.put("desc", desc.attr("content"));
		map.put("icon", favicon(icon.attr("href"), url));

		System.out.println(StringUtil.replaceByMap(tpl, map));

	}

}
