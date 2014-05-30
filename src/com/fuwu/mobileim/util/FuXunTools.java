package com.fuwu.mobileim.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.fuwu.mobileim.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class FuXunTools {
	private static Bitmap bm = null;
	protected static ImageLoader imageLoader = ImageLoader.getInstance();
	static DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.moren)
			.showImageForEmptyUri(R.drawable.moren)
			.showImageOnFail(R.drawable.moren).cacheInMemory(true)
			.cacheOnDisk(true).considerExifParams(true)
			.displayer(new RoundedBitmapDisplayer(20)).build();
	private static ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

	// 判断手机号合法性
	public static boolean isMobileNO(String mobiles) {
		Pattern p = Pattern
				.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	/**
	 * 16以内的十进制数转换成 四位二进制
	 */
	public static String toNumber(int number) {
		String str = Integer.toBinaryString(number);
		if (number < 2) {
			return "000" + str;
		} else if (number < 4) {
			return "00" + str;
		} else if (number < 8) {
			return "0" + str;
		} else {
			return str;
		}
	}

	/**
	 * 判断 字符串,如："1010"，，对应区间内是否存在"1"
	 */
	public static Boolean isExist(String str, int index1, int index2) {
		Boolean boo = false;
		for (int i = index1; i <= index2; i++) {
			if ((str.charAt(i) + "").equals("1")) {
				boo = true;
			}
		}
		return boo;
	}

	public static void set_bk(final String url, final ImageView imageView) {

		final Handler mHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case 0:
					Drawable drawable = (Drawable) msg.obj;
					imageView.setImageDrawable(drawable);
					break;
				default:
					break;
				}
			}
		};

		Thread thread = new Thread() {
			public void run() {
				HttpClient hc = new DefaultHttpClient();
				Drawable face_drawable;
				try {
					Log.i("linshi------------", url);
					URL myurl = new URL(url);
					// 获得连接
					HttpURLConnection conn = (HttpURLConnection) myurl
							.openConnection();
					conn.setConnectTimeout(6000);// 设置超时
					conn.setDoInput(true);
					conn.setUseCaches(false);// 不缓存
					conn.connect();
					InputStream is = conn.getInputStream();// 获得图片的数据流
					// bm =decodeSampledBitmapFromStream(is,150,150);

					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = false;
					// options.outWidth = 159;
					// options.outHeight = 159;
					options.inSampleSize = 1;
					bm = BitmapFactory.decodeStream(is, null, options);
					Log.i("linshi", bm.getWidth() + "---" + bm.getHeight());
					is.close();
					if (bm != null) {
						Log.i("linshi",
								bm.getWidth() + "---2---" + bm.getHeight());
						File f = new File(Urlinterface.head_pic, "bbb");

						if (f.exists()) {
							f.delete();
						}
						if (!f.getParentFile().exists()) {
							f.getParentFile().mkdirs();
						}
						Log.i("linshi", "----1");
						FileOutputStream out = new FileOutputStream(f);
						Log.i("linshi", "----6");
						bm.compress(Bitmap.CompressFormat.PNG, 60, out);
						out.flush();
						out.close();
						Log.i("linshi", "已经保存");
						Log.i("linshi", "----6");
						Log.i("linshi", "已经保存2");

						face_drawable = new BitmapDrawable(bm);
						Message msg = new Message();// 创建Message 对象
						msg.what = 0;
						msg.obj = face_drawable;
						mHandler.sendMessage(msg);

					}

				} catch (Exception e) {
					Log.i("linshi", "发生异常");
					// Log.i("linshi", url);
				}

			}
		};

		thread.start();

	}
	public static void getBitmap(final String url) {


		Thread thread = new Thread() {
			public void run() {
				HttpClient hc = new DefaultHttpClient();
				try {
					Log.i("linshi------------", url);
					URL myurl = new URL(url);
					// 获得连接
					HttpURLConnection conn = (HttpURLConnection) myurl
							.openConnection();
					conn.setConnectTimeout(6000);// 设置超时
					conn.setDoInput(true);
					conn.setUseCaches(false);// 不缓存
					conn.connect();
					InputStream is = conn.getInputStream();// 获得图片的数据流
					// bm =decodeSampledBitmapFromStream(is,150,150);
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = false;
					// options.outWidth = 159;
					// options.outHeight = 159;
					options.inSampleSize = 1;
					bm = BitmapFactory.decodeStream(is, null, options);
					Log.i("linshi", bm.getWidth() + "---" + bm.getHeight());
					is.close();
					if (bm != null) {
						File f = new File(Urlinterface.head_pic, "bbb");
						if (f.exists()) {
							f.delete();
						}
						if (!f.getParentFile().exists()) {
							f.getParentFile().mkdirs();
						}
						Log.i("linshi", "----1");
						FileOutputStream out = new FileOutputStream(f);
						Log.i("linshi", "----6");
						bm.compress(Bitmap.CompressFormat.PNG, 60, out);
						out.flush();
						out.close();
						Log.i("linshi", "已经保存");
					}

				} catch (Exception e) {
					Log.i("linshi", "发生异常");
					// Log.i("linshi", url);
				}

			}
		};

		thread.start();

	}

	public static void setBackground(final String url, final ImageView imageView) {
		imageLoader.displayImage(url, imageView, options, animateFirstListener);
	}
	
	
}
