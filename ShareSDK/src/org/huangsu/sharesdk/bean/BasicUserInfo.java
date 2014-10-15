package org.huangsu.sharesdk.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 用户基础信息
 * 
 * @author huangsu2012@gmail.com 2014年10月13日
 * 
 */
public class BasicUserInfo implements Parcelable {
	// 用户id
	private String uid;
	// 用户性别 0男，1 女，2未知
	private int gender;
	// 用户头像
	private String avatar;
	
	private String name;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BasicUserInfo(String uid, int gender, String avatar,String name) {
		this.uid = uid;
		this.gender = gender;
		this.avatar = avatar;
		this.name=name;
	}

	public BasicUserInfo(Parcel source) {
		uid = source.readString();
		gender = source.readInt();
		avatar = source.readString();
		name=source.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(uid);
		dest.writeInt(gender);
		dest.writeString(avatar);
		dest.writeString(name);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BasicUserInfo [uid=");
		builder.append(uid);
		builder.append(", gender=");
		builder.append(gender);
		builder.append(", avatar=");
		builder.append(avatar);
		builder.append(", name=");
		builder.append(name);
		builder.append("]");
		return builder.toString();
	}

	public final static Parcelable.Creator<BasicUserInfo> CREATOR = new Creator<BasicUserInfo>() {

		@Override
		public BasicUserInfo[] newArray(int size) {
			return new BasicUserInfo[size];
		}

		@Override
		public BasicUserInfo createFromParcel(Parcel source) {
			return new BasicUserInfo(source);
		}
	};

}
