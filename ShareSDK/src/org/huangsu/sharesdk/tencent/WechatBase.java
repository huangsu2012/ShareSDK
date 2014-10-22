package org.huangsu.sharesdk.tencent;

import java.util.HashMap;
import java.util.Map;

import org.huangsu.sharesdk.bean.AccessToken;
import org.huangsu.sharesdk.bean.BasicUserInfo;
import org.huangsu.sharesdk.bean.ShareParams;
import org.huangsu.sharesdk.core.DataManager;
import org.huangsu.sharesdk.core.NetworkClient;
import org.huangsu.sharesdk.core.Platform;
import org.huangsu.sharesdk.core.ProxyActivity;
import org.huangsu.sharesdk.listener.OauthResultListener;
import org.huangsu.sharesdk.listener.ResponseListener;
import org.huangsu.sharesdk.listener.ShareResultListener;
import org.huangsu.sharesdk.util.JsonUtil;
import org.huangsu.sharesdk.util.LogUtil;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public abstract class WechatBase extends Platform {
	protected WechatBase(Context context, NetworkClient client,
			DataManager dataManager) {
		super(context, client, dataManager);
	}

	protected static IWXAPI iwxapi;
	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
	private static boolean registerSuccess;
	private final static Map<String, ShareResultListener> shareResultListeners = new HashMap<String, ShareResultListener>(
			2);
	private final static Map<String, OauthResultListener> oauthResultListeners = new HashMap<String, OauthResultListener>(
			2);

	public void handleIntent(Intent intent, IWXAPIEventHandler handler) {
		initWXAPIIfNeccessary();
		iwxapi.handleIntent(intent, handler);
	}

	public void handleResp(BaseResp baseResp) {
		if (baseResp != null) {
			if (baseResp instanceof SendAuth.Resp) {
				final OauthResultListener listener = oauthResultListeners
						.remove(baseResp.transaction);
				switch (baseResp.errCode) {
				case BaseResp.ErrCode.ERR_OK:
					String code = ((SendAuth.Resp) baseResp).code;
					String state = ((SendAuth.Resp) baseResp).state;
					if (LogUtil.TAG.equals(state)) {
						String url = info.accesstokenurl;
						client.get(url, null, getAccessTokenReqParams(code),
								new ParseTokenListener(listener));
					}
					break;
				case BaseResp.ErrCode.ERR_USER_CANCEL:
					if (listener != null) {
						listener.onCancel();
					}
					break;
				default:
					if (listener != null) {
						listener.onError(baseResp.errStr, null);
					}
					break;
				}
			} else {
				ShareResultListener listener = shareResultListeners
						.remove(baseResp.transaction);
				switch (baseResp.errCode) {
				case BaseResp.ErrCode.ERR_OK:
					if (listener != null) {
						listener.onSuccess();
					}
					break;
				case BaseResp.ErrCode.ERR_USER_CANCEL:
					if (listener != null) {
						listener.onCancel();
					}
					break;
				default:
					if (listener != null) {
						listener.onError(baseResp.errStr, null);
					}
					break;
				}
			}
		}
	}

	@Override
	protected void reoauth(ProxyActivity activity, Long transaction) {
		AccessToken accessToken = getAccessToken();
		if (accessToken != null && !TextUtils.isEmpty(accessToken.refreshToken)) {
			initInfo();
			String url = WechatConstants.REFRESHTOKEN;
			Map<String, String> map = new HashMap<String, String>();
			map.put("appid", info.appid);
			map.put("grant_type", "refresh_token");
			map.put("refresh_token", accessToken.refreshToken);
			client.get(url, null, map, new ParseTokenListener(activity));
		} else {
			oauth(activity, transaction);
		}
	}

	@Override
	public boolean shouldOauthBeforeShare() {
		return false;
	}

	@Override
	public Map<String, String> getCodeReqParams() {
		return null;
	}

	@Override
	protected Map<String, String> getAccessTokenReqParams(String code) {
		initInfo();
		if (!TextUtils.isEmpty(code) && checkCofigIntegrity()) {
			Map<String, String> req = new HashMap<String, String>();
			req.put("code", code);
			req.put("grant_type", "authorization_code");
			req.put("secret", info.appsecret);
			req.put("appid", info.appid);
			return req;
		}
		return null;
	}

	@Override
	protected boolean checkCofigIntegrity() {
		return !TextUtils.isEmpty(info.accesstokenurl)
				&& !TextUtils.isEmpty(info.appid)
				&& !TextUtils.isEmpty(info.appsecret);
	}

	@Override
	protected void oauth(ProxyActivity activity, Long transaction) {
		if (activity == null || transaction == null) {
			throw new IllegalArgumentException(
					"activity and transaction must not be null");
		}
		initWXAPIIfNeccessary();
		if (registerSuccess) {
			SendAuth.Req req = new SendAuth.Req();
			req.scope = info.scope;
			req.state = LogUtil.TAG;
			if (!iwxapi.sendReq(req)) {
				LogUtil.d("send req to wechat fail");
				activity.onError("send req to wechat fail", null);
			} else {
				oauthResultListeners.put(
						String.valueOf(System.currentTimeMillis()), activity);
			}
		} else {
			activity.onError("register to wechat app fail", null);
		}
	}

	@Override
	protected AccessToken parseToken(JsonObject jsonObject) {
		String token = JsonUtil.getAttribute(String.class, jsonObject,
				"access_token");
		if (!TextUtils.isEmpty(token)) {
			String uid = JsonUtil.getAttribute(String.class, jsonObject,
					"openid");
			long expiresIn = System.currentTimeMillis()
					+ JsonUtil.getAttribute(Long.class, jsonObject,
							"expires_in") * 1000;
			String refreshToken = JsonUtil.getAttribute(String.class,
					jsonObject, "refresh_token");
			return new AccessToken(token, uid, expiresIn, refreshToken);
		}
		return null;
	}

	@Override
	public AccessToken getAccessToken() {
		return getAccessToken(WECHAT);
	}

	@Override
	protected void saveAccessToken(AccessToken accessToken) {
		saveAccessToken(WECHAT, accessToken);
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
			return true;
		}
		return false;
	}

	@Override
	protected void doShare(ProxyActivity activity, ShareParams params) {
		initWXAPIIfNeccessary();
		if (!registerSuccess) {
			LogUtil.d("register to wechat app fail");
			activity.onError("register to wechat app fail", null);
			return;
		}

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		if (iwxapi.getWXAppSupportAPI() >= TIMELINE_SUPPORTED_VERSION
				&& WECHATMOMENTS.equals(info.platformid)) {
			req.scene = SendMessageToWX.Req.WXSceneTimeline;
		} else {
			req.scene = SendMessageToWX.Req.WXSceneSession;
		}
		WXMediaMessage mediaMessage = new WXMediaMessage();
		mediaMessage.title = params.getTitle();
		mediaMessage.description = params.getContent();
		if (TextUtils.isEmpty(params.getTargetUrl())) {
			if (params.getBitmap() == null && params.getImageUrls() == null) {
				WXTextObject textObject = new WXTextObject();
				textObject.text = params.getContent();
				mediaMessage.mediaObject = textObject;
			} else {
				WXImageObject imageObject = new WXImageObject();
				imageObject.imageUrl = params.getImageUrls() == null ? null
						: params.getImageUrls()[0];
				imageObject.imageData = params.getBitmap();
				mediaMessage.mediaObject = imageObject;
			}
		} else {
			WXWebpageObject webpageObject = new WXWebpageObject(
					params.getTargetUrl());
			mediaMessage.mediaObject = webpageObject;
			mediaMessage.thumbData = params.getBitmap();
		}
		req.message = mediaMessage;
		if (!iwxapi.sendReq(req)) {
			LogUtil.d("send req to wechat fail");
			activity.onError("send req to wechat fail", null);
		} else {
			shareResultListeners.put(req.transaction, activity);
		}
	}

	protected IWXAPI initWXAPIIfNeccessary() {
		initInfo();
		if (iwxapi == null) {
			iwxapi = WXAPIFactory.createWXAPI(context, info.appid);
			registerSuccess = iwxapi.registerApp(info.appid);
		}
		return iwxapi;
	}

	@Override
	protected boolean isShareSuccess(JsonObject jsonObject) {
		return false;
	}

	@Override
	protected String getErrorMsg(JsonObject jsonObject) {
		return JsonUtil.getAttribute(String.class, jsonObject, "errmsg");
	}

	@Override
	protected void doGetUserInfo(AccessToken accessToken, String uid,
			ResponseListener listener) {
		String url = WechatConstants.BASEUSERINFO;
		Map<String, String> map = new HashMap<String, String>();
		map.put("access_token", accessToken.token);
		map.put("openid", uid);
		client.get(url, null, map, listener);

	}

	@Override
	protected BasicUserInfo parseBasicUserInfo(JsonObject jsonObject) {
		String uid = JsonUtil.getAttribute(String.class, jsonObject, "openid");
		if (!TextUtils.isEmpty(uid)) {
			String name = JsonUtil.getAttribute(String.class, jsonObject,
					"nickname");
			String avatar = JsonUtil.getAttribute(String.class, jsonObject,
					"headimgurl");
			int gender = 0;
			String sex = JsonUtil.getAttribute(String.class, jsonObject, "sex");
			if ("1".equals(sex)) {
				gender = 0;
			} else if ("2".equals(sex)) {
				gender = 1;
			} else {
				gender = 2;
			}
			return new BasicUserInfo(uid, gender, avatar, name);
		}
		return null;
	}

	private class ParseTokenListener implements ResponseListener {
		private OauthResultListener listener;

		public ParseTokenListener(OauthResultListener listener) {
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
			AccessToken accessToken = parseToken(result);
			saveAccessToken(accessToken);
			if (listener != null) {
				if (accessToken != null) {
					listener.onSuccess(accessToken);
				} else {
					listener.onError(getErrorMsg(result), null);
				}
			}
		}

	}

}
