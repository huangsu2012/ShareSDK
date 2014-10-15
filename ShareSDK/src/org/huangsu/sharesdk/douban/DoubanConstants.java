package org.huangsu.sharesdk.douban;

public interface DoubanConstants {
	public final static int TOKENEXPIRED=106;
	/**
	 * AccessToken刷新，POST方式请求
	 */
	public final static String REFERESHTOKEN = "https://www.douban.com/service/auth2/token";
	/**
	 * 发广播消息
	 */
	public final static String SENDRADIO = "https://api.douban.com/shuo/v2/statuses/";
	/**
	 * 获取用户基本信息地址（只能使用get方式请求）
	 */
	public final static String BASEUSERINFO = "https://api.douban.com/v2/user";

}
