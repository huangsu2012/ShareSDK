package org.huangsu.sharesdk.core;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.huangsu.sharesdk.bean.AccessToken;
import org.huangsu.sharesdk.bean.BasicUserInfo;
import org.huangsu.sharesdk.bean.PlatformInfo;
import org.huangsu.sharesdk.bean.ShareParams;
import org.huangsu.sharesdk.listener.BasicUserInfoListener;
import org.huangsu.sharesdk.listener.OauthResultListener;
import org.huangsu.sharesdk.listener.ResponseListener;
import org.huangsu.sharesdk.listener.ShareResultListener;
import org.huangsu.sharesdk.network.NetworkClient;
import org.huangsu.sharesdk.util.LogUtil;
import org.huangsu.sharesdk.util.XMLUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

import com.google.gson.JsonObject;

public abstract class Platform implements PlatformConstants {
	protected final PlatformInfo info;
	protected Context context;
	private boolean isInit = false;
	protected String sharedPrefPrefix = "sharesdk_";
	protected NetworkClient client;
	protected final static int DEFAULT_OAUTH_CODE = 8888;
	protected final static int DEFAULT_SSO_CODE = 5657;
	protected String dots = "...";
	protected Charset charset = Charset.forName("UTF-8");
	final static String LOGINACTION = "org.huangsu.sharesdk.core.loginAction";
	final static String SHAREACTION = "org.huangsu.sharesdk.core.shareAction";

	public PlatformInfo getInfo() {
		return info;
	}

	protected Platform(Context context, NetworkClient client) {
		if (context == null || client == null) {
			throw new IllegalArgumentException(
					"context and client must not be null");
		}
		info = new PlatformInfo(getPlatformid());
		info.nameId = getNameId();
		this.context = context.getApplicationContext();
		this.client = client;
	}

	protected abstract String getPlatformid();

	protected abstract int getNameId();

	/**
	 * 分享之前是否需要授权
	 * 
	 * @return
	 */
	protected abstract boolean shouldOauthBeforeShare();

	public abstract Map<String, String> getCodeReqParams();

	public String getCodeFromUrl(String url) {
		if (!TextUtils.isEmpty(url)) {
			return url.substring(url.indexOf("code=") + "code=".length());
		}
		return null;
	}

	protected abstract Map<String, String> getAccessTokenReqParams(String code);

	protected abstract AccessToken parseToken(JsonObject jsonObject);

	public void getAccessTokenWithCode(String code, OauthResultListener listener) {
		if (TextUtils.isEmpty(code) || listener == null) {
			return;
		}
		Map<String, String> params = getAccessTokenReqParams(code);
		if (params != null) {
			final OauthResultListener tempListener = listener;
			client.get(info.accesstokenurl, null, params,
					new ResponseListener() {
						@Override
						public void onSuccess(JsonObject result) {
							tempListener.onSuccess(parseToken(result));
						}

						@Override
						public void onError(String msg, Throwable error) {
							tempListener.onError(null, error);
						}
					});
		} else {
			LogUtil.wtf("the %s config  is not completed", info.platformid);
			listener.onError(String.format("the %s config is not completed",
					info.platformid), null);
		}
	}

	/**
	 * 授权
	 * 
	 * @param activity
	 */
	protected void oauth(ProxyActivity activity, Long transaction) {
		initInfo();
		if (activity == null || transaction == null) {
			throw new IllegalArgumentException(
					"activity and transaction must not be null");
		}
		try {
			startOauthActivity(activity);
		} catch (ClassNotFoundException e) {
			LogUtil.e(e, "");
		}
	}

	private void startOauthActivity(ProxyActivity activity)
			throws ClassNotFoundException {
		Class<?> clazz = Class.forName(getOauthActivityClassName());
		Intent intent = new Intent(activity, clazz);
		intent.putExtra("platformid", info.platformid);
		activity.startActivityForResult(intent, DEFAULT_OAUTH_CODE);
	}

	private String getOauthActivityClassName() {
		String result = null;
		ApplicationInfo appInfo;
		try {
			appInfo = context.getPackageManager().getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
			result = appInfo.metaData.getString("oauthActivity");
		} catch (NameNotFoundException e) {
		}
		return result;
	}

