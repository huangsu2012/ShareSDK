package org.huangsu.sharesdk.network;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.huangsu.sharesdk.listener.VolleyListener;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

public class StringRequest extends VolleyRequest<String> {
	protected Map<String, String> params;

	public StringRequest(String url, VolleyListener<String> listener) {
		this(url, null, listener);
	}

	public StringRequest(String url, Map<String, String> headers,
			VolleyListener<String> listener) {
		this(url, headers, null, listener);
	}

	public StringRequest(String url, Map<String, String> headers,
			Map<String, String> params, VolleyListener<String> listener) {
		super(params == null ? Method.GET : Method.POST, url, listener, headers);
		this.params = params;
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return params;
	}

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		String parsed;
		try {
			parsed = new String(response.data,
					HttpHeaderParser.parseCharset(response.headers));
		} catch (UnsupportedEncodingException e) {
			parsed = new String(response.data);
		}
		return Response.success(parsed,
				HttpHeaderParser.parseCacheHeaders(response));
	}

}
