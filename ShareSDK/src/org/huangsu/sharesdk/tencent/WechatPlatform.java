package org.huangsu.sharesdk.tencent;

import org.huangsu.sharesdk.R;
import org.huangsu.sharesdk.network.NetworkClient;

import android.content.Context;

public class WechatPlatform extends WechatBase {

	protected WechatPlatform(Context context, NetworkClient client) {
		super(context, client);
	}

	@Override
	protected String getPlatformid() {
		return WECHAT;
	}

	@Override
	protected int getNameId() {
		return R.string.wechat;
	}

}
