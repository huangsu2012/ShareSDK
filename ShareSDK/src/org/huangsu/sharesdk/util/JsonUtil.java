package org.huangsu.sharesdk.util;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

public class JsonUtil {
	private JsonUtil() {

	}

	/**
	 * 将请求参数封装类转换为json字符串
	 * 
	 * @param model
	 * @return
	 */
	public static String constructJSON(Object model) {
		if (model == null) {
			return null;
		}
		Gson gson = new Gson();
		return gson.toJson(model);
	}

	/**
	 * 将返回的字符串封装成相应的model类
	 * 
	 * @param json
	 * @return
	 */
	public static <T> T pasrseFromJSON(String json, Class<T> clazz) {
		T model = null;
		try {
			if (!StringUtil.isEmpty(json)) {
				Gson gson = new Gson();
				model = gson.fromJson(json, clazz);
			}
		} catch (Exception ex) {
			LogUtil.e(ex, "");
			return null;
		}
		return model;
	}

	public static <T> T pasrseFromJSON(String json, TypeToken<T> typeToken) {
		T result = null;
		if (typeToken != null && !StringUtil.isEmpty(json)) {
			Gson gson = new Gson();
			result = gson.fromJson(json, typeToken.getType());
		}
		return result;
	}

	public static JsonObject getJsonObjectFromJsonStr(String json) {
		if (StringUtil.isEmpty(json)) {
			return null;
		}
		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(json);
		if (element.isJsonObject()) {
			JsonObject jsonObject = element.getAsJsonObject();
			return jsonObject;
		}
		return null;
	}

	/**
	 * 从json字符串中解析出相应的key对应的值
	 * 
	 * @param clazz
	 *            value值的类型
	 * @param json
	 *            要解析的json字符串
	 * @param key
	 *            要获取的value的name
	 * @return T
	 */
	public static <T> T getAttribute(Class<T> clazz, String json, String key) {
		return getAttribute(clazz, getJsonObjectFromJsonStr(json), key);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getAttribute(Class<T> clazz, JsonObject jsonObject,
			String key) {
		T result = null;
		if (jsonObject != null) {
			if (!StringUtil.isEmpty(key) && jsonObject.has(key)) {
				JsonElement jsonElement = jsonObject.get(key);
				if (clazz == String.class) {
					result = (T) jsonElement.getAsString();
				} else if (clazz == Integer.class || clazz == int.class) {
					result = (T) Integer.valueOf(jsonElement.getAsInt());
				} else if (clazz == Long.class || clazz == long.class) {
					result = (T) Long.valueOf(jsonElement.getAsLong());
				} else if (clazz == Float.class || clazz == float.class) {
					result = (T) Float.valueOf(jsonElement.getAsFloat());
				} else if (clazz == Double.class || clazz == double.class) {
					result = (T) Double.valueOf(jsonElement.getAsDouble());
				} else if (clazz == Boolean.class || clazz == boolean.class) {
					result = (T) Boolean.valueOf(jsonElement.getAsBoolean());
				} else if (clazz == Byte.class || clazz == byte.class) {
					result = (T) Byte.valueOf(jsonElement.getAsByte());
				} else if (clazz == Character.class || clazz == char.class) {
					result = (T) Character
							.valueOf(jsonElement.getAsCharacter());
				} else if (clazz == Short.class || clazz == short.class) {
					result = (T) Short.valueOf(jsonElement.getAsShort());
				} else if (clazz == BigInteger.class) {
					result = (T) jsonElement.getAsBigInteger();
				} else if (clazz == BigDecimal.class) {
					result = (T) jsonElement.getAsBigDecimal();
				} else if (clazz == Number.class) {
					result = (T) jsonElement.getAsNumber();
				} else if (clazz == JsonNull.class) {
					if (jsonElement.isJsonNull()) {
						result = (T) jsonElement.getAsJsonNull();
					}
				} else if (clazz == JsonArray.class) {
					if (jsonElement.isJsonArray()) {
						result = (T) jsonElement.getAsJsonArray();
					}
				} else if (clazz == JsonPrimitive.class) {
					if (jsonElement.isJsonPrimitive()) {
						result = (T) jsonElement.getAsJsonPrimitive();
					}
				} else if (clazz == JsonObject.class) {
					if (jsonElement.isJsonObject()) {
						result = (T) jsonElement.getAsJsonObject();
					}
				}
			}
		}
		return result;
	}
}
