package org.huangsu.sharesdk.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

public class URLUtil {
	private static String DEFAULTCHARSET = "UTF-8";

	public static String constructUrl(String baseUrl, Map<String, String> params) {
		return constructUrl(baseUrl, params, DEFAULTCHARSET);
	}

	/**
	 * 构建请求url
	 * 
	 * @param baseUrl
	 *            请求地址
	 * @param params
	 *            请求参数部分
	 * @param charsetName
	 *            对参数进行何种类型的编码，为null时表示不进行编码
	 * @return
	 * 
	 */
	public static String constructUrl(String baseUrl,
			Map<String, String> params, String charsetName) {
		StringBuilder url = new StringBuilder(baseUrl);
		if (params != null && !params.isEmpty()) {
			url.append("?");
			Iterator<String> iterator = params.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				if (StringUtil.isEmpty(charsetName)) {
					url.append(key + "=" + params.get(key));
				} else {
					try {
						url.append(key
								+ "="
								+ URLEncoder.encode(params.get(key),
										charsetName));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				url.append("&");
			}
			url.delete(url.lastIndexOf("&"), url.length());
		}
		return url.toString();
	}
}
