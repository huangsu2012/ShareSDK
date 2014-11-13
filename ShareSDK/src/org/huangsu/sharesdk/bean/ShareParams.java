package org.huangsu.sharesdk.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class ShareParams implements Parcelable{
	private String content;
	private String title;
	private String comment;
	private String[] imageUrls;
	private String imageName;
	private byte[] bitmap;
	private String targetUrl;

	public final static Parcelable.Creator<ShareParams> CREATOR = new Creator<ShareParams>() {

		@Override
		public ShareParams[] newArray(int size) {
			return new ShareParams[size];
		}

		@Override
		public ShareParams createFromParcel(Parcel source) {
			return new ShareParams(source);
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(content);
		dest.writeString(title);
		dest.writeString(comment);
		dest.writeInt(imageUrls == null ? 0 : imageUrls.length);
		dest.writeStringArray(imageUrls);
		dest.writeString(imageName);
		dest.writeInt(bitmap == null ? 0 : bitmap.length);
		dest.writeByteArray(bitmap);
		dest.writeString(targetUrl);
	}

	public ShareParams(String content) {
		setContent(content);
	}

	public ShareParams(Parcel source) {
		content = source.readString();
		title = source.readString();
		comment = source.readString();
		int imageUrlsLength = source.readInt();
		if (imageUrlsLength > 0) {
			imageUrls = new String[imageUrlsLength];
			source.readStringArray(imageUrls);
		}
		imageName = source.readString();
		int byteLength = source.readInt();
		if (byteLength > 0) {
			bitmap = new byte[byteLength];
			source.readByteArray(bitmap);
		}
		targetUrl = source.readString();
	}

	/**
	 * 设置分享内容标题，必须有的平台：人人
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	/**
	 * 设置分享内容，所有平台都必须有
	 * 
	 * @param content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	/**
	 * 设置对分享内容的描述（也叫做对分享内容的评论），必须有的平台：人人
	 * 
	 * @param comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public String[] getImageUrls() {
		return imageUrls;
	}

	/**
	 * 设置分享的图片地址
	 * 
	 * @param imageUrl
	 */
	public void setImageUrls(String[] imageUrls) {
		this.imageUrls = imageUrls;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public byte[] getBitmap() {
		return bitmap;
	}

	public void setBitmap(byte[] bitmap) {
		this.bitmap = bitmap;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		if (TextUtils.isEmpty(targetUrl) || !targetUrl.startsWith("http://")) {
			this.targetUrl = "http://www.cslemi.com";
		} else {
			this.targetUrl = targetUrl;
		}
	}
	

}
