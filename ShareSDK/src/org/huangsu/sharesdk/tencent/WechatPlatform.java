package org.huangsu.sharesdk.tencent;

import org.huangsu.sharesdk.R;
import org.huangsu.sharesdk.core.DataManager;
import org.huangsu.sharesdk.core.NetworkClient;

import android.content.Context;

public class WechatPlatform extends WechatBase {

	protected WechatPlatform(Context context, NetworkClient client,DataManager dataManager) {
		super(context, client,dataManager);
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
