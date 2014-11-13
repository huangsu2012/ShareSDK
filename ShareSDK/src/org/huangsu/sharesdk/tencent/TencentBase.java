package org.huangsu.sharesdk.tencent;

import org.huangsu.sharesdk.bean.AccessToken;
import org.huangsu.sharesdk.bean.ShareParams;
import org.huangsu.sharesdk.core.DataManager;
import org.huangsu.sharesdk.core.NetworkClient;
import org.huangsu.sharesdk.core.Platform;
import org.huangsu.sharesdk.core.ProxyActivity;
import org.huangsu.sharesdk.listener.OauthResultListener;
import org.huangsu.sharesdk.listener.ShareResultListener;
import org.huangsu.sharesdk.util.GsonUtil;
import org.huangsu.sharesdk.util.LogUtil;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.tencent.utils.SystemUtils;

public abstract class TencentBase extends Platform {
	protected TencentBase(Context context, NetworkClient client,
			DataManager dataManager) {
		super(context, client, dataManager);
	}

	protected Tencent tencent;

	@Override
	protected void oauth(ProxyActivity activity, Long transaction) {
		if (activity == null || transaction == null) {
			throw new IllegalArgumentException(
					"activity and transaction must not be null");
		}
		initTencent();
		if (tencent == null) {
			String msg = "没有在AndroidManifest.xml中检测到com.tencent.tauth.AuthActivity,请加上com.tencent.open.AuthActivity,并配置<data android:scheme=\"tencent"
					+ info.appid + "\" />,详细信息请查看官网文档.";
			msg = msg
					+ "\n配置示例如下: \n<activity\n     android:name=\"com.tencent.connect.util.AuthActivity\"\n     android:noHistory=\"true\"\n     android:launchMode=\"singleTask\">\n<intent-filter>\n    <action android:name=\"android.intent.action.VIEW\" />\n     <category android:name=\"android.intent.category.DEFAULT\" />\n    <category android:name=\"android.intent.category.BROWSABLE\" />\n    <data android:scheme=\"tencent"
					+ info.appid + "\" />\n" + "</intent-filter>\n"
					+ "</activity>";
			LogUtil.e(msg);
			activity.onError(msg, null);
		}
		tencent.login(activity, info.scope, new OauthResultListenerWrapper(
				activity));
	}

	@Override
	protected AccessToken parseToken(JsonObject jsonObject) {
		Integer resultCode = GsonUtil.getAttribute(Integer.class, jsonObject,
				"ret");
		LogUtil.d("the ret code:%s", resultCode);
		if (resultCode != null && resultCode == 0) {
			String access_token = GsonUtil.getAttribute(String.class,
					jsonObject, "access_token");
			if (access_token != null) {
				long expiresin = GsonUtil.getAttribute(long.class, jsonObject,
						"expires_in") * 1000 + System.currentTimeMillis();
				String openid = GsonUtil.getAttribute(String.class, jsonObject,
						"openid");
				return new AccessToken(access_token, openid, expiresin);
			}
		}
		return null;
	}

	protected boolean isSupportSSOLogin() {
		String str = SystemUtils.getAppVersionName(context,
				"com.tencent.mobileqq");
		if (str == null) {
			return false;
		}
		if (SystemUtils.checkMobileQQ(context)) {
			return true;
		}
		return false;
	}

	@Override
	protected boolean checkCofigIntegrity() {
		return super.checkCofigIntegrity() && !TextUtils.isEmpty(info.appid)
				&& !TextUtils.isEmpty(info.appkey);
	}

	protected void initTencent() {
		initInfo();
		if (tencent == null) {
			tencent = Tencent.createInstance(info.appid, context);
		}
	}

	@Override
	public void logout() {
		super.logout();
		initTencent();
		tencent.logout(context);
	}

	@Override
	public boolean shouldOauthBeforeShare() {
		return false;
	}

