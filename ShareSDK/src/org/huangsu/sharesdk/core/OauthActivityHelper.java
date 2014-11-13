package org.huangsu.sharesdk.core;

import java.util.Map;

import org.huangsu.sharesdk.bean.AccessToken;
import org.huangsu.sharesdk.bean.PlatformInfo;
import org.huangsu.sharesdk.listener.OauthResultListener;
import org.huangsu.sharesdk.util.LogUtil;
import org.huangsu.sharesdk.util.URLUtil;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

/**
 * A helper class used to oauth
 * 
 * @author huangsu2012@gmail.com
 *
 */
public class OauthActivityHelper {
	private String platformid;
	private Platform platform;
	private PlatformInfo info;
	private final static int RESULT_ERROR = 2;
	private final Activity mActivity;
	private String encodedAuthorizeurl;
	

	public String getEncodedAuthorizeurl() {
		return encodedAuthorizeurl;
	}

	public OauthActivityHelper(Activity activity) {
		if (activity == null) {
			throw new NullPointerException("activity is null");
		}
		mActivity = activity;
	}

	/**
	 * 
	 * @return true indicate the encodedAuthorizeurl is not empty
	 */
	public boolean hanldeIntent() {
		Intent intent = mActivity.getIntent();
		platformid = intent.getStringExtra("platformid");
		if (TextUtils.isEmpty(platformid)) {
			mActivity.finish();
			return false;
		}
		Platform platform = PlatformFactory.getInstance(mActivity).getPlatform(
				platformid);
		if (platform == null) {
			return false;
		}
		info = platform.getInfo();
		Map<String, String> authParams = platform.getCodeReqParams();
		if (authParams != null) {
			encodedAuthorizeurl = URLUtil.constructUrl(info.authorizeurl,
					authParams);
			return !TextUtils.isEmpty(encodedAuthorizeurl);
		} else {
			feedBack(RESULT_ERROR, "the authParams is null", null);
			return false;
		}
	}

	/**
	 * 返回结果
	 * 
	 * @param accessToken
	 */
	public void feedBack(int resultCode, String msg, AccessToken accessToken) {
		LogUtil.d("resultCode:%s,msg:%s", resultCode, msg);
		Intent intent = mActivity.getIntent();
		intent.putExtra("msg", msg);
		if (accessToken != null) {
			intent.putExtra("accessToken", accessToken);
		}
		mActivity.setResult(resultCode, intent);
		mActivity.finish();
	}

	/**
	 * Get {@code AccessToken} with the url contains code
	 * 
	 * @param url
	 * @param listener
	 * @return true if the url is not empty and start with the redirecturl of
	 *         the platform
	 */
	public boolean getAccessTokenWithCode(String url,
			final OauthResultListener listener) {
		if (!TextUtils.isEmpty(url) && url.startsWith(info.redirecturl)) {
			platform.getAccessTokenWithCode(platform.getCodeFromUrl(url),
					new OauthResultListener() {
						@Override
						public void onSuccess(AccessToken accessToken) {
							if (listener != null) {
								listener.onSuccess(accessToken);
							}
							feedBack(Activity.RESULT_OK, null, accessToken);
						}

						@Override
						public void onError(String msg, Throwable throwable) {
							if (listener != null) {
								listener.onError(msg, throwable);
							}
							feedBack(RESULT_ERROR, msg, null);
						}

						@Override
						public void onCancel() {
							if (listener != null) {
								listener.onCancel();
							}
							feedBack(Activity.RESULT_CANCELED,
									"user cancel auth", null);
						}
					});
			return true;
		}
		return false;
	}
}
