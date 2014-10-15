package org.huangsu.sharesdk.listener;

import org.huangsu.sharesdk.bean.AccessToken;

public interface OauthResultListener extends BaseListener {
	void onSuccess(AccessToken accessToken);
}
