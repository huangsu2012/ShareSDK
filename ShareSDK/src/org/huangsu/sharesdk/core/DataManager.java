package org.huangsu.sharesdk.core;

import org.huangsu.sharesdk.bean.AccessToken;
import org.huangsu.sharesdk.bean.BasicUserInfo;

public interface DataManager {
	public AccessToken getAccessToken(String platformid);

	public void saveAccessToken(String platformid, AccessToken accessToken);
	
	public BasicUserInfo getBasicUserInfo(String platformid);
	
	public void saveBasicUserInfo(String platformid, BasicUserInfo info);
}
