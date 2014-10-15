package org.huangsu.sharesdk.network;

import java.util.Map;

import org.apache.http.entity.mime.content.ContentBody;
import org.huangsu.sharesdk.listener.ResponseListener;
import org.huangsu.sharesdk.listener.VolleyListener;
import org.huangsu.sharesdk.util.URLUtil;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

public class VolleyNetworkClient implements NetworkClient {
	private static NetworkClient client;
	private RequestQueue queue;

	private VolleyNetworkClient(Context context) {
		queue = Volley.newRequestQueue(context);
	}

	public static synchronized NetworkClient getInstance(Context context) {
		if (client == null) {
			client = new VolleyNetworkClient(context);
		}
		return client;
	}

	@Override
	public void get(String url, ResponseListener listener) {
		get(url, null, listener);
	}

	public void get(String url, Map<String, String> headers,
			ResponseListener listener) {
		Request<?> request = new JsonObjectRequest(url, headers,
				new ListenerAdapter(listener));
		queue.add(request);
	}

	public void get(String url, Map<String, String> headers,
			Map<String, String> params, ResponseListener listener) {
		Request<?> request = new JsonObjectRequest(URLUtil.constructUrl(url,
				params, "utf-8"), headers, new ListenerAdapter(listener));
		queue.add(request);
	}

	@Override
	public void post(String url, Map<String, String> params,
			ResponseListener listener) {
		post(url, null, params, listener);
	}

	public void post(String url, Map<String, String> headers,
			Map<String, String> params, ResponseListener listener) {
		Request<?> request = new JsonObjectRequest(url, headers, params,
				new ListenerAdapter(listener));
		queue.add(request);
	}

	@Override
	public void muiltiPost(String url, Map<String, ContentBody> params,
			ResponseListener listener) {
		muiltiPost(url, null, params, listener);
	}

	public void muiltiPost(String url, Map<String, String> headers,
			Map<String, ContentBody> params, ResponseListener listener) {
		Request<?> request = new MuiltiJsonObjectRequest(url,
				new ListenerAdapter(listener), params);
		queue.add(request);
	}

	private class ListenerAdapter extends VolleyListener<JsonObject> {
		private ResponseListener listener;

		public ListenerAdapter(ResponseListener listener) {
			this.listener = listener;
		}

		@Override
		public void onResponse(JsonObject response) {
			if (listener != null) {
				listener.onSuccess(response);
			}

		}

		@Override
		public void onErrorResponse(VolleyError error) {
			if (listener != null) {
				listener.onError(null, error);
			}
		}

	}

}
