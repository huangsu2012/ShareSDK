package org.huangsu.sharesdk.core;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.huangsu.sharesdk.bean.ShareParams;
import org.huangsu.sharesdk.douban.DoubanPlatform;
import org.huangsu.sharesdk.listener.ShareResultListener;
import org.huangsu.sharesdk.network.NetworkClient;
import org.huangsu.sharesdk.network.VolleyNetworkClient;
import org.huangsu.sharesdk.renren.RenRenPlatform;
import org.huangsu.sharesdk.sina.SinaPlatform;
import org.huangsu.sharesdk.tencent.QQPlatform;
import org.huangsu.sharesdk.tencent.QzonePlatform;
import org.huangsu.sharesdk.tencent.TencentWeiboPlatform;
import org.huangsu.sharesdk.tencent.WechatMomentsPlatform;
import org.huangsu.sharesdk.tencent.WechatPlatform;
import org.huangsu.sharesdk.util.LogUtil;

import android.app.Activity;
import android.content.Context;

public class PlatformFactory implements PlatformConstants {
	private static PlatformFactory platformFactory;
	private NetworkClient client;
	private Context context;
	private Map<String, Platform> platforms;
	private Map<String, Class<? extends Platform>> platformClasses;

	private PlatformFactory(Context context, NetworkClient client) {
		this.context = context.getApplicationContext();
		platforms = new ConcurrentHashMap<String, Platform>();
		this.client = client;
		initPlatformClasses();

	}

	private void initPlatformClasses() {
		platformClasses = new HashMap<String, Class<? extends Platform>>();
		platformClasses.put(QQ, QQPlatform.class);
		platformClasses.put(QZONE, QzonePlatform.class);
		platformClasses.put(WECHAT, WechatPlatform.class);
		platformClasses.put(WECHATMOMENTS, WechatMomentsPlatform.class);
		platformClasses.put(SINAWEIBO, SinaPlatform.class);
		platformClasses.put(TENCENTWEIBO, TencentWeiboPlatform.class);
		platformClasses.put(RENREN, RenRenPlatform.class);
		platformClasses.put(DOUBAN, DoubanPlatform.class);
	}

	public static synchronized PlatformFactory getInstance(Context context,
			NetworkClient client) {
		if (platformFactory == null) {
			platformFactory = new PlatformFactory(context, client);
		}
		return platformFactory;
	}

	public static synchronized PlatformFactory getInstance(Context context) {
		return getInstance(context, VolleyNetworkClient.getInstance(context));
	}

	public Platform getPlatform(String platformid) {
		Platform platform = platforms.get(platformid);
		if (platform == null) {
			Class<? extends Platform> clazz = platformClasses.get(platformid);
			if (clazz != null) {
				try {
					platform = clazz.getConstructor(Context.class,
							NetworkClient.class).newInstance(context, client);
				} catch (InstantiationException e) {
					LogUtil.e(e, "");
				} catch (IllegalAccessException e) {
					LogUtil.e(e, "");
				} catch (IllegalArgumentException e) {
					LogUtil.e(e, "");
				} catch (InvocationTargetException e) {
					LogUtil.e(e, "");
				} catch (NoSuchMethodException e) {
					LogUtil.e(e, "");
				}
			}
			if (platform != null) {
				platforms.put(platformid, platform);
			}
		}
		return platform;
	}

	public void share(String platformid, Activity activity, ShareParams params,
			ShareResultListener listener) {
		Platform platform = getPlatform(platformid);
		if (platform != null) {
			platform.share(params, listener);
		} else {
			if (listener != null) {
				listener.onError("the platform id is illegal", null);
			}
		}
	}

	public void destroy() {
		if (platformClasses != null) {
			platformClasses.clear();
		}
		if (platforms != null) {
			platforms.clear();
		}
		platformFactory = null;
		client = null;
		platformClasses = null;
		platforms = null;
		context = null;
	}
}
