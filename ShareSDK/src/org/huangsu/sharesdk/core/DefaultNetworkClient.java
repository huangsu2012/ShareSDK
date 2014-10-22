package org.huangsu.sharesdk.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.huangsu.sharesdk.listener.ResponseListener;
import org.huangsu.sharesdk.util.URLUtil;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The default implementation of NetworkClient,it's too simple,therefore ,you
 * should supply your own implementation
 * 
 * @author huangsu2012@gmail.com
 * 
 */
public class DefaultNetworkClient implements NetworkClient {

	@Override
	public void get(String url, Map<String, String> headers,
			Map<String, String> params, ResponseListener listener) {

		new CommonNetworkTask(url, headers, params, listener, false).execute();

	}

	@Override
	public void post(String url, Map<String, String> headers,
			Map<String, String> params, ResponseListener listener) {

		new CommonNetworkTask(url, headers, params, listener, true).execute();

	}

	@Override
	public void muiltiPost(String url, Map<String, String> headers,
			Map<String, ContentBody> params, ResponseListener listener) {
		new MuiltiNetworkTask(url, headers, params, listener).execute();
	}

	private static abstract class NetworkTask extends
			AsyncTask<Void, Void, JsonObject> {
		protected String url;
		private Map<String, String> headers;
		private ResponseListener listener;
		private String msg;
		private Throwable throwable;

		public NetworkTask(String url, Map<String, String> headers,
				ResponseListener listener) {
			if (headers == null) {
				headers = Collections.emptyMap();
			}
			this.url = url;
			this.headers = headers;
			this.listener = listener;
		}

		@Override
		protected JsonObject doInBackground(Void... params) {
			if (!TextUtils.isEmpty(url)
					|| (!url.startsWith("http://") && !url
							.startsWith("https://"))) {
				try {
					HttpClient client = AndroidHttpClient.newInstance("");
					HttpUriRequest httpRequest = createHttpRequest();
					if (httpRequest != null) {
						addHeaders(httpRequest, headers);
						int timeoutMs = 5000;
						HttpParams httpParams = httpRequest.getParams();
						HttpConnectionParams.setConnectionTimeout(httpParams,
								timeoutMs);
						HttpConnectionParams
								.setSoTimeout(httpParams, timeoutMs);
						HttpResponse response = client.execute(httpRequest);
						int status = response.getStatusLine().getStatusCode();
						if (status == HttpStatus.SC_OK
								&& response.getEntity() != null) {
							String result = EntityUtils.toString(
									response.getEntity(), "UTF-8");
							if (!TextUtils.isEmpty(result)) {
								JsonParser jsonParser = new JsonParser();
								JsonElement element = jsonParser.parse(result);
								if (element.isJsonObject()) {
									JsonObject jsonObject = element
											.getAsJsonObject();
									return jsonObject;
								}
							} else {
								msg = "no data returned from server!";
							}
						} else if (response.getEntity() == null) {
							msg = "no data returned from server!";
						} else {
							msg = "unexpected status code:" + status;
						}
					}
				} catch (IOException e) {
					throwable = e;
				}
			} else {
				msg = "illegal url";
			}
			return null;
		}

		private static void addHeaders(HttpUriRequest httpRequest,
				Map<String, String> headers) {
			for (String key : headers.keySet()) {
				httpRequest.setHeader(key, headers.get(key));
			}
		}

		protected abstract HttpUriRequest createHttpRequest();

		@Override
		protected void onPostExecute(JsonObject result) {
			if (result != null) {
				listener.onSuccess(result);
			} else {
				listener.onError(msg, throwable);
			}
		}
	}

	private class CommonNetworkTask extends NetworkTask {
		private Map<String, String> params;
		private boolean post;

		public CommonNetworkTask(String url, Map<String, String> headers,
				Map<String, String> params, ResponseListener listener,
				boolean post) {
			super(url, headers, listener);
			this.post = post;
			this.params = params;
		}

		@Override
		protected HttpUriRequest createHttpRequest() {
			HttpUriRequest request = null;
			if (post && params != null) {
				request = new HttpPost(url);
				ByteArrayEntity entity = new ByteArrayEntity(encodeParameters(
						params, "UTF-8"));
				((HttpPost) request).setEntity(entity);
			} else {
				request = new HttpGet(URLUtil.constructUrl(url, params));
			}
			return request;
		}

		private byte[] encodeParameters(Map<String, String> params,
				String paramsEncoding) {
			StringBuilder encodedParams = new StringBuilder();
			try {
				for (Map.Entry<String, String> entry : params.entrySet()) {
					encodedParams.append(URLEncoder.encode(entry.getKey(),
							paramsEncoding));
					encodedParams.append('=');
					encodedParams.append(URLEncoder.encode(entry.getValue(),
							paramsEncoding));
					encodedParams.append('&');
				}
				return encodedParams.toString().getBytes(paramsEncoding);
			} catch (UnsupportedEncodingException uee) {
				throw new RuntimeException("Encoding not supported: "
						+ paramsEncoding, uee);
			}
		}
	}

	private class MuiltiNetworkTask extends NetworkTask {
		private Map<String, ContentBody> params;

		public MuiltiNetworkTask(String url, Map<String, String> headers,
				Map<String, ContentBody> params, ResponseListener listener) {
			super(url, headers, listener);
			this.params = params;
		}

		@Override
		protected HttpUriRequest createHttpRequest() {
			HttpPost postRequest = new HttpPost(url);
			MultipartEntity entity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			if (params != null) {
				Set<Entry<String, ContentBody>> entries = params.entrySet();
				Iterator<Entry<String, ContentBody>> iterator = entries
						.iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, ContentBody> entry = (Map.Entry<String, ContentBody>) iterator
							.next();
					entity.addPart(entry.getKey(), entry.getValue());
				}
			}
			postRequest.addHeader(entity.getContentType());
			postRequest.setEntity(entity);
			return postRequest;
		}
	}

}
