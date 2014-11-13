package org.huangsu.sharesdk.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class PlatformInfo implements Parcelable, Comparable<PlatformInfo> {
	public String appkey;
	public String redirecturl;
	public String accesstokenurl;
	public String authorizeurl;
	public String appid;
	public String appsecret;
	public String platformid;
	public String scope;
	public int showpriority;
	public int nameId;

	public PlatformInfo(String platformid) {
		this.platformid = platformid;
	}

	public PlatformInfo(Parcel source) {
		appkey = source.readString();
		redirecturl = source.readString();
		accesstokenurl = source.readString();
		authorizeurl = source.readString();
		appid = source.readString();
		appsecret = source.readString();
		platformid = source.readString();
		scope = source.readString();
		showpriority = source.readInt();
		nameId = source.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(appkey);
		dest.writeString(redirecturl);
		dest.writeString(accesstokenurl);
		dest.writeString(authorizeurl);
		dest.writeString(appid);
		dest.writeString(appsecret);
		dest.writeString(platformid);
		dest.writeString(scope);
		dest.writeInt(showpriority);
		dest.writeInt(nameId);
	}

	public static final Parcelable.Creator<PlatformInfo> CREATOR = new Creator<PlatformInfo>() {

		@Override
		public PlatformInfo[] newArray(int size) {
			return new PlatformInfo[size];
		}

		@Override
		public PlatformInfo createFromParcel(Parcel source) {
			return new PlatformInfo(source);
		}
	};

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((platformid == null) ? 0 : platformid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlatformInfo other = (PlatformInfo) obj;
		if (platformid == null) {
			if (other.platformid != null)
				return false;
		} else if (!platformid.equals(other.platformid))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PlatformInfo [appkey=");
		builder.append(appkey);
		builder.append(", redirecturl=");
		builder.append(redirecturl);
		builder.append(", accesstokenurl=");
		builder.append(accesstokenurl);
		builder.append(", authorizeurl=");
		builder.append(authorizeurl);
		builder.append(", appid=");
		builder.append(appid);
		builder.append(", appsecret=");
		builder.append(appsecret);
		builder.append(", platformid=");
		builder.append(platformid);
		builder.append(", scope=");
		builder.append(scope);
		builder.append(", showpriority=");
		builder.append(showpriority);
		builder.append(", nameId=");
		builder.append(nameId);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int compareTo(PlatformInfo another) {
		if (showpriority > another.showpriority) {
			return 1;
		} else if (showpriority < another.showpriority) {
			return -1;
		} else {
			return 0;
		}
	}

}
