package org.huangsu.sharesdk.sina;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.huangsu.sharesdk.R;
import org.huangsu.sharesdk.bean.AccessToken;
import org.huangsu.sharesdk.bean.BasicUserInfo;
import org.huangsu.sharesdk.bean.ShareParams;
import org.huangsu.sharesdk.core.DataManager;
import org.huangsu.sharesdk.core.NetworkClient;
import org.huangsu.sharesdk.core.Platform;
import org.huangsu.sharesdk.core.ProxyActivity;
import org.huangsu.sharesdk.listener.ResponseListener;
import org.huangsu.sharesdk.util.JsonUtil;
import org.huangsu.sharesdk.util.LogUtil;
import org.huangsu.sharesdk.util.StringUtil;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.sina.sso.RemoteSSO;

public class SinaPlatform extends Platform {
	protected SinaPlatform(Context context, NetworkClient client,
			DataManager dataManager) {
		super(context, client, dataManager);
	}

	private ServiceConnection conn = null;
	private static final String WEIBO_SIGNATURE = "30820295308201fea00302010202044b4ef1bf300d"
			+ "06092a864886f70d010105050030818d310b300906035504061302434e3110300e0603550408130"
			+ "74265694a696e673110300e060355040713074265694a696e67312c302a060355040a132353696e"
			+ "612e436f6d20546563686e6f6c6f677920284368696e612920436f2e204c7464312c302a0603550"
			+ "40b132353696e612e436f6d20546563686e6f6c6f677920284368696e612920436f2e204c746430"
			+ "20170d3130303131343130323831355a180f32303630303130323130323831355a30818d310b300"
			+ "906035504061302434e3110300e060355040813074265694a696e673110300e0603550407130742"
			+ "65694a696e67312c302a060355040a132353696e612e436f6d20546563686e6f6c6f67792028436"
			+ "8696e612920436f2e204c7464312c302a060355040b132353696e612e436f6d20546563686e6f6c"
			+ "6f677920284368696e612920436f2e204c746430819f300d06092a864886f70d010101050003818"
			+ "d00308189028181009d367115bc206c86c237bb56c8e9033111889b5691f051b28d1aa8e42b66b7"
			+ "413657635b44786ea7e85d451a12a82a331fced99c48717922170b7fc9bc1040753c0d38b4cf2b2"
			+ "2094b1df7c55705b0989441e75913a1a8bd2bc591aa729a1013c277c01c98cbec7da5ad7778b2fa"
			+ "d62b85ac29ca28ced588638c98d6b7df5a130203010001300d06092a864886f70d0101050500038"
			+ "181000ad4b4c4dec800bd8fd2991adfd70676fce8ba9692ae50475f60ec468d1b758a665e961a3a"
			+ "edbece9fd4d7ce9295cd83f5f19dc441a065689d9820faedbb7c4a4c4635f5ba1293f6da4b72ed3"
			+ "2fb8795f736a20c95cda776402099054fccefb4a1a558664ab8d637288feceba9508aa907fc1fe2"
			+ "b1ae5a0dec954ed831c0bea4";
	private static String ssoPackageName = "";// "com.sina.weibo";
	private static String ssoActivityName = "";// "com.sina.weibo.MainTabActivity";

	@Override
	protected String getPlatformid() {
		return SINAWEIBO;
	}

	@Override
	protected int getNameId() {
		return R.string.sinaweibo;
	}

	@Override
	public boolean shouldOauthBeforeShare() {
		return true;
	}

