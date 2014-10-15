package org.huangsu.sharesdk.tencent;

import org.huangsu.sharesdk.R;
import org.huangsu.sharesdk.network.NetworkClient;

import android.content.Context;

public class WechatMomentsPlatform extends WechatBase {

	

	protected WechatMomentsPlatform(Context context, NetworkClient client) {
		super(context, client);
	}

	@Override
	protected String getPlatformid() {
		return WECHATMOMENTS;
	}

	@Override
	protected int getNameId() {
		return R.string.wechatmoments;
	}

}
