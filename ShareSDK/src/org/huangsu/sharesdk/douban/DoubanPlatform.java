package org.huangsu.sharesdk.douban;

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

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.JsonObject;

public class DoubanPlatform extends Platform {
	protected DoubanPlatform(Context context, NetworkClient client,DataManager dataManager) {
		super(context, client,dataManager);
	}

	private Map<String, String> headers;

	@Override
	protected String getPlatformid() {
		return DOUBAN;
	}

	@Override
	protected int getNameId() {
		return R.string.douban;
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
		String access_token = JsonUtil.getAttribute(String.class, jsonObject,
				"access_token");
		if (access_token != null) {
			Long expires_in = JsonUtil.getAttribute(Long.class, jsonObject,
					"expires_in") * 1000 + System.currentTimeMillis();
			String douban_user_id = JsonUtil.getAttribute(String.class,
					jsonObject, "douban_user_id");
			String refreshToken = JsonUtil.getAttribute(String.class,
					jsonObject, "refresh_token");
			return new AccessToken(access_token, douban_user_id, expires_in,
					refreshToken);
		}
		return null;
	}

	@Override
	protected void reoauth(ProxyActivity activity, Long transaction) {
		AccessToken accessToken = getAccessToken();
		if (accessToken != null && !TextUtils.isEmpty(accessToken.refreshToken)) {
			if (accessToken.expiresIn < System.currentTimeMillis()) {
				initInfo();
				final ProxyActivity proxyActivity = activity;
				String url = DoubanConstants.REFERESHTOKEN;
				Map<String, String> map = new HashMap<String, String>();
				map.put("client_id", info.appkey);
				map.put("client_secret", info.appsecret);
				map.put("redirect_uri", info.redirecturl);
				map.put("grant_type", "refresh_token");
				map.put("refresh_token", accessToken.refreshToken);
				client.get(url, null, map, new ResponseListener() {

					@Override
					public void onSuccess(JsonObject result) {
						AccessToken accessToken = parseToken(result);
						if (accessToken == null) {
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
				return;
			}
		}
		oauth(activity, transaction);
	}

	@Override
	protected void doShare(ProxyActivity activity, ShareParams params) {
		String url = DoubanConstants.SENDRADIO;
		String accessToken = getAccessToken().token;
		getHeaders(accessToken);
		if (params.getBitmap() != null
				&& !StringUtil.isEmpty(params.getImageName())) {
			try {
				Map<String, ContentBody> reqParams = new HashMap<String, ContentBody>();
				reqParams.put("source", new StringBody(info.appkey, charset));
				reqParams.put("text", new StringBody(params.getContent(),
						charset));
				reqParams.put(
						"image",
						new ByteArrayBody(params.getBitmap(), StringUtil
								.getMimeType(params.getImageName()), params
								.getImageName()));
				if (!StringUtil.isEmpty(params.getTargetUrl())) {
					reqParams.put("rec_url",
							new StringBody(params.getTargetUrl(), charset));

				}
				if (params.getImageUrls() != null) {
					reqParams.put("rec_image",
							new StringBody(params.getImageUrls()[0], charset));
				}
				if (!StringUtil.isEmpty(params.getComment())) {
					reqParams.put("rec_desc",
							new StringBody(params.getComment(), charset));
				}
				client.muiltiPost(url, headers, reqParams,
						getWrapperedListener(activity));
			} catch (UnsupportedEncodingException e) {
				LogUtil.e(e, "");
			}
		} else {
			Map<String, String> normalParams = new HashMap<String, String>();
			normalParams.put("source", info.appkey);
			normalParams.put("text", params.getContent());
			if (params.getImageUrls() != null) {
				normalParams.put("rec_image", params.getImageUrls()[0]);
			}
			if (!StringUtil.isEmpty(params.getTargetUrl())) {
				normalParams.put("rec_url", params.getTargetUrl());
			}
			if (!StringUtil.isEmpty(params.getComment())) {
				normalParams.put("rec_desc", params.getComment());
			}
			client.post(url, headers, normalParams,
					getWrapperedListener(activity));
		}

	}

	public Map<String,String> getHeaders(String accessToken) {
		if (headers == null) {
			headers = new HashMap<String, String>();
			headers.put("Accept",
					"application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			headers.put("Accept-Language", "zh-CN,zh;q=0.8");
			headers.put("Connection", "keep-alive");
		}
		headers.put("Authorization", "Bearer " + accessToken);
		return headers;
	}

	@Override
	protected boolean isShareSuccess(JsonObject jsonObject) {
		return JsonUtil.getAttribute(int.class, jsonObject, "code") == null;
	}

	@Override
	protected String getErrorMsg(JsonObject jsonObject) {
		return JsonUtil.getAttribute(String.class, jsonObject, "msg");
	}

	@Override
	protected void doGetUserInfo(AccessToken accessToken, String uid,
			ResponseListener listener) {
		String url = DoubanConstants.BASEUSERINFO + ":" + uid;
		String token = accessToken.token;
		getHeaders(token);
		client.get(url, headers,null, listener);
	}

	@Override
	protected BasicUserInfo parseBasicUserInfo(JsonObject jsonObject) {
		String uid = JsonUtil.getAttribute(String.class, jsonObject, "id");
		if (!TextUtils.isEmpty(uid)) {
			String name = JsonUtil.getAttribute(String.class, jsonObject,
					"name");
			String avatar = JsonUtil.getAttribute(String.class, jsonObject,
					"avatar");
			return new BasicUserInfo(uid, 2, avatar, name);
		}
		return null;
	}

}
