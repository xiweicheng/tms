package com.lhjz.portal.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import io.github.gitbucket.markedj.Marked;
import io.github.gitbucket.markedj.Options;

/**
 * 字符串常用工具类.
 * 
 * @creation 2013-10-8 下午12:57:30
 * @modification 2013-10-8 下午12:57:30
 * @company Canzs
 * @author xiweicheng
 * @version 1.0
 * 
 */
public final class StringUtil {

	/**
	 * 判断是不是数字字符串.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumber(String str) {

		if (isEmpty(str)) {
			return false;
		} else {
			try {
				Double.parseDouble(str);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}

	/**
	 * 判断是否为正确的邮件格式
	 * 
	 * @param str
	 * @return boolean
	 */
	public static boolean isEmail(String str) {
		if (isEmpty(str))
			return false;
		return str.matches("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$");
	}

	/**
	 * 判断字符串是否为合法手机号 11位 13 14 15 18开头
	 * 
	 * @param str
	 * @return boolean
	 */
	public static boolean isMobile(String str) {
		if (isEmpty(str))
			return false;
		return str.matches("^(13|14|15|18)\\d{9}$");
	}

	/**
	 * 裁剪字符串到指定长度.
	 * 
	 * @param string
	 * @param i
	 */
	public static String limitLength(String str, int i) {
		if (isEmpty(str)) {
			return "";
		} else {
			if (str.length() <= i) {
				return str;
			} else {
				return str.substring(0, i - 3) + "...";
			}
		}
	}

	/** EMPTY [String] */
	public static final String EMPTY = "";

	/** EMPTY [String] */
	public static final String SEARCH_SEPARATOR = "@&~";

	private StringUtil() {
		super();
	}

	/**
	 * 字符串占位符替换.
	 * 
	 * @param tpl
	 *            like xx{?1}yyy{?2}zzz
	 * @param vals
	 *            替换占位符的值
	 * @return 替换后的结果
	 */
	public static String replace(String tpl, Object... vals) {

		if (tpl != null) {

			for (int i = 1; i <= vals.length; i++) {
				tpl = tpl.replace("{?" + i + "}", String.valueOf(vals[i - 1]));
			}
		}

		return tpl;
	}

	/**
	 * replace like xxxx{key}yyy{key}zzzz
	 * 
	 * @author xiweicheng
	 * @creation 2013年11月28日 下午1:25:16
	 * @modification 2013年11月28日 下午1:25:16
	 * @param tpl
	 *            string like xxxx{key}yyy{key}zzzz
	 * @param map
	 * @return
	 */
	public static String replaceByMap(String tpl, Map<String, Object> map) {

		if (tpl != null && map != null && map.size() > 0) {

			for (String key : map.keySet()) {
				tpl = tpl
						.replace("{" + key + "}", String.valueOf(map.get(key)));
			}
		}

		return tpl;
	}

	/**
	 * replace like xxxx{key}yyy{key}zzzz
	 * 
	 * @author xiweicheng
	 * @creation 2013年11月28日 下午1:58:16
	 * @modification 2013年11月28日 下午1:58:16
	 * @param tpl
	 *            string like xxxx{key}yyy{key}zzzz
	 * @param vals
	 *            key:value pair like k1, v1, k2, v2 ...
	 * @return
	 */
	public static String replaceByKV(String tpl, Object... vals) {

		Map<String, Object> map = new HashMap<String, Object>();

		for (int i = 0; i < vals.length; i += 2) {
			if (i + 1 < vals.length) {
				map.put(String.valueOf(vals[i]), vals[i + 1]);
			}
		}

		return replaceByMap(tpl, map);
	}

	/**
	 * 连接字符串.
	 * 
	 * @param connector
	 * @param objects
	 * @return
	 */
	public static String join(String connector, Object... objects) {

		StringBuffer sBuffer = new StringBuffer();

		for (Object object : objects) {
			sBuffer.append(object).append(connector);
		}

		if (sBuffer.length() > 0) {
			sBuffer.delete(sBuffer.length() - connector.length(),
					sBuffer.length());
		}

		return sBuffer.toString();
	}

	/**
	 * 连接字符串.
	 * 
	 * @param connector
	 * @param arr
	 * @return
	 */
	public static String join(String connector, String[] arr) {

		StringBuffer sBuffer = new StringBuffer();

		for (Object object : arr) {
			sBuffer.append(object).append(connector);
		}

		if (sBuffer.length() > 0) {
			sBuffer.delete(sBuffer.length() - connector.length(),
					sBuffer.length());
		}

		return sBuffer.toString();
	}

