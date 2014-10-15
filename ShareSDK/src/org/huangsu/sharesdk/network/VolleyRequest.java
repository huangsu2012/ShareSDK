package org.huangsu.sharesdk.network;

import java.util.Map;

import org.huangsu.sharesdk.listener.VolleyListener;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;

public abstract class VolleyRequest<T> extends Request<T> {
	protected Map<String, String> headers;
	protected VolleyListener<T> listener;

	public VolleyRequest(int method, String url, VolleyListener<T> listener) {
		this(method, url, listener, null);
	}

	public VolleyRequest(int method, String url, VolleyListener<T> listener,
			Map<String, String> headers) {
		super(method, url, listener);
		this.headers = headers;
		this.listener = listener;
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		return headers == null ? super.getHeaders() : headers;
	}

	@Override
	protected void deliverResponse(T response) {
		if (listener != null) {
			listener.onResponse(response);
		}
	}

}
