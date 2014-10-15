package org.huangsu.sharesdk.network;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.huangsu.sharesdk.listener.VolleyListener;
import org.huangsu.sharesdk.util.JsonUtil;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonObject;

public class JsonObjectRequest extends VolleyRequest<JsonObject> {
	protected Map<String, String> params;

	public JsonObjectRequest(String url, VolleyListener<JsonObject> listener) {
		this(url, null, listener);
	}

	public JsonObjectRequest(String url, Map<String, String> headers,
			VolleyListener<JsonObject> listener) {
		this(url, headers, null, listener);
	}

	public JsonObjectRequest(String url, Map<String, String> headers,
			Map<String, String> params, VolleyListener<JsonObject> listener) {
		super(params == null ? Method.GET : Method.POST, url, listener, headers);
		this.params = params;
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return params;
	}

	@Override
	protected Response<JsonObject> parseNetworkResponse(NetworkResponse response) {
		try {
			String jsonString = new String(response.data,
					HttpHeaderParser.parseCharset(response.headers));
			return Response.success(
					JsonUtil.getJsonObjectFromJsonStr(jsonString),
					HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		}
	}
}
