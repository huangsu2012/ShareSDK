package org.huangsu.sharesdk.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
	private List<String> sortedPlatformIds;
	private boolean inited = false;

	private PlatformFactory(Context context) {
		this.context = context.getApplicationContext();
		platforms = new ConcurrentHashMap<String, Platform>(8);
		initPlatformClasses();
	}

	private void initPlatformClasses() {
		platformClasses = new HashMap<String, Class<? extends Platform>>(8);
		platformClasses.put(QQ, QQPlatform.class);
		platformClasses.put(QZONE, QzonePlatform.class);
		platformClasses.put(WECHAT, WechatPlatform.class);
		platformClasses.put(WECHATMOMENTS, WechatMomentsPlatform.class);
		platformClasses.put(SINAWEIBO, SinaPlatform.class);
		platformClasses.put(TENCENTWEIBO, TencentWeiboPlatform.class);
		platformClasses.put(RENREN, RenRenPlatform.class);
		platformClasses.put(DOUBAN, DoubanPlatform.class);
	}

	public List<String> getSortedPlatformIds() {
		if (sortedPlatformIds == null) {
			sortedPlatformIds = new ArrayList<String>(platformClasses.size());
			Iterator<String> iterator = platformClasses.keySet().iterator();
			while (iterator.hasNext()) {
				String string = (String) iterator.next();
				Platform platform = getPlatform(string);
				platform.initInfo();
				sortedPlatformIds.add(string);
			}
			Collections.sort(sortedPlatformIds, new Comparator<String>() {
				@Override
				public int compare(String lhs, String rhs) {
					Platform l = getPlatform(lhs);
					Platform r = getPlatform(rhs);
					return l.getInfo().compareTo(r.getInfo());
				}
			});
		}
		return sortedPlatformIds;
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
	 * implementation of{@code NetworkClient} and {@code DataManager}
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
					Constructor<? extends Platform> constructor = clazz
							.getDeclaredConstructor(Context.class,
									NetworkClient.class, DataManager.class);
					constructor.setAccessible(true);
					platform = constructor.newInstance(context, client,
							dataManager);
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
			} else {
				LogUtil.d("the platform(%s) is not exit", platformid);
			}
			if (platform != null) {
				platforms.put(platformid, platform);
			} else {
				LogUtil.d("the platform(%s) is not exit", platformid);
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
		if (sortedPlatformIds != null) {
			sortedPlatformIds.clear();
		}
		platformFactory = null;
		client = null;
		platformClasses = null;
		platforms = null;
		context = null;
		dataManager = null;
		sortedPlatformIds = null;
	}
}