	/**
	 * 连接字符串.
	 * 
	 * @param connector
	 * @param strs
	 * @return
	 */
	public static String join2(String connector, String... strs) {

		StringBuffer sBuffer = new StringBuffer();

		for (Object object : strs) {
			sBuffer.append(object).append(connector);
		}

		if (sBuffer.length() > 0) {
			sBuffer.delete(sBuffer.length() - connector.length(),
					sBuffer.length());
		}

		return sBuffer.toString();
	}

	/**
	 * 连接字符串.
	 * 
	 * @param connector
	 * @param objects
	 * @return
	 */
	public static String join(String connector, List<String> objects) {

		StringBuffer sBuffer = new StringBuffer();

		for (String object : objects) {
			sBuffer.append(object).append(connector);
		}

		if (sBuffer.length() > 0) {
			sBuffer.delete(sBuffer.length() - connector.length(),
					sBuffer.length());
		}

		return sBuffer.toString();
	}
	
	/**
	 * 连接字符串.
	 * 
	 * @param connector
	 * @param objects
	 * @return
	 */
	public static String join2(String connector, List<Object> objects) {
 
		StringBuffer sBuffer = new StringBuffer();

		for (Object object : objects) {
			sBuffer.append(object).append(connector);
		}

		if (sBuffer.length() > 0) {
			sBuffer.delete(sBuffer.length() - connector.length(),
					sBuffer.length());
		}

		return sBuffer.toString();
	}

	/**
	 * 连接字符串.
	 * 
	 * @param connector
	 * @param objects
	 * @return
	 */
	public static String join(String connector, Set<String> objects) {

		StringBuffer sBuffer = new StringBuffer();

		for (String object : objects) {
			sBuffer.append(object).append(connector);
		}

		if (sBuffer.length() > 0) {
			sBuffer.delete(sBuffer.length() - connector.length(),
					sBuffer.length());
		}

		return sBuffer.toString();
	}

	/**
	 * 对象转字符串.
	 * 
	 * @param object
	 * @return
	 */
	public static String toString(Object object) {

		return object == null ? EMPTY : object.toString();
	}

	/**
	 * 从map中获取key对应的字符串值.
	 * 
	 * @param map
	 * @param key
	 * @return
	 */
	public static String getString(Map<String, Object> map, String key) {

		if (map == null || !map.containsKey(key)) {
			return null;
		}

		return toString(map.get(key));
	}

	/**
	 * 从map中获取key对应的字符串值.
	 * 
	 * @param map
	 * @param key
	 * @return
	 */
	public static String getNotNullString(Map<String, Object> map, String key) {

		if (map == null || !map.containsKey(key)) {
			return EMPTY;
		}

		return toString(map.get(key));
	}

	/**
	 * 字符串判空.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(Object str) {

		return str == null || str.equals(EMPTY);
	}

	/**
	 * 字符串判非空.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNotEmpty(Object str) {

		return !isEmpty(str);
	}

	/**
	 * 字符串判非空.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isValid(String str) {

		return !isEmpty(str);
	}

	/**
	 * 字符串数组转化为List<String>.
	 * 
	 * @param values
	 * @return
	 */
	public static List<String> array2List(String... values) {

		List<String> list = new ArrayList<String>();

		for (String value : values) {
			list.add(value);
		}

		return list;
	}

	/**
	 * 字符串数组转化为Map<String, Object>
	 * 
	 * @author xiweicheng
	 * @creation 2013年11月30日 下午6:22:26
	 * @modification 2013年11月30日 下午6:22:26
	 * @param values
	 * @return
	 */
	public static Map<String, Object> array2Map(Object... values) {

		Map<String, Object> map = new HashMap<>();

		if (values.length > 0) {

			for (int i = 0; i < values.length; i += 2) {

				if (i + 1 < values.length) {
					map.put(String.valueOf(values[i]), values[i + 1]);
				}
			}
		}

		return map;
	}

	/**
	 * 字符串数组转化为Map<String, Object>
	 * 
	 * @author xiweicheng
	 * @creation 2013年11月30日 下午6:22:26
	 * @modification 2013年11月30日 下午6:22:26
	 * @param values
	 * @return
	 */
	public static Map<String, String> stringArr2Map(String... values) {

		Map<String, String> map = new HashMap<>();

		if (values.length > 0) {

			for (int i = 0; i < values.length; i += 2) {

				if (i + 1 < values.length) {
					map.put(values[i], values[i + 1]);
				}
			}
		}

		return map;
	}

	/**
	 * 字符串分割.
	 * 
	 * @author xiweicheng
	 * @creation 2013年11月30日 下午6:33:03
	 * @modification 2013年11月30日 下午6:33:03
	 * @param val
	 * @param decollator
	 * @return
	 */
	public static String[] split(String val, String decollator) {

		if (!isEmpty(val)) {
			return val.split(decollator);
		}

		return null;
	}

