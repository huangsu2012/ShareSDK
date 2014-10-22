package org.huangsu.sharesdk.tencent;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.huangsu.sharesdk.R;
import org.huangsu.sharesdk.bean.AccessToken;
import org.huangsu.sharesdk.bean.ShareParams;
import org.huangsu.sharesdk.core.DataManager;
import org.huangsu.sharesdk.core.NetworkClient;
import org.huangsu.sharesdk.core.ProxyActivity;
import org.huangsu.sharesdk.util.LogUtil;
import org.huangsu.sharesdk.util.StringUtil;

import android.content.Context;

public class TencentWeiboPlatform extends QQPlatform {

	protected TencentWeiboPlatform(Context context, NetworkClient client,DataManager dataManager) {
		super(context, client,dataManager);
	}

	@Override
	protected String getPlatformid() {
		return TENCENTWEIBO;
	}

	@Override
	protected int getNameId() {
		return R.string.tencentweibo;
	}

	@Override
	public boolean shouldOauthBeforeShare() {
		return true;
	}

	@Override
	protected void doShare(ProxyActivity activity, ShareParams params) {
		String url = QQConstants.BASEURL;
		AccessToken accessToken = getAccessToken();
		String token = accessToken.token;
		if (params.getBitmap() == null
				|| StringUtil.isEmpty(params.getImageName())) {
			url += QQConstants.PUBLISHWEBO;
			Map<String, String> normalParams = new HashMap<String, String>();
			normalParams.put("access_token", token);
			normalParams.put("format", "json");
			normalParams.put("content", StringUtil.subStringOfOrigin(
					params.getContent(), 140, dots));
			normalParams.put("oauth_consumer_key", info.appid);
			normalParams.put("openid", accessToken.uid);
			client.post(url, null, normalParams, getWrapperedListener(activity));

		} else {
			url += QQConstants.PUBLISHWEBOIMG;
			Map<String, ContentBody> reqParams = new HashMap<String, ContentBody>();
			try {
				reqParams.put("access_token", new StringBody(token, charset));
				reqParams.put("format", new StringBody("json", charset));
				reqParams.put(
						"content",
						new StringBody(StringUtil.subStringOfOrigin(
								params.getContent(), 140, dots), charset));
				reqParams.put("oauth_consumer_key", new StringBody(info.appid,
						charset));
				reqParams.put("openid",
						new StringBody(accessToken.uid, charset));
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

}
