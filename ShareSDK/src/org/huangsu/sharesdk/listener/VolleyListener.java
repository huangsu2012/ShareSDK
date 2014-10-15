package org.huangsu.sharesdk.listener;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

public abstract class VolleyListener<T> implements Listener<T>,
		ErrorListener {
}
