package org.huangsu.sharesdk.listener;

import org.huangsu.sharesdk.bean.BasicUserInfo;

public interface BasicUserInfoListener {
	void onError(String msg,Throwable throwable);
	void onSuccess(BasicUserInfo info);
}
