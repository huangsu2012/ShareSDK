package org.huangsu.sharesdk.core;

import java.util.HashMap;
import java.util.Map;

import org.huangsu.sharesdk.bean.AccessToken;
import org.huangsu.sharesdk.bean.ShareParams;
import org.huangsu.sharesdk.listener.OauthResultListener;
import org.huangsu.sharesdk.listener.ShareResultListener;
import org.huangsu.sharesdk.util.LogUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;

public class ProxyActivity extends Activity implements PlatformConstants,
		ShareResultListener, OauthResultListener {
	private long transaction;
	private String platformid;
	private Platform platform;
	private String action;
	private static Map<Long, OauthResultListener> oauthResultListeners = new HashMap<Long, OauthResultListener>(8);
	private static Map<Long, ShareParamsWrapper> shareParamsWrappers = new HashMap<Long, ShareParamsWrapper>(8);
	private ShareResultListener shareResultListener;
	private OauthResultListener oauthResultListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent intent = getIntent();
		transaction = intent.getLongExtra("transaction", 0);
		platformid = intent.getStringExtra("platformid");
		action = intent.getAction();
		LogUtil.d("action:%s", action);
		if (!Platform.LOGINACTION.equals(action)
				&& !Platform.SHAREACTION.equals(action)) {
			finish();
			return;
		}
		if (TextUtils.isEmpty(platformid) || transaction <= 0) {
			finish();
			return;
		}
		platform = PlatformFactory.getInstance(this).getPlatform(platformid);
		if (platform != null) {
			if (Platform.LOGINACTION.equals(action)) {
				oauthResultListener = oauthResultListeners.remove(transaction);
				if (platform.isOauth()) {
					platform.oauth(this, transaction);
				} else {
					platform.reoauth(this, transaction);
				}
			} else if (Platform.SHAREACTION.equals(action)) {
				ShareParamsWrapper paramsWrapper = shareParamsWrappers
						.remove(transaction);
				if (paramsWrapper != null) {
					shareResultListener = paramsWrapper.listener;
					platform.doShare(this, paramsWrapper.params);
				}
			}
		} else {
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		LogUtil.d("onActivityResult requestCode:%s,resultCode:%s,data:%s",
				requestCode, resultCode, data);
		platform.handleOauthResult(requestCode, resultCode, data, this);
	}

	@Override
	public void onShareStart() {

	}

	@Override
	public void onError(String msg, Throwable error) {
		if (shareResultListener != null) {
			shareResultListener.onError(msg, error);
		}
		if (oauthResultListener != null) {
			oauthResultListener.onError(msg, error);
		}
		finish();
	}

	@Override
	public void onSuccess() {
		if (shareResultListener != null) {
			shareResultListener.onSuccess();
		}
		finish();
	}

	@Override
	public void onCancel() {
		if (shareResultListener != null) {
			shareResultListener.onCancel();
		}
		if (oauthResultListener != null) {
			oauthResultListener.onCancel();
		}
		finish();
	}

	@Override
	public void onSuccess(AccessToken accessToken) {
		if (oauthResultListener != null) {
			oauthResultListener.onSuccess(accessToken);
		}
		finish();
	}

	static void addOauthListener(long transaction, OauthResultListener listener) {
		oauthResultListeners.put(transaction, listener);
	}

	static void addShareResultListener(long transaction, ShareParams params,
			ShareResultListener listener) {
		shareParamsWrappers.put(transaction, new ShareParamsWrapper(params,
				listener));
	}

	private static class ShareParamsWrapper {
		public ShareParams params;
		public ShareResultListener listener;

		public ShareParamsWrapper(ShareParams params,
				ShareResultListener listener) {
			this.params = params;
			this.listener = listener;
		}
	}
}
