package org.huangsu.sharesdk.renren;

public interface RenRenConstants {
	/**
	 * 获取用户基本信息地址（只能使用get方式请求)
	 */
	public final static String BASEUSERINFO = "https://api.renren.com/v2/user/get";
	/**
	 * AccessToken刷新，get方式请求
	 */
	public final static String REFRESHTOKEN = "https://graph.renren.com/oauth/token";
	/**
	 * 添加新鲜事
	 */
	public final static String FEEDPUT = "https://api.renren.com/v2/feed/put";
	/**
	 * 上传图片
	 */
	public final static String UPLOADPIC = "https://api.renren.com/v2/photo/upload";
	/**
	 * 分享，post请求
	 */
	public final static String SHARE = "https://api.renren.com/v2/share/url/put";
}