	@Override
	protected void oauth(ProxyActivity activity, Long transaction) {
		final Long time = transaction;
		final ProxyActivity proxyActivity = activity;
		conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
				SinaPlatform.super.oauth(proxyActivity, time);
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				RemoteSSO remoteSSOservice = RemoteSSO.Stub
						.asInterface(service);
				try {
					ssoPackageName = remoteSSOservice.getPackageName();
					ssoActivityName = remoteSSOservice.getActivityName();
					boolean singleSignOnStarted = startSingleSignOn(
							proxyActivity, info.appkey, info.scope,
							DEFAULT_SSO_CODE);
					if (!singleSignOnStarted) {
						SinaPlatform.super.oauth(proxyActivity, time);
					}
				} catch (RemoteException e) {
					LogUtil.e(e, "");
					SinaPlatform.super.oauth(proxyActivity, time);
				}
			}
		};
		if (!bindRemoteSSOService(activity)) {
			super.oauth(activity, time);
		}
	}

	private boolean bindRemoteSSOService(Activity activity) {
		Context context = activity.getApplicationContext();
		Intent intent = new Intent("com.sina.weibo.remotessoservice");
		return context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}

	private boolean startSingleSignOn(Activity activity, String appkey,
			String scope, int activityCode) {
		boolean didSucceed = true;
		Intent intent = new Intent();
		intent.setClassName(ssoPackageName, ssoActivityName);
		intent.putExtra("appKey", appkey);
		intent.putExtra("redirectUri", info.redirecturl);
		if (!TextUtils.isEmpty(scope)) {
			intent.putExtra("scope", scope);
		}

		// validate Signature
		if (!validateAppSignatureForIntent(activity, intent)) {
			return false;
		}

		try {
			activity.startActivityForResult(intent, activityCode);
		} catch (ActivityNotFoundException e) {
			didSucceed = false;
		}

		activity.getApplication().unbindService(conn);
		return didSucceed;
	}

	private boolean validateAppSignatureForIntent(Activity activity,
			Intent intent) {
		ResolveInfo resolveInfo = activity.getPackageManager().resolveActivity(
				intent, 0);
		if (resolveInfo == null) {
			return false;
		}

		String packageName = resolveInfo.activityInfo.packageName;
		try {
			PackageInfo packageInfo = activity.getPackageManager()
					.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
			for (Signature signature : packageInfo.signatures) {
				if (WEIBO_SIGNATURE.equals(signature.toCharsString())) {
					return true;
				}
			}
		} catch (NameNotFoundException e) {
			return false;
		}
		return false;
	}

	@Override
	protected void handleOauthResult(int requestCode, int resultCode,
			Intent data, ProxyActivity activity) {
		if (requestCode == DEFAULT_SSO_CODE) {
			// Successfully redirected.
			if (resultCode == Activity.RESULT_OK) {
				// Check OAuth 2.0/2.10 error code.
				String error = data.getStringExtra("error");
				if (error == null) {
					error = data.getStringExtra("error_type");
				}

				// error occurred.
				if (error != null) {
					if (error.equals("access_denied")
							|| error.equals("OAuthAccessDeniedException")) {
						LogUtil.d("authorize canceled by user.");
						activity.onCancel();

					} else {
						String description = data
								.getStringExtra("error_description");
						if (description != null) {
							error = error + ":" + description;
						}
						LogUtil.d("authorize failed: %s", error);
						activity.onError(error, null);
					}

				} else {
					LogUtil.d("data:%s", data.getExtras().toString());
					String accessToken = data.getStringExtra("access_token");
					if (!TextUtils.isEmpty(accessToken)) {
						long expiresin = Long.parseLong(data
								.getStringExtra("expires_in"))
								* 1000
								+ System.currentTimeMillis();
						String uid = data.getStringExtra("uid");
						AccessToken accessToken2 = new AccessToken(accessToken,
								uid, expiresin);
						saveAccessToken(accessToken2);
						activity.onSuccess(accessToken2);

					} else {
						activity.onError("the returned token is null", null);
					}
				}
				// An error occurred before we could be redirected.
			} else {
				// An Android error occured.
				LogUtil.d("Login failed:%s", data.getStringExtra("error"));
				activity.onError(data.getStringExtra("error"), null);
			}
		} else if (requestCode == DEFAULT_OAUTH_CODE) {
			super.handleOauthResult(requestCode, resultCode, data, activity);
		}
	}

	@Override
	protected Map<String, String> getAccessTokenReqParams(String code) {
		initInfo();
		if (!TextUtils.isEmpty(code) && checkCofigIntegrity()) {
			Map<String, String> req = new HashMap<String, String>();
			req.put("code", code);
			req.put("redirect_uri", info.redirecturl);
			req.put("grant_type", "authorization_code");
			req.put("client_secret", info.appsecret);
			req.put("client_id", info.appkey);
			return req;
		}
		return null;
	}

	@Override
	protected AccessToken parseToken(JsonObject jsonObject) {
		String accessToken = JsonUtil.getAttribute(String.class, jsonObject,
				"access_token");
		if (accessToken != null) {
			Long expires_in = JsonUtil.getAttribute(Long.class, jsonObject,
					"expires_in") * 1000 + System.currentTimeMillis();
			String uid = JsonUtil.getAttribute(String.class, jsonObject, "uid");
			return new AccessToken(accessToken, uid, expires_in);
		}
		return null;
	}

	@Override
	public Map<String, String> getCodeReqParams() {
		initInfo();
		if (checkCofigIntegrity()) {
			Map<String, String> authParams = new HashMap<String, String>();
			authParams.put("client_id", info.appkey);
			authParams.put("redirect_uri", info.redirecturl);
			if (!TextUtils.isEmpty(info.scope)) {
				authParams.put("scope", info.scope);
			}
			authParams.put("display", "mobile");
			return authParams;
		}
		return null;
	}

	@Override
	protected boolean checkCofigIntegrity() {
		return super.checkCofigIntegrity() && !TextUtils.isEmpty(info.appkey)
				&& !TextUtils.isEmpty(info.appsecret);
	}

	@Override
	protected void doShare(ProxyActivity activity, ShareParams params) {
		String url = SinaConstants.BASEURL;
		String accessToken = getAccessToken().token;
		if (params.getBitmap() == null
				|| TextUtils.isEmpty(params.getImageName())) {
			url += SinaConstants.PUBLISHWEBO;
			Map<String, String> normalParams = new HashMap<String, String>();
			normalParams.put("access_token", accessToken);
			normalParams.put("status", StringUtil.subStringOfOrigin(
					params.getContent(), 140, dots));
			client.post(url, null, normalParams, getWrapperedListener(activity));
		} else {
			url += SinaConstants.PUBLISHWEBOIMG;
			Map<String, ContentBody> reqParams = new HashMap<String, ContentBody>();
			try {
				reqParams.put("access_token", new StringBody(accessToken,
						charset));
				reqParams.put(
						"status",
						new StringBody(StringUtil.subStringOfOrigin(
								params.getContent(), 140, dots), charset));
				reqParams.put(
						"pic",
						new ByteArrayBody(params.getBitmap(), StringUtil
								.getMimeType(params.getImageName()), params
								.getImageName()));
				client.muiltiPost(url, null, reqParams,
						getWrapperedListener(activity));
			} catch (UnsupportedEncodingException e) {
				LogUtil.e(e, "");
			}
		}
	}

	@Override
	protected boolean isShareSuccess(JsonObject jsonObject) {
		return isJsonResultRight(jsonObject);
	}

	private boolean isJsonResultRight(JsonObject jsonObject) {
		String error = JsonUtil.getAttribute(String.class, jsonObject, "error");
		return TextUtils.isEmpty(error);
	}

	@Override
	protected String getErrorMsg(JsonObject jsonObject) {
		return JsonUtil.getAttribute(String.class, jsonObject, "error");
	}

	@Override
	protected void doGetUserInfo(AccessToken accessToken, String uid,
			ResponseListener listener) {
		String url = SinaConstants.BASEURL + SinaConstants.BASEUSERINFO;
		Map<String, String> params = new HashMap<String, String>();
		params.put("access_token", accessToken.token);
		params.put("uid", uid);
		client.get(url, null, params, listener);
	}

	@Override
	protected BasicUserInfo parseBasicUserInfo(JsonObject jsonObject) {
		String uid = JsonUtil.getAttribute(String.class, jsonObject, "id");
		if (!TextUtils.isEmpty(uid)) {
			int gender = 0;
			String genderTemp = JsonUtil.getAttribute(String.class, jsonObject,
					"gender");
			if ("m".equals(genderTemp)) {
				gender = 0;
			} else if ("f".equals(genderTemp)) {
				gender = 1;
			} else {
				gender = 2;
			}
			/**
			 * 大头像地址
			 */
			String avatar = JsonUtil.getAttribute(String.class, jsonObject,
					"avatar_large");
			if (TextUtils.isEmpty(avatar)) {
				avatar = JsonUtil.getAttribute(String.class, jsonObject,
						"profile_image_url");
			}
			String name = JsonUtil.getAttribute(String.class, jsonObject,
					"screen_name");
			return new BasicUserInfo(uid, gender, avatar, name);
		}
		return null;
	}
}
