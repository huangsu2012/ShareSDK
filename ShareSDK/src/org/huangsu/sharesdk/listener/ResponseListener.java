package org.huangsu.sharesdk.listener;

import com.google.gson.JsonObject;

public interface ResponseListener {
	void onError(String msg, Throwable error);

	void onSuccess(JsonObject result);
}
