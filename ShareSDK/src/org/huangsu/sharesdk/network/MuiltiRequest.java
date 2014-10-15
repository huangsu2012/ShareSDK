package org.huangsu.sharesdk.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.huangsu.sharesdk.listener.VolleyListener;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyLog;

public abstract class MuiltiRequest<T> extends VolleyRequest<T> {
	private final MultipartEntity entity = new MultipartEntity(
			HttpMultipartMode.BROWSER_COMPATIBLE);

	public MuiltiRequest(String url,
			VolleyListener<T> listener,
			Map<String, ContentBody> bodies) {
		super(Method.POST, url, listener);
		setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 0, 0));
		if (bodies != null && !bodies.isEmpty()) {
			Iterator<Entry<String, ContentBody>> entries = bodies.entrySet()
					.iterator();
			while (entries.hasNext()) {
				Map.Entry<String, ContentBody> entry = (Map.Entry<String, ContentBody>) entries
						.next();
				entity.addPart(entry.getKey(), entry.getValue());
			}
		}
	}

	public Map<String, String> getHeaders() throws AuthFailureError {
		if (headers != null) {
			headers.putAll(getDefaultHeaders());
		} else {
			headers = getDefaultHeaders();
		}
		return headers;
	}

	private Map<String, String> getDefaultHeaders() {
		headers = new HashMap<String, String>();
		headers.put("Accept",
				"application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		headers.put("Connection", "keep-alive");
		headers.put("Content-Length", Long.toString(entity.getContentLength()));
		return headers;
	}

	@Override
	public byte[] getBody() throws AuthFailureError {
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		try {
			entity.writeTo(arrayOutputStream);
		} catch (IOException e) {
			VolleyLog.e(e, "");
		}
		byte[] data = arrayOutputStream.toByteArray();
		try {
			arrayOutputStream.close();
		} catch (IOException e) {
			VolleyLog.e(e, "");
		}
		VolleyLog.d("contentLength:%s", data.length);
		return data;
	}

	@Override
	public String getBodyContentType() {
		return entity.getContentType().getValue();
	}
}