	/**
	 * 处理oauthActivity返回的结果
	 * 
	 */
	protected void handleOauthResult(int requestCode, int resultCode,
			Intent data, ProxyActivity activity) {
		if (requestCode == DEFAULT_OAUTH_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				AccessToken accessToken = data
						.getParcelableExtra("accessToken");
				saveAccessToken(accessToken);
				if (accessToken != null) {
					activity.onSuccess(accessToken);
				} else {
					activity.onError("illegal data", null);
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				activity.onCancel();
			} else {
				activity.onError(data.getStringExtra("msg"), null);
			}
		}
	}

	/**
	 * 授权过期后重新进行授权
	 * 
	 * @param activity
	 */
	protected void reoauth(ProxyActivity activity, Long transaction) {
		oauth(activity, transaction);
	}

	/**
	 * 登录三方平台
	 * 
	 * @param activity
	 * @param listener
	 *            登录后的返回结果
	 * @param forceLogin
	 *            是否强制登录 如果为true表示不管之前是否登录过都重新登录
	 */
	public final void login(OauthResultListener listener, boolean forceLogin) {
		initInfo();
		if (forceLogin) {
			doLogin(listener);
		} else {
			if (!isOauth() || isExpired()) {
				doLogin(listener);
			} else {
				if (listener != null) {
					listener.onSuccess(getAccessToken());
				}
			}
		}
	}

	private void doLogin(OauthResultListener listener) {
		long transaction = System.currentTimeMillis();
		ProxyActivity.addOauthListener(transaction, listener);
		startProxyActivity(transaction, LOGINACTION);
	}

	private void startProxyActivity(long transaction, String action) {
		Intent intent = new Intent(context, ProxyActivity.class);
		intent.putExtra("transaction", transaction);
		intent.putExtra("platformid", info.platformid);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(action);
		context.startActivity(intent);
	}

	/**
	 * 是否已经授权
	 * 
	 * @return
	 */
	protected boolean isOauth() {
		SharedPreferences preferences = context.getSharedPreferences(
				sharedPrefPrefix + info.platformid, Context.MODE_PRIVATE);
		if (preferences != null
				&& !TextUtils.isEmpty(preferences.getString(TOKEN, null))) {
			return true;
		}
		return false;
	}

	/**
	 * 授权是否过期
	 * 
	 * @return
	 */
	protected boolean isExpired() {
		SharedPreferences preferences = context.getSharedPreferences(
				sharedPrefPrefix + info.platformid, Context.MODE_PRIVATE);
		if (preferences != null
				&& preferences.getLong(EXPIREDIN, 0) > System
						.currentTimeMillis()) {
			return false;
		}
		return true;
	}

