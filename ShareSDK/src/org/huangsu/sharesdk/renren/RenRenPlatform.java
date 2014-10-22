package org.huangsu.sharesdk.renren;

import java.util.HashMap;
import java.util.Map;

import org.huangsu.sharesdk.R;
import org.huangsu.sharesdk.bean.AccessToken;
import org.huangsu.sharesdk.bean.BasicUserInfo;
import org.huangsu.sharesdk.bean.ShareParams;
import org.huangsu.sharesdk.core.DataManager;
import org.huangsu.sharesdk.core.NetworkClient;
import org.huangsu.sharesdk.core.Platform;
import org.huangsu.sharesdk.core.ProxyActivity;
import org.huangsu.sharesdk.listener.ResponseListener;
import org.huangsu.sharesdk.listener.ShareResultListener;
import org.huangsu.sharesdk.util.JsonUtil;
import org.huangsu.sharesdk.util.LogUtil;
import org.huangsu.sharesdk.util.StringUtil;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class RenRenPlatform extends Platform {
	protected RenRenPlatform(Context context, NetworkClient client,DataManager dataManager) {
		super(context, client,dataManager);
	}

	private Map<String, String> headers;

	@Override
	protected String getPlatformid() {
		return RENREN;
	}

	@Override
	protected int getNameId() {
		return R.string.renren;
	}

	@Override
	public boolean shouldOauthBeforeShare() {
		return true;
	}

	@Override
	public Map<String, String> getCodeReqParams() {
		initInfo();
		if (checkCofigIntegrity()) {
			Map<String, String> authParams = new HashMap<String, String>();
			authParams.put("client_id", info.appkey);
			authParams.put("redirect_uri", info.redirecturl);
			authParams.put("response_type", "code");
			if (!TextUtils.isEmpty(info.scope)) {
				authParams.put("scope", info.scope);
			}
			authParams.put("display", "touch");
			return authParams;
		}
		return null;
	}

	@Override
	protected Map<String, String> getAccessTokenReqParams(String code) {
		initInfo();
		if (checkCofigIntegrity()) {
			Map<String, String> req = new HashMap<String, String>();
			req.put("grant_type", "authorization_code");
			req.put("client_id", info.appkey);
			req.put("client_secret", info.appsecret);
			req.put("redirect_uri", info.redirecturl);
			req.put("code", code);
			return req;
		}
		return null;
	}

	@Override
	protected boolean checkCofigIntegrity() {
		return super.checkCofigIntegrity() && !TextUtils.isEmpty(info.appkey)
				&& !TextUtils.isEmpty(info.appsecret);
	}

	@Override
	protected AccessToken parseToken(JsonObject jsonObject) {
		String accessToken = JsonUtil.getAttribute(String.class, jsonObject,
				"access_token");
		if (accessToken != null) {
			Long expires_in = JsonUtil.getAttribute(Long.class, jsonObject,
					"expires_in") * 1000 + System.currentTimeMillis();
			JsonObject userObject = JsonUtil.getAttribute(JsonObject.class,
					jsonObject, "user");
			String userid = JsonUtil.getAttribute(String.class, userObject,
					"id");
			String refreshToken = JsonUtil.getAttribute(String.class,
					jsonObject, "refresh_token");
			saveBasicUserInfo(parseBasicUserInfo(userObject));
			return new AccessToken(accessToken, userid, expires_in,
					refreshToken);
		}
		return null;
	}

	@Override
	protected void reoauth(ProxyActivity activity, Long transaction) {
		AccessToken accessToken = getAccessToken();
		if (accessToken != null && !TextUtils.isEmpty(accessToken.refreshToken)) {
			initInfo();
			final ProxyActivity proxyActivity = activity;
			String url = RenRenConstants.REFRESHTOKEN;
			Map<String, String> map = new HashMap<String, String>();
			map.put("grant_type", "refresh_token");
			map.put("refresh_token", accessToken.refreshToken);
			map.put("client_id", info.appkey);
			map.put("client_secret", info.appsecret);
			client.get(url, null, map, new ResponseListener() {

				@Override
				public void onSuccess(JsonObject result) {
					AccessToken accessToken = parseToken(result);
					if (accessToken != null) {
						proxyActivity.onSuccess(accessToken);
					} else {
						proxyActivity.onError("parse data error", null);
					}
					saveAccessToken(accessToken);
				}

				@Override
				public void onError(String msg, Throwable error) {
					proxyActivity.onError(msg, error);
				}
			});
		} else {
			oauth(activity, transaction);
		}
	}

	@Override
	protected void doShare(ProxyActivity activity, ShareParams params) {
		String accessToken = getAccessToken().token;
		getHeaders(accessToken);
		final String url = RenRenConstants.FEEDPUT;
		// if (params.getBitmap() != null
		// && !StringUtil.isEmpty(params.getImageName())) {
		//
		// } else {
		final Map<String, String> normalParams = new HashMap<String, String>();
		normalParams.put("access_token", accessToken);
		// 新鲜事主体内容 注意：最多200个字符
		normalParams.put("description",
				StringUtil.subStringOfOrigin(params.getContent(), 200, dots));
		// 新鲜事标题 注意：最多30个字符
		normalParams.put("title",
				StringUtil.subStringOfOrigin(params.getTitle(), 30, dots));
		// 新鲜事标题和图片指向的链接
		normalParams.put("targetUrl", params.getTargetUrl());
		// 用户输入的自定义内容。注意：最多200个字符
		normalParams.put("message",
				StringUtil.subStringOfOrigin(params.getComment(), 200, dots));
		if (params.getImageUrls() != null) {
			normalParams.put("imageUrl", params.getImageUrls()[0]);
		}
		client.post(url, headers, normalParams, getWrapperedListener(activity));
		// }
	}

	private void getHeaders(String accessToken) {
		if (headers == null) {
			headers = new HashMap<String, String>();
			headers.put("Accept",
					"application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			headers.put("Accept-Language", "zh-CN,zh;q=0.8");
			headers.put("Connection", "keep-alive");
		}
		headers.put("Authorization", "Bearer " + accessToken);
	}

	@Override
	protected boolean validateShareParams(ShareParams params,
			ShareResultListener listener) {
		if (super.validateShareParams(params, listener)) {
			if (TextUtils.isEmpty(params.getTitle())) {
				LogUtil.d("the title can't be empty");
				if (listener != null) {
					listener.onError("the title can't be empty", null);
				}
				return false;
			}
			if (TextUtils.isEmpty(params.getComment())) {
				LogUtil.d("the comment can't be empty");
				if (listener != null) {
					listener.onError("the comment can't be empty", null);
				}
				return false;
			}
			if (TextUtils.isEmpty(params.getTargetUrl())) {
				LogUtil.d("the targetUrl can't be empty");
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
		return JsonUtil.getAttribute(JsonObject.class, jsonObject, "error") == null;
	}

	@Override
	protected String getErrorMsg(JsonObject jsonObject) {
		return JsonUtil.getAttribute(String.class,
				JsonUtil.getAttribute(JsonObject.class, jsonObject, "error"),
				"message");
	}

	@Override
	protected void doGetUserInfo(AccessToken accessToken, String uid,
			ResponseListener listener) {
		String url = RenRenConstants.BASEUSERINFO;
		Map<String, String> params = new HashMap<String, String>();
		params.put("userId", uid);
		getHeaders(accessToken.token);
		client.get(url, headers, params, listener);
	}

	@Override
	protected BasicUserInfo parseBasicUserInfo(JsonObject jsonObject) {
		String uid = JsonUtil.getAttribute(String.class, jsonObject, "id");
		if (!TextUtils.isEmpty(uid)) {
			String name = JsonUtil.getAttribute(String.class, jsonObject,
					"name");
			JsonArray avatars = JsonUtil.getAttribute(JsonArray.class,
					jsonObject, "avatar");
			String avatar = null;
			if (avatars != null && avatars.size() > 0) {
				avatar = JsonUtil.getAttribute(String.class, avatars.get(0)
						.getAsJsonObject(), "url");
			}
			String sex = JsonUtil.getAttribute(String.class, JsonUtil
					.getAttribute(JsonObject.class, jsonObject,
							"basicInformation"), "sex");
			int gender = 0;
			if ("MALE".equals(sex)) {
				gender = 0;
			} else if ("FEMALE".equals(sex)) {
				gender = 1;
			} else {
				gender = 2;
			}
			return new BasicUserInfo(uid, gender, avatar, name);
		}
		return null;
	}

}
