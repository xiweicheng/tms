package com.lhjz.portal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;

public class JsonUtil {

	private static Logger logger = Logger.getLogger(JsonUtil.class);

	private JsonUtil() {
	}

	private static final Gson gson1 = new GsonBuilder().disableHtmlEscaping().setDateFormat("yyyy/MM/dd HH:mm:ss")
			.create();
	private static final Gson gson2 = new GsonBuilder().disableHtmlEscaping().setDateFormat("yyyy/MM/dd HH:mm:ss")
			.setPrettyPrinting().create();

	private static JsonParser jsonParser = new JsonParser();

	/**
	 * object to json string.
	 * 
	 * @param object
	 * @return
	 */
	public static String toJson(Object object) {

		try {
			return gson1.toJson(object);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * pretty format json string.
	 * 
	 * @param json
	 * @return
	 */
	public static String toPrettyJson(String json) {

		try {
			JsonElement jsonElement = jsonParser.parse(json);

			return gson2.toJson(jsonElement);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * json string to object.
	 * 
	 * @param json
	 * @param classOfT
	 * @return
	 */
	public static <T> T json2Object(String json, Class<T> classOfT) {

		try {
			if (logger.isDebugEnabled()) {
				logger.debug(json);
			}
			return gson1.fromJson(json, classOfT);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * object to object<T>.
	 * 
	 * @param json
	 * @param classOfT
	 * @return
	 */
	public static <T> T Obj2Object(Object object, Class<T> classOfT) {

		try {
			if (logger.isDebugEnabled()) {
				logger.debug(toJson(object));
			}
			return gson1.fromJson(toJson(object), classOfT);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * map array to object List<T>.
	 * 
	 * @param map
	 * @param mapArrKey
	 * @param classOfT
	 * @return
	 */
	public static <T> List<T> mapArr2ObjectList(Map<?, ?> map, String mapArrKey, Class<T> classOfT) {
		List<T> list = new ArrayList<T>();

		try {
			if (map != null && map.size() > 0) {
				Object obj = map.get(mapArrKey);

				if (obj instanceof List<?>) {
					for (Object item : (List<?>) obj) {
						if (item instanceof Map<?, ?>) {
							list.add(JsonUtil.Obj2Object(item, classOfT));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return list;
	}

	/**
	 * Json string to JsonElement.
	 * 
	 * @param jsonObject
	 * @return
	 */
	public static JsonElement toJsonElement(String jsonObject) {

		try {
			return jsonParser.parse(jsonObject);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * Json path read value.
	 * @param json
	 * @param jsonPath
	 * @param filters
	 * @return
	 */
	public static <T> T read(String json, String jsonPath, Predicate... filters) {
		try {
			return JsonPath.read(json, jsonPath, filters);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}
}