	/**
	 * 获取一组值.
	 * 
	 * @author xiweicheng
	 * @creation 2013年11月30日 下午5:58:37
	 * @modification 2013年11月30日 下午5:58:37
	 * @param map
	 * @param keyArr
	 * @return
	 */
	public static String[] getValues(Map<String, String> map, String... keyArr) {

		String[] objArr = new String[keyArr.length];

		if (keyArr.length > 0 && map != null && map.size() > 0) {
			int i = 0;

			for (String key : keyArr) {
				objArr[i++] = map.get(key);
			}
		}

		return objArr;
	}

	/**
	 * HTML标签转义方法 —— java代码库
	 * 
	 * @param content
	 * @return
	 */
	public static String html(String content) {

		if (content == null) {
			return "";
		}

		String html = content;
		html = html.replace("'", "&apos;");
		html = html.replace("\"", "&quot;");
		html = html.replace("\t", "&nbsp;&nbsp;");// 替换跳格
		html = html.replace("<", "&lt;");
		html = html.replace(">", "&gt;");

		return html;
	}

	/**
	 * 使用textarea包裹返回到前端的json body
	 * 
	 * @param jsonBody
	 * @return
	 */
	public static String wrapByTextarea(String jsonBody) {
		return String.format(
				"<textarea data-type='application/json'>%s</textarea>",
				jsonBody);
	}
	
	public static String mdParse(String markdown) {
		Options options = new  Options();
		options.setBreaks(true);

		return Marked.marked(markdown, options);
	}
	
