package org.huangsu.sharesdk.ui;

import java.util.Map;

import org.huangsu.sharesdk.R;
import org.huangsu.sharesdk.bean.AccessToken;
import org.huangsu.sharesdk.bean.PlatformInfo;
import org.huangsu.sharesdk.core.Platform;
import org.huangsu.sharesdk.core.PlatformFactory;
import org.huangsu.sharesdk.listener.OauthResultListener;
import org.huangsu.sharesdk.util.LogUtil;
import org.huangsu.sharesdk.util.URLUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class OauthActivity extends Activity {
	private WebView webView = null;
	private String platformid;
	private Platform platform;
	private PlatformInfo info;
	private ProgressDialog progressDialog;
	private final static int RESULT_ERROR = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		platformid = intent.getStringExtra("platformid");
		if (TextUtils.isEmpty(platformid)) {
			finish();
			return;
		}
		platform = PlatformFactory.getInstance(this).getPlatform(platformid);
		if (platform == null) {
			finish();
			return;
		}
		setContentView(R.layout.oauth_layout);
		info = platform.getInfo();
		webView = (WebView) findViewById(R.id.sharesdk_oauth_webview);
		webView.setWebViewClient(new WebViewClientImpl());
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setUseWideViewPort(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		// webView.setWebChromeClient(new WebViewChromeClient());
		CookieManager.getInstance().removeAllCookie();
		Map<String, String> authParams = platform.getCodeReqParams();
		if (authParams != null) {
			String encodedURL = URLUtil.constructUrl(info.authorizeurl,
					authParams, null);
			LogUtil.d("the encoded authrize url:%s", encodedURL);
			progressDialog = ProgressDialog.show(this, null,
					getString(R.string.sharesdk_loading_page));
			webView.loadUrl(encodedURL);
		} else {
			feedBack(RESULT_ERROR, "缺少必要授权参数", null);
		}
	}

	/**
	 * 返回结果
	 * 
	 * @param accessToken
	 */
	private void feedBack(int resultCode, String msg, AccessToken accessToken) {
		LogUtil.d("resultCode:%s,msg:%s", resultCode, msg);
		Intent intent = getIntent();
		intent.putExtra("msg", msg);
		if (accessToken != null) {
			intent.putExtra("accessToken", accessToken);
		}
		setResult(resultCode, intent);
		finish();
	}

	@Override
	public void onBackPressed() {
		if (webView.canGoBack()) {
			webView.goBack();
		} else {
			feedBack(RESULT_CANCELED, null, null);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		hideProgress();
		if (webView != null) {
			ViewGroup group = (ViewGroup) webView.getParent();
			group.removeAllViews();
			webView.removeAllViews();
			webView.destroy();
		}
	}

	private void hideProgress() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.hide();
		}
	}

	private class WebViewClientImpl extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			LogUtil.d("the webview loadurl is:%s", url);
			if (url.startsWith(info.redirecturl)) {
				platform.getAccessTokenWithCode(platform.getCodeFromUrl(url),
						new OauthResultListener() {

							@Override
							public void onSuccess(AccessToken accessToken) {
								hideProgress();
								feedBack(RESULT_OK, null, accessToken);
							}

							@Override
							public void onError(String msg, Throwable throwable) {
								hideProgress();
								feedBack(RESULT_ERROR, msg, null);
							}

							@Override
							public void onCancel() {
								hideProgress();
								feedBack(RESULT_CANCELED, null, null);

							}
						});
				progressDialog = ProgressDialog.show(OauthActivity.this, null,
						getString(R.string.sharesdk_loading));
				progressDialog.show();
			} else {
				view.loadUrl(url);
			}
			return true;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			if (progressDialog != null) {
				progressDialog.show();
			}
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			hideProgress();
		}
	}

	// private class WebViewChromeClient extends WebChromeClient {
	// @Override
	// public void onProgressChanged(WebView view, int newProgress) {
	// super.onProgressChanged(view, newProgress);
	// progressBar.setProgress(newProgress);
	// }
	// }
}
