package org.huangsu.sharesdk.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.text.TextUtils;

public class XMLUtil {
	public static XmlPullParser getPullParserFromClass(ClassLoader classLoader,
			String fileName, String encoding) throws XmlPullParserException {
		return getPullParser(classLoader.getResourceAsStream(fileName),
				encoding);
	}

	public static XmlPullParser getPullParserFromAssert(Context context,
			String fileName, String encoding) throws XmlPullParserException,
			IOException {
		return getPullParser(
				context.getAssets().open(fileName, Context.MODE_PRIVATE),
				encoding);
	}

	public static XmlPullParser getPullParser(InputStream in, String encoding)
			throws XmlPullParserException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser pullParser = null;
		pullParser = factory.newPullParser();
		pullParser.setInput(in, encoding);
		return pullParser;
	}

	/**
	 * 获取xml文档中第一个符合条件的元素的所有属性及属性值
	 * 
	 * @param pullParser
	 * @param namespace
	 *            命名空间
	 * @param elementName
	 *            元素名
	 * @param attributeName
	 *            该元素具有的属性名,可以为空（注意该值如果为空会自动忽略attributeValue）
	 * @param attributeValue
	 *            该元素具有的与属性名对应的值
	 * @return 包含属性名与属性值的Map或者null(如果没有找到的话)
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static Map<String, String> getAttributes(XmlPullParser pullParser,
			String namespace, String elementName, String attributeName,
			String attributeValue) throws XmlPullParserException, IOException {
		Map<String, String> attributes = null;
		boolean shouldEndParse = false;
		if (pullParser != null) {
			if (!TextUtils.isEmpty(elementName)) {
				int event = pullParser.getEventType();
				while (event != XmlPullParser.END_DOCUMENT && !shouldEndParse) {
					switch (event) {
					case XmlPullParser.START_TAG:
						if (elementName.equals(pullParser.getName())) {
//							VolleyLog.d("parsing tag is %s", pullParser.getName());
							if (!TextUtils.isEmpty(attributeName)
									&& !TextUtils.isEmpty(attributeValue)) {
								// VolleyLog.d("attributeName %s",attributeName);
								// VolleyLog.d("attributeValue %s",attributeValue);
								if (attributeValue.equals(pullParser
										.getAttributeValue(namespace,
												attributeName))) {
//									VolleyLog.d("finded the suit element");
									attributes = getAttributes(pullParser);
									shouldEndParse = true;
								}
							} else {
								attributes = getAttributes(pullParser);
								shouldEndParse = true;
							}
						}
						break;
					}
					event = pullParser.next();
				}
			}
		}
		return attributes;
	}

	private static Map<String, String> getAttributes(XmlPullParser pullParser) {
		Map<String, String> attributes = new HashMap<String, String>(
				pullParser.getAttributeCount());
		for (int i = 0; i < pullParser.getAttributeCount(); i++) {
			attributes.put(pullParser.getAttributeName(i),
					pullParser.getAttributeValue(i));
		}
		return attributes;
	}
}
