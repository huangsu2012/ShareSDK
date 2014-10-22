package org.huangsu.sharesdk.core;

import java.util.Map;

import org.apache.http.entity.mime.content.ContentBody;
import org.huangsu.sharesdk.listener.ResponseListener;

public interface NetworkClient {

	public void get(String url, Map<String, String> headers,
			Map<String, String> params, ResponseListener listener);

	public void post(String url, Map<String, String> headers,
			Map<String, String> params, ResponseListener listener);

	public void muiltiPost(String url, Map<String, String> headers,
			Map<String, ContentBody> params, ResponseListener listener);
}
