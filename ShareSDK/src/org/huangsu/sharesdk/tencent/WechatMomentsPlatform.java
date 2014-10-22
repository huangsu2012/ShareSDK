package org.huangsu.sharesdk.tencent;

import org.huangsu.sharesdk.R;
import org.huangsu.sharesdk.core.DataManager;
import org.huangsu.sharesdk.core.NetworkClient;

import android.content.Context;

public class WechatMomentsPlatform extends WechatBase {

	

	protected WechatMomentsPlatform(Context context, NetworkClient client,DataManager dataManager) {
		super(context, client,dataManager);
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
