package org.huangsu.sharesdk.core;

/**
 * 与三方平台有关的一些常量
 * 
 * @author huangsu2012@gmail.com 2013-10-10
 * 
 */
public interface PlatformConstants {
	final static String CONFIGFILE = "platforms.xml";
	/**
	 * 以下为元素名
	 */
	final static String PLATFORMS = "plaforms";
	final static String PLATFORM = "platform";

	/**
	 * 以下为属性名
	 */
	final static String REDIRECTURLS = "redirecturls";// 跟redirecturl是一个意思，只是redirecturl优先级高于此
	final static String REDIRECTURL = "redirecturl";// 重定向url用户授权成功后的重定向url
	final static String ID = "id";// 平台id,唯一值
	final static String APPID = "appid";
	final static String APPKEY = "appkey";
	final static String APPSECRET = "appsecret";
	final static String AUTHORIZEURL = "authorizeurl";
	final static String ACCESSTOKENURL = "accesstokenurl";
	final static String SCOPE="scope";
	final static String SHOWPRIORITY="showpriority";
	/**
	 * 以下为id属性的值
	 */
	public final static String TENCENTWEIBO = "tecentweibo";
	public final static String SINAWEIBO = "sinaweibo";
	public final static String QQ = "qq";
	public final static String QZONE = "qzone";
	public final static String DOUBAN = "douban";
	public final static String RENREN = "renren";
	public final static String WECHAT = "wechat";
	public final static String WECHATMOMENTS = "wechatmoments";
}
