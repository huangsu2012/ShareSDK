package org.huangsu.sharesdk.ui;

import org.huangsu.sharesdk.R;
import org.huangsu.sharesdk.core.OauthActivityHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class OauthActivity extends Activity {
	private WebView webView = null;
	private ProgressDialog progressDialog;
	private OauthActivityHelper helper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		helper = new OauthActivityHelper(this);
		if (!helper.hanldeIntent()) {
			return;
		}
		setContentView(R.layout.oauth_layout);
		webView = (WebView) findViewById(R.id.sharesdk_oauth_webview);
		webView.setWebViewClient(new WebViewClientImpl());
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setUseWideViewPort(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		// webView.setWebChromeClient(new WebViewChromeClient());
		CookieManager.getInstance().removeAllCookie();
		progressDialog = ProgressDialog.show(this, null,
				getString(R.string.sharesdk_loading_page));
		webView.loadUrl(helper.getEncodedAuthorizeurl());
	}

	@Override
	public void onBackPressed() {
		if (webView.canGoBack()) {
			webView.goBack();
		} else {
			helper.feedBack(RESULT_CANCELED, null, null);
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
			if (helper.getAccessTokenWithCode(url, null)) {
				progressDialog = ProgressDialog.show(OauthActivity.this, null,
						getString(R.string.sharesdk_loading));
				return false;
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
}
