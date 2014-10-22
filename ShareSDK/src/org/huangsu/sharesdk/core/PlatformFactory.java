package org.huangsu.sharesdk.core;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.huangsu.sharesdk.bean.ShareParams;
import org.huangsu.sharesdk.douban.DoubanPlatform;
import org.huangsu.sharesdk.listener.ShareResultListener;
import org.huangsu.sharesdk.renren.RenRenPlatform;
import org.huangsu.sharesdk.sina.SinaPlatform;
import org.huangsu.sharesdk.tencent.QQPlatform;
import org.huangsu.sharesdk.tencent.QzonePlatform;
import org.huangsu.sharesdk.tencent.TencentWeiboPlatform;
import org.huangsu.sharesdk.tencent.WechatMomentsPlatform;
import org.huangsu.sharesdk.tencent.WechatPlatform;
import org.huangsu.sharesdk.util.LogUtil;

import android.content.Context;

/**
 * The factory is used to manage plaforms
 * 
 * @author huangsu2012@gmail.com
 * 
 */
public class PlatformFactory implements PlatformConstants {
	private static PlatformFactory platformFactory;
	private NetworkClient client;
	private Context context;
	private DataManager dataManager;
	private Map<String, Platform> platforms;
	private Map<String, Class<? extends Platform>> platformClasses;
	private boolean inited = false;

	private PlatformFactory(Context context) {
		this.context = context.getApplicationContext();
		platforms = new ConcurrentHashMap<String, Platform>();
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

	public static synchronized PlatformFactory getInstance(Context context) {
		if (platformFactory == null) {
			if (context == null) {
				throw new NullPointerException("the context is null");
			}
			platformFactory = new PlatformFactory(context);
		}
		return platformFactory;
	}

	/**
	 * You should invoke this function when you want supply your own
	 * implementation of<a>NetworkClient<a> and <a>DataManager<a>
	 * 
	 * @param client
	 * @param dataManager
	 */
	public PlatformFactory init(NetworkClient client, DataManager dataManager) {
		if (dataManager == null) {
			dataManager = new DefaultDataManager(context, null);
		}
		if (client == null) {
			client = new DefaultNetworkClient();
		}
		this.client = client;
		this.dataManager = dataManager;
		inited = true;
		return platformFactory;
	}

	public PlatformFactory init() {
		if (!inited) {
			init(new DefaultNetworkClient(), new DefaultDataManager(context,
					null));
		}
		return platformFactory;
	}

	public Platform getPlatform(String platformid) {
		init();
		Platform platform = platforms.get(platformid);
		if (platform == null) {
			Class<? extends Platform> clazz = platformClasses.get(platformid);
			if (clazz != null) {
				try {
					platform = clazz.getConstructor(Context.class,
							NetworkClient.class, DataManager.class)
							.newInstance(context, client, dataManager);
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

	public void share(String platformid, ShareParams params,
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
		dataManager = null;
	}
}
