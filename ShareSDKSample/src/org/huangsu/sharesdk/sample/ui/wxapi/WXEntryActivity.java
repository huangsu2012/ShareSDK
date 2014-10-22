package org.huangsu.sharesdk.sample.ui.wxapi;

import org.huangsu.sharesdk.core.PlatformConstants;
import org.huangsu.sharesdk.core.PlatformFactory;
import org.huangsu.sharesdk.tencent.WechatBase;

import android.app.Activity;
import android.os.Bundle;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
	private WechatBase wechatBase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		wechatBase = (WechatBase) PlatformFactory.getInstance(this)
				.getPlatform(PlatformConstants.WECHAT);
		wechatBase.handleIntent(getIntent(), this);
	}

	/**
	 * 微信给应用发送请求时回调
	 */
	@Override
	public void onReq(BaseReq baseReq) {

	}

	/**
	 * 应用给微信发送请求的响应结果回调
	 */
	@Override
	public void onResp(BaseResp baseResp) {
		wechatBase.handleResp(baseResp);
		finish();
	}
}
