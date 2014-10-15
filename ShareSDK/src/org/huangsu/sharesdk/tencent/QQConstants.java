package org.huangsu.sharesdk.tencent;

public interface QQConstants {
	public final static int TOKENEXPIRED = 100014;
	/**
	 * 基础访问地址
	 */
	public final static String BASEURL = "https://graph.qq.com";
	/**
	 * 获取授权用户基本信息地址（只能使用get方式请求,地址都是相对于基础地址来的，下同）
	 */
	public final static String BASEUSERINFO = "/user/get_user_info";
	/**
	 * 发表日志到QQ空间(只能使用POST方式请求)
	 */
	public final static String ADDONEBLOG = "/blog/add_one_blog";
	/**
	 * 获取用户粉丝列表
	 */
	public final static String WEIBOFANS = "/relation/get_fanslist";
	/**
	 * 获取用户偶像列表
	 */
	public final static String WEIBOIDOLS = "/relation/get_idollist";

	/**
	 * 发布一条微博消息（只能使用post方式请求）
	 */

	public final static String PUBLISHWEBO = "/t/add_t";
	/**
	 * 上传图片并发布一条微博(即包含本地图片以及文本内容,只能使用post方式请求)
	 */
	public final static String PUBLISHWEBOIMG = "/t/add_pic_t";

	public final static String SHARTOQZONE = "/share/add_share";
}
