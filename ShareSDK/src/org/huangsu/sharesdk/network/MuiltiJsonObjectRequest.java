package org.huangsu.sharesdk.network;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.entity.mime.content.ContentBody;
import org.huangsu.sharesdk.listener.VolleyListener;
import org.huangsu.sharesdk.util.JsonUtil;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonObject;

public class MuiltiJsonObjectRequest extends MuiltiRequest<JsonObject> {
	
	public MuiltiJsonObjectRequest(String url,
			VolleyListener<JsonObject> listener,
			Map<String, ContentBody> bodies) {
		super(url, listener, bodies);
	}

	@Override
	protected Response<JsonObject> parseNetworkResponse(NetworkResponse response) {
		try {
			String jsonString = new String(response.data,
					HttpHeaderParser.parseCharset(response.headers));
			return Response.success(JsonUtil.getJsonObjectFromJsonStr(jsonString),
					HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} 
	}

}
