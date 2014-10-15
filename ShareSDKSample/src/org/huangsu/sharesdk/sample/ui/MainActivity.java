package org.huangsu.sharesdk.sample.ui;

import org.huangsu.sharesdk.bean.AccessToken;
import org.huangsu.sharesdk.bean.ShareParams;
import org.huangsu.sharesdk.core.Platform;
import org.huangsu.sharesdk.core.PlatformConstants;
import org.huangsu.sharesdk.core.PlatformFactory;
import org.huangsu.sharesdk.listener.OauthResultListener;
import org.huangsu.sharesdk.listener.ShareResultListener;
import org.huangsu.sharesdk.util.LogUtil;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		Button shareToQQ;

		Button shareToQZone;

		Button shareToWechat;

		Button shareToWechatMoments;

		Button shareToSina;

		Button shareToTencentWeibo;

		Button shareToRenRen;

		Button shareToDouban;

		Button loginToWechat;
		Button loginToQQ;
		Button loginToSina;
		Button loginToRenRen;
		Button loginToDouBan;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			shareToQQ = (Button) view.findViewById(R.id.share_to_qq);
			shareToQZone = (Button) view.findViewById(R.id.share_to_qzone);
			shareToWechat = (Button) view.findViewById(R.id.share_to_wechat);
			shareToWechatMoments = (Button) view
					.findViewById(R.id.share_to_wechat_moments);
			shareToSina = (Button) view.findViewById(R.id.share_to_sina);
			shareToTencentWeibo = (Button) view
					.findViewById(R.id.share_to_tencent_weibo);
			shareToRenRen = (Button) view.findViewById(R.id.share_to_renren);
			shareToDouban = (Button) view.findViewById(R.id.share_to_douban);
			OnClickListener listener = new OnClick();
			shareToQQ.setOnClickListener(listener);
			shareToQZone.setOnClickListener(listener);
			shareToWechat.setOnClickListener(listener);
			shareToWechatMoments.setOnClickListener(listener);
			shareToSina.setOnClickListener(listener);
			shareToTencentWeibo.setOnClickListener(listener);
			shareToRenRen.setOnClickListener(listener);
			shareToDouban.setOnClickListener(listener);

			loginToDouBan = (Button) view.findViewById(R.id.login_to_douban);
			loginToQQ = (Button) view.findViewById(R.id.login_to_qq);
			loginToRenRen = (Button) view.findViewById(R.id.login_to_renren);
			loginToSina = (Button) view.findViewById(R.id.login_to_sina);
			loginToWechat = (Button) view.findViewById(R.id.login_to_wechat);
			loginToDouBan.setOnClickListener(listener);
			loginToQQ.setOnClickListener(listener);
			loginToRenRen.setOnClickListener(listener);
			loginToSina.setOnClickListener(listener);
			loginToWechat.setOnClickListener(listener);
		}

		private class OnClick implements OnClickListener {

			@Override
			public void onClick(View v) {
				Platform platform = null;
				String platformid = null;
				ShareParams params = new ShareParams("这只是个测试");
				params.setComment("这只是个测试");
				params.setTitle("这只是个测试");
				params.setTargetUrl("http://www.baidu.com");
				boolean login = false;
				switch (v.getId()) {
				case R.id.login_to_qq:
					login = true;
				case R.id.share_to_qq:
					platformid = PlatformConstants.QQ;
					break;
				case R.id.share_to_qzone:
					platformid = PlatformConstants.QZONE;
					break;
				case R.id.login_to_wechat:
					login = true;
				case R.id.share_to_wechat:
					platformid = PlatformConstants.WECHAT;
					break;
				case R.id.share_to_wechat_moments:
					platformid = PlatformConstants.WECHATMOMENTS;
					break;
				case R.id.login_to_sina:
					login = true;
				case R.id.share_to_sina:
					platformid = PlatformConstants.SINAWEIBO;
					break;
				case R.id.share_to_tencent_weibo:
					platformid = PlatformConstants.TENCENTWEIBO;
					break;
				case R.id.login_to_renren:
					login = true;
				case R.id.share_to_renren:
					platformid = PlatformConstants.RENREN;
					break;
				case R.id.login_to_douban:
					login = true;
				case R.id.share_to_douban:
					platformid = PlatformConstants.DOUBAN;
					break;
				default:
					break;
				}
				platform = PlatformFactory.getInstance(getActivity())
						.getPlatform(platformid);
				LogUtil.d("platformid:%s,login:%s", platformid,login);
				if (platform != null) {
					final String platformName = getString(platform.getInfo().nameId);
					if (login) {
						platform.login(new OauthResultListener() {

							@Override
							public void onSuccess(AccessToken accessToken) {
								Toast.makeText(
										getActivity(),
										"login to" + platformName
												+ "success,accessToken:"
												+ accessToken.toString(),
										Toast.LENGTH_SHORT).show();
								;

							}

							@Override
							public void onError(String msg, Throwable throwable) {
								Toast.makeText(
										getActivity(),
										"login to" + platformName
												+ "fail,fail msg:" + msg,
										Toast.LENGTH_SHORT).show();
								;

							}

							@Override
							public void onCancel() {
								Toast.makeText(getActivity(),
										"user cancel login to" + platformName,
										Toast.LENGTH_SHORT).show();

							}
						}, false);
					} else {
						platform.share(params, new ShareResultListener() {
							@Override
							public void onSuccess() {
								Toast.makeText(
										getActivity().getApplicationContext(),
										"分享到" + platformName + "成功",
										Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onShareStart() {
								Toast.makeText(
										getActivity().getApplicationContext(),
										"正在分享到" + platformName,
										Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onError(String msg, Throwable error) {
								Toast.makeText(
										getActivity().getApplicationContext(),
										"分享到" + platformName + "失败",
										Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onCancel() {
								Toast.makeText(
										getActivity().getApplicationContext(),
										"用户取消分享到" + platformName,
										Toast.LENGTH_SHORT).show();
							}
						});
					}
				}
			}

		}
	}

}
