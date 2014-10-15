package org.huangsu.sharesdk.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class AccessToken implements Parcelable {
	public String token;
	public String uid;
	public long expiresIn;
	public String refreshToken;

	public AccessToken(String token, String uid, long expiresIn) {
		this(token, uid, expiresIn, null);
	}

	public AccessToken(String token, String uid, long expiresIn,
			String refreshToken) {
		this.token = token;
		this.uid = uid;
		this.expiresIn = expiresIn;
		this.refreshToken = refreshToken;
	}

	public AccessToken(Parcel source) {
		token = source.readString();
		uid = source.readString();
		expiresIn = source.readLong();
		refreshToken=source.readString();
	}
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(token);
		dest.writeString(uid);
		dest.writeLong(expiresIn);
		dest.writeString(refreshToken);
	}
	

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AccessToken [token=");
		builder.append(token);
		builder.append(", uid=");
		builder.append(uid);
		builder.append(", expiresIn=");
		builder.append(expiresIn);
		builder.append(", refreshToken=");
		builder.append(refreshToken);
		builder.append("]");
		return builder.toString();
	}


	public final static Parcelable.Creator<AccessToken> CREATOR = new Creator<AccessToken>() {

		@Override
		public AccessToken[] newArray(int size) {
			return new AccessToken[size];
		}

		@Override
		public AccessToken createFromParcel(Parcel source) {
			return new AccessToken(source);
		}
	};
}
