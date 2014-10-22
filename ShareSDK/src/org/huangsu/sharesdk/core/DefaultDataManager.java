package org.huangsu.sharesdk.core;

import org.huangsu.sharesdk.bean.AccessToken;
import org.huangsu.sharesdk.bean.BasicUserInfo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

public class DefaultDataManager implements DataManager {
	private Context mContext;
	private String mSharedPrefix;
	public final static String TOKEN = "token";// 令牌
	public final static String UID = "uid";// 用户id
	public final static String EXPIREDIN = "expires_in";// 授权过期时间
	public final static String REFRESHTOKEN = "refresh_token";// 刷新令牌
	public final static String USERNAME = "user_name";// 用户的昵称
	public final static String USERAVATAR = "user_avatar";// 用户头像
	public final static String USERGENDER = "user_gender";// 用户性别

	public DefaultDataManager(Context context, String sharedPrefix) {
		if (context == null) {
			throw new NullPointerException("context is null");
		}
		mContext = context.getApplicationContext();
		mSharedPrefix = sharedPrefix == null ? "" : sharedPrefix;
	}

	@Override
	public AccessToken getAccessToken(String platformid) {
		if (!TextUtils.isEmpty(platformid)) {
			SharedPreferences preferences = mContext.getSharedPreferences(
					mSharedPrefix + platformid, Context.MODE_PRIVATE);
			if (preferences != null) {
				String token = preferences.getString(TOKEN, null);
				String uid = preferences.getString(UID, null);
				long expiresIn = preferences.getLong(EXPIREDIN, 0);
				if (!TextUtils.isEmpty(token)) {
					return new AccessToken(token, uid, expiresIn,
							preferences.getString(REFRESHTOKEN, null));
				}
			}
		}
		return null;
	}

	@Override
	public void saveAccessToken(String platformid, AccessToken accessToken) {
		if (TextUtils.isEmpty(platformid) || accessToken == null) {
			return;
		}
		SharedPreferences preferences = mContext.getSharedPreferences(
				mSharedPrefix + platformid, Activity.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(TOKEN, accessToken.token);
		editor.putLong(EXPIREDIN, accessToken.expiresIn);
		editor.putString(UID, accessToken.uid);
		editor.putString(REFRESHTOKEN, accessToken.refreshToken);
		editor.commit();
	}

	@Override
	public BasicUserInfo getBasicUserInfo(String platformid) {
		if (!TextUtils.isEmpty(platformid)) {
			SharedPreferences preferences = mContext.getSharedPreferences(
					mSharedPrefix + platformid, Activity.MODE_PRIVATE);
			if (preferences != null) {
				String uid = preferences.getString(UID, null);
				int gender = preferences.getInt(USERGENDER, 0);
				String avatar = preferences.getString(USERAVATAR, null);
				String name = preferences.getString(USERNAME, null);
				if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(name)) {
					return new BasicUserInfo(uid, gender, avatar, name);
				}
			}
		}
		return null;
	}

	@Override
	public void saveBasicUserInfo(String platformid, BasicUserInfo info) {
		if (info == null || TextUtils.isEmpty(platformid)) {
			return;
		}
		SharedPreferences preferences = mContext.getSharedPreferences(
				mSharedPrefix + platformid, Activity.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(USERAVATAR, info.avatar);
		editor.putInt(USERGENDER, info.gender);
		editor.putString(USERNAME, info.name);
		editor.commit();

	}

}
