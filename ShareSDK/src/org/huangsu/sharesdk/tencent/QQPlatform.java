package org.huangsu.sharesdk.tencent;

import java.util.HashMap;
import java.util.Map;

import org.huangsu.sharesdk.R;
import org.huangsu.sharesdk.bean.AccessToken;
import org.huangsu.sharesdk.bean.BasicUserInfo;
import org.huangsu.sharesdk.bean.ShareParams;
import org.huangsu.sharesdk.core.DataManager;
import org.huangsu.sharesdk.core.NetworkClient;
import org.huangsu.sharesdk.core.ProxyActivity;
import org.huangsu.sharesdk.listener.ResponseListener;
import org.huangsu.sharesdk.util.GsonUtil;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

public class QQPlatform extends TencentBase {

	protected QQPlatform(Context context, NetworkClient client,DataManager dataManager) {
		super(context, client,dataManager);
	}

	@Override
	protected String getPlatformid() {
		return QQ;
	}

	@Override
	protected int getNameId() {
		return R.string.qq;
	}

	@Override
	protected void initInfo() {
		initInfo(QQ);
	}

	@Override
	protected Map<String, String> getCodeReqParams() {
		initInfo();
		if (checkCofigIntegrity()) {
			Map<String, String> authParams = new HashMap<String, String>();
			authParams.put("client_id", info.appid);
			authParams.put("response_type", "code");
			authParams.put("display", "mobile");
			authParams.put("redirect_uri", info.redirecturl);
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
		if (!TextUtils.isEmpty(code) && checkCofigIntegrity()) {
			Map<String, String> req = new HashMap<String, String>();
			req.put("grant_type", "authorization_code");
			req.put("client_id", info.appid);
			req.put("client_secret", info.appkey);
			req.put("code", code);
			req.put("redirect_uri", info.redirecturl);
			return req;
		}
		return null;
	}

	@Override
	protected void doShare(ProxyActivity activity, ShareParams params,
			IUiListener listener) {
		if (isValid()) {
			Bundle data = new Bundle();
			int shareType = QQShare.SHARE_TO_QQ_TYPE_DEFAULT;
			if (params.getImageUrls() != null
					&& params.getImageUrls().length > 0) {
				data.putString(QzoneShare.SHARE_TO_QQ_IMAGE_URL,
						params.getImageUrls()[0]);
			}
			data.putString(QQShare.SHARE_TO_QQ_TITLE, params.getTitle());
			data.putString(QQShare.SHARE_TO_QQ_TARGET_URL,
					params.getTargetUrl());
			data.putString(QQShare.SHARE_TO_QQ_SUMMARY, params.getContent());
			data.putString(QQShare.SHARE_TO_QQ_APP_NAME, getAPPName());
			data.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, shareType);
			tencent.shareToQQ(activity, data, listener);
		} else {
			listener.onError(new UiError(-1,
					"your qq version is too low,please update your qq version",
					null));
		}
	}

	protected boolean isValid() {
		PackageInfo pi = null;
		try {
			pi = context.getPackageManager().getPackageInfo(
					Constants.PACKAGE_QQ, 0);
		} catch (Throwable t) {
			pi = null;
			return false;
		}
		if (pi == null) {
			return false;
		}
		String[] ver = pi.versionName.split("\\.");
		int[] verCode = new int[ver.length];
		for (int i = 0; i < verCode.length; i++) {
			try {
				verCode[i] = Integer.parseInt(ver[i]);
			} catch (Throwable t) {
				verCode[i] = 0;
			}
		}
		return ((verCode.length > 0 && verCode[0] >= 5) || (verCode.length > 1
				&& verCode[0] >= 4 && verCode[1] >= 6));
	}

	@Override
	protected String getErrorMsg(JsonObject jsonObject) {
		return GsonUtil.getAttribute(String.class, jsonObject, "msg");
	}

	@Override
	public AccessToken getAccessToken() {
		return getAccessToken(QQ);
	}

	@Override
	protected void doGetUserInfo(AccessToken accessToken, String uid,
			ResponseListener listener) {
		String url = QQConstants.BASEUSERINFO;
		client.get(url, null,getCommonParams(accessToken), listener);
	}

	protected Map<String, String> getCommonParams(AccessToken accessToken) {
		Map<String, String> req = new HashMap<String, String>();
		req.put("access_token", accessToken.token);
		req.put("oauth_consumer_key", info.appid);
		req.put("openid", accessToken.uid);
		return req;
	}

	@Override
	protected BasicUserInfo parseBasicUserInfo(JsonObject jsonObject) {
		Integer ret = GsonUtil.getAttribute(Integer.class, jsonObject, "ret");
		if (ret != null && ret == 0) {
			String name = GsonUtil.getAttribute(String.class, jsonObject,
					"nickname");
			String avatar = GsonUtil.getAttribute(String.class, jsonObject,
					"figureurl_qq_2");
			if (TextUtils.isEmpty(avatar)) {
				avatar = GsonUtil.getAttribute(String.class, jsonObject,
						"figureurl_qq_1");
			}
			int gender = 0;
			if ("女".equals(GsonUtil.getAttribute(String.class, jsonObject,
					"gender"))) {
				gender = 1;
			}
			return new BasicUserInfo(getAccessToken().uid, gender, avatar,
					name);
		}
		return null;
	}
}