	public void logout() {
		SharedPreferences preferences = context.getSharedPreferences(
				sharedPrefPrefix + info.platformid, Activity.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(TOKEN, null);
		editor.putLong(EXPIREDIN, 0);
		editor.putString(UID, null);
		editor.putString(REFRESHTOKEN, null);
		editor.commit();
	}

	public final void share(ShareParams params, ShareResultListener listener) {
		if (!validateShareParams(params, listener)) {
			return;
		}
		if (shouldOauthBeforeShare()) {
			final ShareParams tempParams = params;
			final ShareResultListener tempListener = listener;
			login(new OauthResultListener() {
				@Override
				public void onSuccess(AccessToken accessToken) {
					if (accessToken != null) {
						startShare(tempListener, tempParams);
					} else {
						tempListener.onError("authorize failed", null);
					}
				}

				@Override
				public void onError(String msg, Throwable throwable) {
					tempListener.onError(msg, throwable);
				}

				@Override
				public void onCancel() {
					tempListener.onCancel();
				}
			}, false);

		} else {
			initInfo();
			startShare(listener, params);
		}
	}

	private void startShare(ShareResultListener listener, ShareParams params) {
		LogUtil.d("start share to %s", info.platformid);
		listener.onShareStart();
		long trans = System.currentTimeMillis();
		ProxyActivity.addShareResultListener(trans, params, listener);
		startProxyActivity(trans, SHAREACTION);
	}

	protected abstract void doShare(ProxyActivity activity, ShareParams params);

	protected ResponseListener getWrapperedListener(ShareResultListener listener) {
		if (listener != null) {
			return new ShareResponseListener(listener);
		}
		return null;
	}

	protected boolean validateShareParams(ShareParams params,
			ShareResultListener listener) {
		if (params == null || TextUtils.isEmpty(params.getContent())) {
			LogUtil.e("the share content must not be null");
			if (listener != null) {
				listener.onError("the share content must not be null", null);
			}
			return false;
		}
		return true;
	}

	/**
	 * 判断分享是否成功
	 * 
	 * @param jsonObject
	 * @return
	 */
	protected abstract boolean isShareSuccess(JsonObject jsonObject);

	protected abstract String getErrorMsg(JsonObject jsonObject);

	/**
	 * 初始化平台信息，如：appkey等
	 */
	protected void initInfo() {
		initInfo(info.platformid);
	}

	protected void initInfo(String platformid) {
		if (isInit || TextUtils.isEmpty(platformid)) {
			return;
		}
		XmlPullParser pullParser;
		try {
			pullParser = XMLUtil.getPullParserFromAssert(context, CONFIGFILE,
					"utf-8");
			Map<String, String> plaforms = XMLUtil.getAttributes(pullParser,
					null, PLATFORMS, null, null);
			if (plaforms == null) {
				LogUtil.e("the config file is not correct");
				return;
			}
			info.redirecturl = plaforms.get(REDIRECTURLS);
			Map<String, String> attributes = XMLUtil.getAttributes(pullParser,
					null, PLATFORM, ID, platformid);
			if (attributes != null) {
				info.authorizeurl = attributes.get(AUTHORIZEURL);
				String redirectUrl = attributes.get(REDIRECTURL);
				if (!TextUtils.isEmpty(redirectUrl)) {
					info.redirecturl = redirectUrl;
				}
				info.appkey = attributes.get(APPKEY);
				info.accesstokenurl = attributes.get(ACCESSTOKENURL);
				info.appid = attributes.get(APPID);
				info.appsecret = attributes.get(APPSECRET);
				info.scope = attributes.get(SCOPE);
				info.showpriority = Integer.parseInt(attributes
						.get(SHOWPRIORITY));
				isInit = true;
			} else {
				LogUtil.e("the platform:%s is not exit", info.platformid);
			}
		} catch (XmlPullParserException e) {
			LogUtil.e(e, "");
		} catch (IOException e) {
			LogUtil.e(e, "");
		}
	}

	/**
	 * 检查配置文件的完整性，主要在授权的时候有用
	 * 
	 * @return true表示授权时必须有的属性都有
	 */
	protected boolean checkCofigIntegrity() {
		return !TextUtils.isEmpty(info.redirecturl)
				&& !TextUtils.isEmpty(info.authorizeurl)
				&& !TextUtils.isEmpty(info.accesstokenurl);
	}

	protected void saveAccessToken(AccessToken accessToken) {
		if (accessToken != null) {
			saveAccessToken(accessToken.token, accessToken.uid,
					accessToken.expiresIn, accessToken.refreshToken);
		}
	}

	protected void saveAccessToken(String accessToken, String uid,
			long expiresIn) {
		saveAccessToken(accessToken, uid, expiresIn, null);
	}

	protected void saveAccessToken(String accessToken, String uid,
			long expiresIn, String refreshToken) {
		saveAccessToken(info.platformid, accessToken, uid, expiresIn,
				refreshToken);
	}

	protected void saveAccessToken(String platformid, String accessToken,
			String uid, long expiresIn, String refreshToken) {
		SharedPreferences preferences = context.getSharedPreferences(
				sharedPrefPrefix + platformid, Activity.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(TOKEN, accessToken);
		editor.putLong(EXPIREDIN, expiresIn);
		editor.putString(UID, uid);
		editor.putString(REFRESHTOKEN, refreshToken);
		editor.commit();
	}

	protected AccessToken getAccessToken() {
		return getAccessToken(info.platformid);
	}

	protected AccessToken getAccessToken(String platformid) {
		SharedPreferences preferences = context.getSharedPreferences(
				sharedPrefPrefix + platformid, Activity.MODE_PRIVATE);
		if (preferences != null) {
			String token = preferences.getString(TOKEN, null);
			String uid = preferences.getString(UID, null);
			long expiresIn = preferences.getLong(EXPIREDIN, 0);
			if (!TextUtils.isEmpty(token)) {
				return new AccessToken(token, uid, expiresIn,
						preferences.getString(REFRESHTOKEN, null));
			}
		}
		return null;
	}

	protected void saveBasicUserInfo(BasicUserInfo userInfo) {
		if (userInfo != null) {
			saveBasicUserInfo(userInfo.getGender(), userInfo.getAvatar(),
					userInfo.getName());
		}
	}

	protected void saveBasicUserInfo(int gender, String avatar, String name) {
		SharedPreferences preferences = context.getSharedPreferences(
				sharedPrefPrefix + info.platformid, Activity.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(USERAVATAR, avatar);
		editor.putInt(USERGENDER, gender);
		editor.putString(USERNAME, name);
		editor.commit();
	}

	protected BasicUserInfo getBasicUserInfo() {
		return getBasicUserInfo(info.platformid);
	}

	protected BasicUserInfo getBasicUserInfo(String platformid) {
		SharedPreferences preferences = context.getSharedPreferences(
				sharedPrefPrefix + platformid, Activity.MODE_PRIVATE);
		if (preferences != null) {
			String uid = preferences.getString(UID, null);
			int gender = preferences.getInt(USERGENDER, 0);
			String avatar = preferences.getString(USERAVATAR, null);
			String name = preferences.getString(USERNAME, null);
			if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(name)) {
				return new BasicUserInfo(uid, gender, avatar, name);
			}
		}
		return null;
	}

	public void getUserInfo(Activity activity, String uid,
			ResponseListener listener) {
		final ResponseListener responseListener = listener;
		final String uidTemp = uid;
		login(new OauthResultListener() {

			@Override
			public void onSuccess(AccessToken accessToken) {
				if (!TextUtils.isEmpty(uidTemp)) {
					doGetUserInfo(accessToken, uidTemp, responseListener);
				} else {
					doGetUserInfo(accessToken, accessToken.uid,
							responseListener);
				}
			}

			@Override
			public void onError(String msg, Throwable throwable) {
				responseListener.onError(msg, throwable);
			}

			@Override
			public void onCancel() {
				responseListener.onError("user cancel authorize", null);
			}
		}, false);
	}

	public void getBasicUserInfo(Activity activity, String uid,
			BasicUserInfoListener listener) {
		BasicUserInfo info = getBasicUserInfo();
		if (info == null) {
			final BasicUserInfoListener temp = listener;
			getUserInfo(activity, uid, new ResponseListener() {

				@Override
				public void onSuccess(JsonObject result) {
					BasicUserInfo basicUserInfo = parseBasicUserInfo(result);
					saveBasicUserInfo(basicUserInfo);
					if (basicUserInfo != null) {
						temp.onSuccess(basicUserInfo);
					} else {
						temp.onError("parse error", null);
					}
				}

				@Override
				public void onError(String msg, Throwable error) {
					temp.onError(msg, error);
				}
			});
		} else {
			listener.onSuccess(info);
		}
	}

	protected abstract void doGetUserInfo(AccessToken accessToken, String uid,
			ResponseListener listener);

	protected abstract BasicUserInfo parseBasicUserInfo(JsonObject jsonObject);

	private class ShareResponseListener implements ResponseListener {
		private ShareResultListener listener;

		public ShareResponseListener(ShareResultListener listener) {
			this.listener = listener;
		}

		@Override
		public void onError(String msg, Throwable error) {
			if (listener != null) {
				listener.onError(msg, error);
			}
		}

		@Override
		public void onSuccess(JsonObject result) {
			if (listener != null) {
				if (isShareSuccess(result)) {
					listener.onSuccess();
				} else {
					listener.onError(getErrorMsg(result), null);
				}
			}

		}

	}
}
