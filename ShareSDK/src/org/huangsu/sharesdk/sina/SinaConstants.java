package org.huangsu.sharesdk.sina;

public interface SinaConstants {

	/**
	 * 基础访问地址
	 */
	public final static String BASEURL = "https://api.weibo.com/2";
	/**
	 * 获取用户基本信息地址（只能使用get方式请求,地址都是相对于基础地址来的，下同）
	 */
	public final static String BASEUSERINFO = "/users/show.json";

	/**
	 * 发布一条微博消息（只能使用post方式请求）
	 */
	public final static String PUBLISHWEBO = "/statuses/update.json";
	/**
	 * 上传图片并发布一条微博(即包含本地图片以及文本内容,只能使用post方式请求)
	 */
	public final static String PUBLISHWEBOIMG = "/statuses/upload.json";
	/**
	 * 发布一条微博同时指定上传的图片或图片url （高级接口，只能使用post方式请求）
	 */
	public final static String PUBLISHWEBOURL = "/statuses/upload_url_text.json";

}