	public static String md2Html(String markdown) {
		StringBuffer sb = new StringBuffer();
		sb.append("<div class=\"markdown-body\"/>");
		sb.append("<style>.markdown-body{font-size:14px;line-height:1.6}.markdown-body>:first-child{margin-top:0!important}.markdown-body>:last-child{margin-bottom:0!important}.markdown-body a{word-break:break-all}.markdown-body a.absent{color:#C00}.markdown-body a.anchor{bottom:0;cursor:pointer;display:block;left:0;margin-left:-30px;padding-left:30px;position:absolute;top:0}.markdown-body h1,.markdown-body h2,.markdown-body h3,.markdown-body h4,.markdown-body h5,.markdown-body h6{cursor:text;font-weight:700;margin:20px 0 10px;padding:0;position:relative}.markdown-body h1 .mini-icon-link,.markdown-body h2 .mini-icon-link,.markdown-body h3 .mini-icon-link,.markdown-body h4 .mini-icon-link,.markdown-body h5 .mini-icon-link,.markdown-body h6 .mini-icon-link{color:#000;display:none}.markdown-body h1:hover a.anchor,.markdown-body h2:hover a.anchor,.markdown-body h3:hover a.anchor,.markdown-body h4:hover a.anchor,.markdown-body h5:hover a.anchor,.markdown-body h6:hover a.anchor{line-height:1;margin-left:-22px;padding-left:0;text-decoration:none;top:15%}.markdown-body h1:hover a.anchor .mini-icon-link,.markdown-body h2:hover a.anchor .mini-icon-link,.markdown-body h3:hover a.anchor .mini-icon-link,.markdown-body h4:hover a.anchor .mini-icon-link,.markdown-body h5:hover a.anchor .mini-icon-link,.markdown-body h6:hover a.anchor .mini-icon-link{display:inline-block}.markdown-body hr:after,.markdown-body hr:before{display:table;content:\"\"}.markdown-body h1 code,.markdown-body h1 tt,.markdown-body h2 code,.markdown-body h2 tt,.markdown-body h3 code,.markdown-body h3 tt,.markdown-body h4 code,.markdown-body h4 tt,.markdown-body h5 code,.markdown-body h5 tt,.markdown-body h6 code,.markdown-body h6 tt{font-size:inherit}.markdown-body h1{color:#000;font-size:28px}.markdown-body h2{border-bottom:1px solid #CCC;color:#000;font-size:24px}.markdown-body h3{font-size:18px}.markdown-body h4{font-size:16px}.markdown-body h5{font-size:14px}.markdown-body h6{color:#777;font-size:14px}.markdown-body blockquote,.markdown-body dl,.markdown-body ol,.markdown-body p,.markdown-body pre,.markdown-body table,.markdown-body ul{margin:15px 0}.markdown-body hr{overflow:hidden;background:#e7e7e7;height:4px;padding:0;margin:16px 0;border:0;-moz-box-sizing:content-box;box-sizing:content-box}.markdown-body h1+p,.markdown-body h2+p,.markdown-body h3+p,.markdown-body h4+p,.markdown-body h5+p,.markdown-body h6+p,.markdown-body ol li>:first-child,.markdown-body ul li>:first-child{margin-top:0}.markdown-body hr:after{clear:both}.markdown-body a:first-child h1,.markdown-body a:first-child h2,.markdown-body a:first-child h3,.markdown-body a:first-child h4,.markdown-body a:first-child h5,.markdown-body a:first-child h6,.markdown-body>h1:first-child,.markdown-body>h1:first-child+h2,.markdown-body>h2:first-child,.markdown-body>h3:first-child,.markdown-body>h4:first-child,.markdown-body>h5:first-child,.markdown-body>h6:first-child{margin-top:0;padding-top:0}.markdown-body li p.first{display:inline-block}.markdown-body ol,.markdown-body ul{padding-left:30px}.markdown-body ol.no-list,.markdown-body ul.no-list{list-style-type:none;padding:0}.markdown-body ol ol,.markdown-body ol ul,.markdown-body ul ol,.markdown-body ul ul{margin-bottom:0}.markdown-body dl{padding:0}.markdown-body dl dt{font-size:14px;font-style:italic;font-weight:700;margin:15px 0 5px;padding:0}.markdown-body dl dt:first-child{padding:0}.markdown-body dl dt>:first-child{margin-top:0}.markdown-body dl dt>:last-child{margin-bottom:0}.markdown-body dl dd{margin:0 0 15px;padding:0 15px}.markdown-body blockquote>:first-child,.markdown-body dl dd>:first-child{margin-top:0}.markdown-body blockquote>:last-child,.markdown-body dl dd>:last-child{margin-bottom:0}.markdown-body blockquote{border-left:4px solid #DDD;color:#777;padding:0 15px}.markdown-body table th{font-weight:700}.markdown-body table td,.markdown-body table th{border:1px solid #CCC;padding:6px 13px}.markdown-body table tr{background-color:#FFF;border-top:1px solid #CCC}.markdown-body table tr:nth-child(2n){background-color:#F8F8F8}.markdown-body img{max-width:100%}.markdown-body span.frame{display:block;overflow:hidden}.markdown-body span.frame>span{border:1px solid #DDD;display:block;float:left;margin:13px 0 0;overflow:hidden;padding:7px;width:auto}.markdown-body span.frame span img{display:block;float:left}.markdown-body span.frame span span{clear:both;color:#333;display:block;padding:5px 0 0}.markdown-body span.align-center{clear:both;display:block;overflow:hidden}.markdown-body span.align-center>span{display:block;margin:13px auto 0;overflow:hidden;text-align:center}.markdown-body span.align-center span img{margin:0 auto;text-align:center}.markdown-body span.align-right{clear:both;display:block;overflow:hidden}.markdown-body span.align-right>span{display:block;margin:13px 0 0;overflow:hidden;text-align:right}.markdown-body span.align-right span img{margin:0;text-align:right}.markdown-body span.float-left{display:block;float:left;margin-right:13px;overflow:hidden}.markdown-body span.float-left span{margin:13px 0 0}.markdown-body span.float-right{display:block;float:right;margin-left:13px;overflow:hidden}.markdown-body span.float-right>span{display:block;margin:13px auto 0;overflow:hidden;text-align:right}.markdown-body code,.markdown-body tt{background-color:#F8F8F8;border:1px solid #EAEAEA;border-radius:3px;margin:0 2px;padding:0 5px;white-space:normal}.markdown-body pre>code{background:none;border:none;margin:0;padding:0;white-space:pre}.markdown-body .highlight pre,.markdown-body pre{background-color:#F8F8F8;border:1px solid #CCC;border-radius:3px;font-size:13px;line-height:19px;overflow:auto;padding:6px 10px}.markdown-body pre code,.markdown-body pre tt{background-color:transparent;border:none}.markdown-body .emoji{width:1.5em;height:1.5em;display:inline-block;margin-bottom:-.25em;background-size:contain;}</style>");
		sb.append(StringUtil.mdParse(markdown));
		sb.append("</div>");
		return sb.toString();
	}
	
	public static String nl2br(String content) {
		if(StringUtil.isNotEmpty(content)) {
			return content.replaceAll("\n", "<br/>");
		}
		
		return content;
	}
	
	public static String encodingFileName(String fileName) {
		String returnFileName = "";
		try {
			returnFileName = URLEncoder.encode(fileName, "UTF-8");
			returnFileName = StringUtils.replace(returnFileName, "+", "%20");
			if (returnFileName.length() > 100) {
				returnFileName = new String(fileName.getBytes("GBK"),
						"ISO8859-1");
				returnFileName = StringUtils.replace(returnFileName, " ",
						"%20");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return returnFileName;
	}
	
	public static String parseUrl(String href) {
		
		try {
			URL url = new URL(href);
			String v = url.getProtocol() + "://" + url.getHost();
			int p = url.getPort();
			if(p != -1 && p != 80 && p != 443) {
				v = v + ":" + p;
			}
			
			return v;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "";
		}
		
	}

}
