package org.huangsu.sharesdk.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.webkit.MimeTypeMap;

public class StringUtil {
	public static final Pattern CHINESSEPATTERN = Pattern
			.compile("[\\u4e00-\\u9fa5]");

	public static boolean isEmpty(CharSequence charSequence) {
		return charSequence == null || charSequence.length() == 0;
	}

	/**
	 * 某些情况下字符串长度是有限制的，比如分享某些内容的时候，这个时候为了保证字符串长度符合规定，就将超过最大长度的部分用省略号等替代
	 * 
	 * @param origin
	 *            原始字符串
	 * @param maxlength
	 *            字符串允许最大长度(以汉字为1个长度计算)
	 * @param dots
	 *            超过最大长度的字符串的替代字符串（如：...）
	 * @return 返回原始字符串或者经过处理后的字符串
	 */
	public static String subStringOfOrigin(String origin, float maxlength,
			String dots) {
		if (!isEmpty(origin) && !isEmpty(dots)) {
			float originLen = getLengthOfString(origin);
			if (originLen > maxlength) {
				return origin.substring(0,
						(int) maxlength - Math.round(getLengthOfString(dots)))
						+ dots;
			}
		}
		return origin;
	}

	public static float getLengthOfString(CharSequence origin) {
		if (isEmpty(origin)) {
			return 0;
		}
		float valueLength = 0;
		String originString = origin.toString();
		// 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
		for (int i = 0; i < originString.length(); i++) {
			// 获取一个字符
			String temp = originString.substring(i, i + 1);
			Matcher matcher = CHINESSEPATTERN.matcher(temp);
			// 判断是否为中文字符
			if (matcher.matches()) {
				// 中文字符长度为1
				valueLength += 1;
			} else {
				// 其他字符长度为0.5
				valueLength += 0.5;
			}
		}
		return valueLength;
	}

	@SuppressLint("DefaultLocale")
	public static String getMimeType(String url) {
		String mimeType = null;
		if (!StringUtil.isEmpty(url)) {
			String suffix = MimeTypeMap.getFileExtensionFromUrl(url);
			if (isEmpty(suffix)) {
				suffix = url.substring(url.lastIndexOf(".") + 1);
			}
			if (!isEmpty(suffix)) {
				int index = suffix.indexOf("@");
				if (index != -1) {
					suffix = suffix.substring(0, index);
				}
				mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
						suffix.toLowerCase(Locale.US));
			}
		}
		return mimeType;
	}
}
