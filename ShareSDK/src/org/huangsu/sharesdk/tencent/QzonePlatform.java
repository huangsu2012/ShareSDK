package org.huangsu.sharesdk.tencent;

import java.util.ArrayList;

import org.huangsu.sharesdk.R;
import org.huangsu.sharesdk.bean.ShareParams;
import org.huangsu.sharesdk.core.DataManager;
import org.huangsu.sharesdk.core.NetworkClient;
import org.huangsu.sharesdk.core.ProxyActivity;

import android.content.Context;
import android.os.Bundle;

import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;

public class QzonePlatform extends QQPlatform {

	protected QzonePlatform(Context context, NetworkClient client,
			DataManager dataManager) {
		super(context, client, dataManager);
	}

	@Override
	protected String getPlatformid() {
		return QZONE;
	}

	@Override
	protected int getNameId() {
		return R.string.qzone;
	}

	@Override
	protected void doShare(ProxyActivity activity, ShareParams params,
			IUiListener listener) {
		Bundle data = new Bundle();
		int shareType = QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT;
		data.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, shareType);
		data.putString(QzoneShare.SHARE_TO_QQ_TITLE, params.getTitle());
		data.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, params.getContent());
		data.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, params.getTargetUrl());
		ArrayList<String> imageUrls = null;
		if (params.getImageUrls() != null && params.getImageUrls().length > 0) {
			int length = params.getImageUrls().length;
			imageUrls = new ArrayList<String>(length);
			for (int i = 0; i < length; i++) {
				imageUrls.add(params.getImageUrls()[i]);
			}
		} else {
			imageUrls = new ArrayList<String>(1);
		}
		data.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
		tencent.shareToQzone(activity, data, listener);
	}

}