	@Override
	protected void doShare(ProxyActivity activity, ShareParams params) {
		initTencent();
		if (tencent == null) {
			String msg = "没有在AndroidManifest.xml中检测到com.tencent.tauth.AuthActivity,请加上com.tencent.open.AuthActivity,并配置<data android:scheme=\"tencent"
					+ info.appid + "\" />,详细信息请查看官网文档.";
			msg = msg
					+ "\n配置示例如下: \n<activity\n     android:name=\"com.tencent.connect.util.AuthActivity\"\n     android:noHistory=\"true\"\n     android:launchMode=\"singleTask\">\n<intent-filter>\n    <action android:name=\"android.intent.action.VIEW\" />\n     <category android:name=\"android.intent.category.DEFAULT\" />\n    <category android:name=\"android.intent.category.BROWSABLE\" />\n    <data android:scheme=\"tencent"
					+ info.appid + "\" />\n" + "</intent-filter>\n"
					+ "</activity>";
			LogUtil.e(msg);
			activity.onError(msg, null);
			return;
		}
		IUiListener iUiListener = new DefaultShareUIListener(activity);
		doShare(activity, params, iUiListener);
	}

	protected void doShare(ProxyActivity activity, ShareParams params,
			IUiListener listener) {

	}

	protected String getAPPName() {
		PackageManager packageManager = context.getPackageManager();
		try {
			PackageInfo info = packageManager
					.getPackageInfo(context.getPackageName(),
							PackageManager.GET_CONFIGURATIONS);
			return info.applicationInfo.loadLabel(packageManager).toString();
		} catch (NameNotFoundException e) {
			LogUtil.e(e, "");
		}
		return null;
	}

	@Override
	protected boolean validateShareParams(ShareParams params,
			ShareResultListener listener) {
		if (super.validateShareParams(params, listener)) {
			if (TextUtils.isEmpty(params.getTitle())) {
				LogUtil.e("the title can't be empty");
				if (listener != null) {
					listener.onError("the title can't be empty", null);
				}
				return false;
			}
			if (TextUtils.isEmpty(params.getTargetUrl())) {
				LogUtil.e("the targetUrl can't be empty");
				if (listener != null) {
					listener.onError("the targetUrl can't be empty", null);
				}
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	protected boolean isShareSuccess(JsonObject jsonObject) {
		Integer retCode = GsonUtil.getAttribute(Integer.class, jsonObject,
				"ret");
		return retCode != null && retCode == 0;
	}

	protected class DefaultShareUIListener implements IUiListener {
		private ShareResultListener listener;

		public DefaultShareUIListener(ShareResultListener listener) {
			this.listener = listener;
		}

		@Override
		public void onCancel() {
			LogUtil.d("onCancel");
			if (listener != null) {
				listener.onCancel();
			}
		}

		@Override
		public void onComplete(Object obj) {
			LogUtil.d("onComplete:%s", obj.toString());
			if (listener != null) {
				JsonObject jsonObject = GsonUtil.getJsonObject(obj.toString());
				if (isShareSuccess(jsonObject)) {
					listener.onSuccess();
				} else {
					listener.onError(getErrorMsg(jsonObject), null);
				}
			}
		}

		@Override
		public void onError(UiError uiError) {
			LogUtil.d("error message:%s,errorDetail:%s", uiError.errorMessage,
					uiError.errorDetail);
			if (listener != null) {
				listener.onError(uiError.errorMessage, null);
			}
		}

	}

	private class OauthResultListenerWrapper implements IUiListener {
		private OauthResultListener listener;

		public OauthResultListenerWrapper(OauthResultListener listener) {
			this.listener = listener;
		}

		@Override
		public void onCancel() {
			if (listener != null) {
				listener.onCancel();
			}
		}

		@Override
		public void onComplete(Object object) {
			LogUtil.d("user login success,response:%s", object.toString());
			int resultCode;
			try {
				JSONObject jsonObject = JSONObject.class.cast(object);
				if (jsonObject != null) {
					resultCode = jsonObject.getInt("ret");
					LogUtil.d("the ret code:%s", resultCode);
					if (resultCode == 0) {
						long expiresin = Long.parseLong(jsonObject
								.getString("expires_in"))
								* 1000
								+ System.currentTimeMillis();
						String openid = jsonObject.getString("openid");
						String access_token = jsonObject
								.getString("access_token");
						AccessToken accessToken = new AccessToken(access_token,
								openid, expiresin);
						saveAccessToken(accessToken);
						listener.onSuccess(accessToken);
					} else {
						listener.onError(jsonObject.getString("msg"), null);
					}
				} else {
					listener.onError("data error", null);
				}
			} catch (JSONException e) {
				LogUtil.e(e, "");
				listener.onError("data error", null);
			}
		}

		@Override
		public void onError(UiError error) {
			if (listener != null) {
				listener.onError(error.errorMessage, null);
			}
		}

	}

}
