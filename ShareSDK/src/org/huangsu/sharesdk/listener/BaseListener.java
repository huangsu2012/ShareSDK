package org.huangsu.sharesdk.listener;

public interface BaseListener {
	void onError(String msg,Throwable throwable);
	void onCancel();
}
